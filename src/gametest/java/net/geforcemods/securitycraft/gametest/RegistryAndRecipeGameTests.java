package net.geforcemods.securitycraft.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleItemStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.platform.registry.DeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

/** Acceptance checks for registry binding and the cross-loader adapters for bucket recipes. */
public class RegistryAndRecipeGameTests {
    @GameTest
    public void fullCatalogIsBoundInNativeRegistries(GameTestHelper helper) {
        checkRegistry(helper, SCContent.BLOCKS, BuiltInRegistries.BLOCK);
        checkRegistry(helper, SCContent.ITEMS, BuiltInRegistries.ITEM);
        checkRegistry(helper, SCContent.BLOCK_ENTITY_TYPES, BuiltInRegistries.BLOCK_ENTITY_TYPE);
        checkRegistry(helper, SCContent.ENTITY_TYPES, BuiltInRegistries.ENTITY_TYPE);
        checkRegistry(helper, SCContent.DATA_COMPONENTS, BuiltInRegistries.DATA_COMPONENT_TYPE);
        checkRegistry(helper, SCContent.FLUIDS, BuiltInRegistries.FLUID);
        checkRegistry(helper, SCContent.MENU_TYPES, BuiltInRegistries.MENU);
        checkRegistry(helper, SCContent.PARTICLE_TYPES, BuiltInRegistries.PARTICLE_TYPE);
        checkRegistry(helper, SCContent.RECIPE_SERIALIZERS, BuiltInRegistries.RECIPE_SERIALIZER);
        checkRegistry(helper, SCContent.GAME_RULES, BuiltInRegistries.GAME_RULE);
        helper.assertTrue(SCContent.BLOCKS.getEntries().size() >= 734, "The complete block catalog must cover the original JAR's 734 blockstates");
        for (var entry : SCContent.DATA_SERIALIZERS.getEntries()) {
            helper.assertTrue(FabricEntityDataRegistry.get(entry.getId()) == entry.get(), "Custom entity serializers must be registered by namespaced ID: " + entry.getId());
        }
        for (var entry : SCContent.ENTITY_TYPES.getEntries()) {
            helper.assertTrue(entry.get().trackDeltas(), "Custom entity velocity synchronization must stay enabled: " + entry.getId());
        }
        helper.succeed();
    }

    private static <T> void checkRegistry(GameTestHelper helper, DeferredRegister<T> registrations, Registry<T> registry) {
        for (var entry : registrations.getEntries()) {
            helper.assertTrue(entry.isBound() && registry.getValue(entry.getId()) == entry.get(), "Unbound or inconsistent native registry entry: " + entry.getId());
        }
    }

    @GameTest(maxTicks = 920)
    public void fakeFluidsBrewThroughNativeStand(GameTestHelper helper) {
        helper.setBlock(1, 1, 1, Blocks.BREWING_STAND);
        var stand = (BrewingStandBlockEntity) helper.getBlockEntity(new net.minecraft.core.BlockPos(1, 1, 1), BrewingStandBlockEntity.class);
        helper.assertTrue(stand.canPlaceItem(0, new ItemStack(Items.WATER_BUCKET)), "Automation must be able to place a bucket into a potion slot");
        stand.setItem(0, new ItemStack(Items.WATER_BUCKET));
        stand.setItem(1, new ItemStack(Items.LAVA_BUCKET));
        stand.setItem(3, PotionContents.createItemStack(Items.POTION, Potions.HARMING));
        stand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
        helper.runAtTickTime(430, () -> {
            helper.assertTrue(stand.getItem(0).is(SCContent.FAKE_WATER_BUCKET.get()), "Harming potion must brew a fake water bucket");
            helper.assertTrue(stand.getItem(1).is(Items.LAVA_BUCKET), "Harming potion must not transform a lava bucket");
            stand.setItem(3, PotionContents.createItemStack(Items.LINGERING_POTION, Potions.STRONG_HEALING));
        });
        helper.runAtTickTime(870, () -> {
            helper.assertTrue(stand.getItem(0).is(SCContent.FAKE_WATER_BUCKET.get()), "The first brewed bucket must remain unchanged");
            helper.assertTrue(stand.getItem(1).is(SCContent.FAKE_LAVA_BUCKET.get()), "Strong healing potion must brew a fake lava bucket");
            helper.succeed();
        });
    }

    @GameTest
    public void fakeBucketsExposeTransactionalFluidStorage(GameTestHelper helper) {
        verifyBucket(helper, new ItemStack(SCContent.FAKE_WATER_BUCKET.get()), FluidVariant.of(SCContent.FAKE_WATER.get()));
        verifyBucket(helper, new ItemStack(SCContent.FAKE_LAVA_BUCKET.get()), FluidVariant.of(SCContent.FAKE_LAVA.get()));
        helper.succeed();
    }

    private static void verifyBucket(GameTestHelper helper, ItemStack bucket, FluidVariant fluid) {
        SingleItemStorage slot = new SingleItemStorage() {
            @Override protected long getCapacity(ItemVariant variant) { return 1; }
        };
        slot.variant = ItemVariant.of(bucket);
        slot.amount = 1;
        var context = ContainerItemContext.ofSingleSlot(slot);
        var storage = context.find(FluidStorage.ITEM);
        helper.assertTrue(storage != null, "A fake bucket must expose Fabric fluid storage");
        try (Transaction transaction = Transaction.openOuter()) {
            helper.assertTrue(storage.extract(fluid, FluidConstants.BUCKET, transaction) == FluidConstants.BUCKET, "A fake bucket must extract one bucket of its own fluid");
        }
        helper.assertTrue(slot.variant.equals(ItemVariant.of(bucket)) && slot.amount == 1, "Aborting extraction must preserve the filled bucket");
        try (Transaction transaction = Transaction.openOuter()) {
            helper.assertTrue(storage.extract(fluid, FluidConstants.BUCKET, transaction) == FluidConstants.BUCKET, "Committed extraction must return one bucket of fluid");
            transaction.commit();
        }
        helper.assertTrue(slot.variant.isOf(Items.BUCKET) && slot.amount == 1, "Draining a fake bucket must return an empty bucket");
        var emptyStorage = context.find(FluidStorage.ITEM);
        helper.assertTrue(emptyStorage != null, "An empty bucket must expose fluid insertion");
        try (Transaction transaction = Transaction.openOuter()) {
            helper.assertTrue(emptyStorage.insert(fluid, FluidConstants.BUCKET, transaction) == FluidConstants.BUCKET, "An empty bucket must accept fake fluid");
            transaction.commit();
        }
        helper.assertTrue(slot.variant.equals(ItemVariant.of(bucket)) && slot.amount == 1, "Filling must recreate the correct fake bucket");
    }
}
