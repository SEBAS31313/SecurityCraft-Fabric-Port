package net.geforcemods.securitycraft.gametest;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.blockentities.InventoryScannerBlockEntity;
import net.geforcemods.securitycraft.blockentities.KeypadChestBlockEntity;
import net.geforcemods.securitycraft.blockentities.ReinforcedHopperBlockEntity;
import net.geforcemods.securitycraft.util.PasscodeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

/** Dedicated-server integration tests, including vanilla hopper tick code and registered Fabric providers. */
public class InventoryGameTests {
    private static final String OWNER = UUID.nameUUIDFromBytes("SecurityCraftInventoryOwner".getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
    private static final String INTRUDER = UUID.nameUUIDFromBytes("SecurityCraftInventoryIntruder".getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
    private static final BlockPos CHEST = new BlockPos(2, 2, 2);
    private static final BlockPos BELOW = new BlockPos(2, 1, 2);

    @GameTest(maxTicks = 80)
    public void vanillaHopperCannotExtractFromOwnedChest(GameTestHelper helper) {
        KeypadChestBlockEntity chest = chest(helper, CHEST);
        helper.setBlock(BELOW, Blocks.HOPPER);
        chest.setItem(0, new ItemStack(Items.DIAMOND, 8));
        helper.runAfterDelay(24, () -> {
            HopperBlockEntity hopper = helper.getBlockEntity(BELOW, HopperBlockEntity.class);
            helper.assertTrue(hopper.isEmpty(), "Vanilla hopper bypassed protected chest extraction");
            helper.assertValueEqual(chest.getItem(0).getCount(), 8, "Protected inventory remains intact");
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 80)
    public void ownersReinforcedHopperCanExtractWithoutDuplication(GameTestHelper helper) {
        KeypadChestBlockEntity chest = chest(helper, CHEST);
        ReinforcedHopperBlockEntity hopper = reinforcedHopper(helper, BELOW, OWNER, "InventoryOwner");
        chest.setItem(0, new ItemStack(Items.DIAMOND, 8));
        helper.runAfterDelay(24, () -> {
            int extracted = count(hopper);
            helper.assertTrue(extracted > 0, "Same-owner reinforced hopper could not extract");
            helper.assertValueEqual(extracted + count(chest), 8, "Hopper transfer conserves items");
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 80)
    public void intrudersReinforcedHopperCannotExtract(GameTestHelper helper) {
        KeypadChestBlockEntity chest = chest(helper, CHEST);
        ReinforcedHopperBlockEntity hopper = reinforcedHopper(helper, BELOW, INTRUDER, "InventoryIntruder");
        chest.setItem(0, new ItemStack(Items.DIAMOND, 8));
        helper.runAfterDelay(24, () -> {
            helper.assertTrue(hopper.isEmpty(), "Different-owner reinforced hopper extracted protected items");
            helper.assertValueEqual(count(chest), 8, "Intruder cannot remove chest contents");
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 80)
    public void vanillaHopperCanInsertIntoProtectedChest(GameTestHelper helper) {
        BlockPos chestPos = BELOW;
        KeypadChestBlockEntity chest = chest(helper, chestPos);
        helper.setBlock(CHEST, Blocks.HOPPER);
        HopperBlockEntity hopper = helper.getBlockEntity(CHEST, HopperBlockEntity.class);
        hopper.setItem(0, new ItemStack(Items.DIAMOND, 8));
        helper.runAfterDelay(24, () -> {
            helper.assertTrue(count(chest) > 0, "Protected chest incorrectly rejected allowed hopper insertion");
            helper.assertValueEqual(count(chest) + count(hopper), 8, "Hopper insertion conserves items");
            helper.succeed();
        });
    }

    @GameTest
    public void scannerAutomationCannotExtractReferenceSlots(GameTestHelper helper) {
        helper.setBlock(CHEST, SCContent.INVENTORY_SCANNER.get());
        InventoryScannerBlockEntity scanner = helper.getBlockEntity(CHEST, InventoryScannerBlockEntity.class);
        scanner.setOwner(OWNER, "InventoryOwner");
        reinforcedHopper(helper, BELOW, OWNER, "InventoryOwner");
        scanner.getContents().set(0, new ItemStack(Items.DIAMOND, 1));
        scanner.getContents().set(10, new ItemStack(Items.DIAMOND, 4));
        var storage = ItemStorage.SIDED.find(helper.getLevel(), helper.absolutePos(CHEST), Direction.DOWN);
        helper.assertTrue(storage != null, "Scanner did not expose its registered Fabric storage");
        SimpleContainer output = new SimpleContainer(5);
        long moved = StorageUtil.move(storage, ContainerStorage.of(output, null), item -> true, 64, null);
        helper.assertValueEqual(moved, 4L, "Only collected scanner inventory is extractable");
        helper.assertValueEqual(scanner.getContents().get(0).getCount(), 1, "Scanner reference item must remain");
        helper.assertTrue(scanner.getContents().get(10).isEmpty(), "Scanner collected slot should be extracted");
        helper.assertValueEqual(count(output), 4, "Collected scanner items arrive without duplication");
        helper.succeed();
    }

    @GameTest(maxTicks = 200)
    public void chestPasscodeSaltOwnershipAndContentsSurviveSerialization(GameTestHelper helper) {
        KeypadChestBlockEntity chest = chest(helper, CHEST);
        chest.setItem(0, new ItemStack(Items.DIAMOND, 7));
        String passcode = PasscodeUtils.hashPasscodeWithoutSalt("SMP-4932");
        AtomicReference<KeypadChestBlockEntity> restored = new AtomicReference<>();
        AtomicReference<byte[]> verifiedHash = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        chest.hashAndSetPasscode(passcode, hash -> {
            try {
                chest.setSaveSalt(true);
                CompoundTag saved = chest.saveWithFullMetadata(helper.getLevel().registryAccess());
                BlockEntity loaded = BlockEntity.loadStatic(chest.getBlockPos(), chest.getBlockState(), saved, helper.getLevel().registryAccess());
                if (!(loaded instanceof KeypadChestBlockEntity reloaded))
                    throw new IllegalStateException("Saved keypad chest could not be deserialized");
                restored.set(reloaded);
                PasscodeUtils.hashPasscode(passcode, reloaded.getSalt(), verifiedHash::set);
            } catch (Throwable failure) {
                error.set(failure);
            }
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(error.get() == null, "Chest serialization failed: " + error.get());
            KeypadChestBlockEntity loaded = restored.get();
            helper.assertTrue(loaded != null && verifiedHash.get() != null, "Waiting for original passcode hashing worker");
            helper.assertTrue(Arrays.equals(loaded.getPasscode(), verifiedHash.get()), "Persisted passcode and salt must still authenticate");
            helper.assertValueEqual(loaded.getOwner().getUUID(), OWNER, "Chest UUID persists");
            helper.assertValueEqual(count(loaded), 7, "Chest inventory persists");
            CompoundTag clientTag = chest.getUpdateTag(helper.getLevel().registryAccess());
            helper.assertFalse(clientTag.contains("passcode"), "Server must not send passcode hashes in block updates");
            helper.assertFalse(clientTag.contains("saltKey"), "Server must not send passcode salt keys in block updates");
        });
    }

    @GameTest(maxTicks = 100)
    public void disguiseModuleAddsAndRemovesRealBlockLight(GameTestHelper helper) {
        // An opaque enclosure makes the result independent of other parallel tests and skylight.
        for (int x = 1; x <= 6; x++)
            for (int y = 1; y <= 6; y++)
                for (int z = 1; z <= 6; z++)
                    if (x == 1 || x == 6 || y == 1 || y == 6 || z == 1 || z == 6)
                        helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
        BlockPos source = new BlockPos(3, 3, 3);
        helper.setBlock(source, SCContent.REINFORCED_DISPENSER.get());
        var dispenser = helper.getBlockEntity(source, net.geforcemods.securitycraft.blockentities.ReinforcedDispenserBlockEntity.class);
        dispenser.setOwner(OWNER, "InventoryOwner");
        ItemStack disguise = new ItemStack(SCContent.DISGUISE_MODULE.get());
        disguise.set(SCContent.SAVED_BLOCK_STATE.get(), new net.geforcemods.securitycraft.components.SavedBlockState(
            Blocks.GLOWSTONE.defaultBlockState(), net.geforcemods.securitycraft.util.StandingOrWallType.NONE));
        disguise.set(net.minecraft.core.component.DataComponents.CONTAINER,
            net.minecraft.world.item.component.ItemContainerContents.fromItems(java.util.List.of(new ItemStack(Blocks.GLOWSTONE))));
        dispenser.insertModule(disguise, false);
        BlockPos absoluteSource = helper.absolutePos(source);
        helper.assertValueEqual(net.geforcemods.securitycraft.platform.AuxiliaryLightManager.get(helper.getLevel()).getLightAt(absoluteSource), 15,
            "Installing the disguise must publish its light emission");
        int workerEmission = java.util.concurrent.CompletableFuture.supplyAsync(() ->
            net.geforcemods.securitycraft.platform.AuxiliaryLightManager.get(helper.getLevel()).getLightAt(absoluteSource)).join();
        helper.assertValueEqual(workerEmission, 15, "The lighting worker must see the emission without querying a live block entity");
        BlockPos litNeighbor = helper.absolutePos(source.east());
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(helper.getLevel().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, litNeighbor) >= 14,
                "Glowstone disguise must propagate actual block light to its neighbor"))
            .thenExecute(() -> dispenser.removeModule(net.geforcemods.securitycraft.misc.ModuleType.DISGUISE, false))
            .thenWaitUntil(() -> helper.assertValueEqual(helper.getLevel().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, litNeighbor), 0,
                "Removing the disguise must remove the propagated block light"))
            .thenSucceed();
    }

    @GameTest(maxTicks = 40)
    public void reinforcedObsidianFrameCanCreateVanillaPortal(GameTestHelper helper) {
        for (int x = 1; x <= 4; x++) {
            for (int y = 1; y <= 5; y++) {
                if (x == 1 || x == 4 || y == 1 || y == 5)
                    helper.setBlock(new BlockPos(x, y, 3), SCContent.REINFORCED_OBSIDIAN.get());
            }
        }
        helper.setBlock(new BlockPos(2, 2, 3), Blocks.FIRE);
        helper.succeedWhen(() -> {
            for (int x = 2; x <= 3; x++)
                for (int y = 2; y <= 4; y++)
                    helper.assertBlockPresent(Blocks.NETHER_PORTAL, new BlockPos(x, y, 3));
            helper.assertBlockPresent(SCContent.REINFORCED_OBSIDIAN.get(), new BlockPos(1, 3, 3));
        });
    }

    @GameTest(maxTicks = 40)
    public void breakingStoneMineExplodesForIntruderButNotOwner(GameTestHelper helper) {
        var owner = PortTestPlayers.create(helper, "sc_mineowner");
        var intruder = PortTestPlayers.create(helper, "sc_mineother");
        try {
            BlockPos minePos = new BlockPos(3, 2, 3);
            helper.setBlock(minePos, SCContent.STONE_MINE.get());
            var mine = (net.geforcemods.securitycraft.api.IOwnable) helper.getLevel().getBlockEntity(helper.absolutePos(minePos));
            mine.setOwner(owner.getUUID().toString(), owner.getGameProfile().name());
            var victim = helper.spawn(net.minecraft.world.entity.EntityTypes.PIG, new BlockPos(4, 2, 3));
            victim.setNoAi(true);
            float initialHealth = victim.getHealth();
            owner.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
            owner.gameMode.destroyBlock(helper.absolutePos(minePos));
            helper.assertValueEqual(victim.getHealth(), initialHealth, "Owner may remove their stone mine without detonating it");
            helper.setBlock(minePos, SCContent.STONE_MINE.get());
            mine = (net.geforcemods.securitycraft.api.IOwnable) helper.getLevel().getBlockEntity(helper.absolutePos(minePos));
            mine.setOwner(owner.getUUID().toString(), owner.getGameProfile().name());
            intruder.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
            intruder.gameMode.destroyBlock(helper.absolutePos(minePos));
            helper.assertTrue(victim.getHealth() < initialHealth, "Intruder breaking the stone mine must cause real explosion damage");
            helper.assertBlockPresent(Blocks.AIR, minePos);
            helper.succeed();
        } finally {
            PortTestPlayers.remove(owner, intruder);
        }
    }

    private static KeypadChestBlockEntity chest(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, SCContent.KEYPAD_CHEST.get());
        KeypadChestBlockEntity chest = helper.getBlockEntity(pos, KeypadChestBlockEntity.class);
        chest.setOwner(OWNER, "InventoryOwner");
        return chest;
    }

    private static ReinforcedHopperBlockEntity reinforcedHopper(GameTestHelper helper, BlockPos pos, String uuid, String name) {
        helper.setBlock(pos, SCContent.REINFORCED_HOPPER.get());
        ReinforcedHopperBlockEntity hopper = helper.getBlockEntity(pos, ReinforcedHopperBlockEntity.class);
        hopper.setOwner(uuid, name);
        return hopper;
    }

    private static int count(net.minecraft.world.Container inventory) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
            total += inventory.getItem(slot).getCount();
        return total;
    }
}
