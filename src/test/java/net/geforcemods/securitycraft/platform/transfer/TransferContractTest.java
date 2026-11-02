package net.geforcemods.securitycraft.platform.transfer;

import static org.junit.jupiter.api.Assertions.*;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.geforcemods.securitycraft.inventory.ExtractOnlyResourceHandler;
import net.geforcemods.securitycraft.inventory.InsertOnlyResourceHandler;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Contract tests at the native Fabric storage boundary used by hoppers, pipes and sentry ammunition. */
class TransferContractTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
            .build(net.minecraft.data.registries.VanillaRegistries.createLookup())
            .forEach(net.minecraft.core.component.DataComponentInitializers.PendingComponents::apply);
    }

    @Test
    void abortedAndNestedTransactionsDoNotLoseOrDuplicateItems() {
        SimpleContainer inventory = new SimpleContainer(new ItemStack(Items.DIAMOND, 8));
        var handler = VanillaContainerWrapper.of(inventory);
        try (Transaction outer = Transaction.openOuter()) {
            assertEquals(3, handler.extract(0, ItemResource.of(Items.DIAMOND), 3, outer));
            try (Transaction nested = outer.openNested()) {
                assertEquals(2, handler.extract(0, ItemResource.of(Items.DIAMOND), 2, nested));
                nested.commit();
            }
            assertEquals(3, inventory.getItem(0).getCount());
        }
        assertEquals(8, inventory.getItem(0).getCount());
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(3, handler.extract(0, ItemResource.of(Items.DIAMOND), 3, transaction));
            transaction.commit();
        }
        assertEquals(5, inventory.getItem(0).getCount());
    }

    @Test
    void emptySlotCapacityAndBlankResourceRemainWellDefined() {
        SimpleContainer inventory = new SimpleContainer(1);
        var storage = VanillaContainerWrapper.of(inventory).asStorage();
        assertTrue(storage.getSlot(0).isResourceBlank());
        assertEquals(0, storage.getSlot(0).getAmount());
        assertEquals(64, storage.getSlot(0).getCapacity());
    }

    @Test
    void extractionRestrictionsSurviveBothFabricAccessPaths() {
        SimpleContainer protectedInventory = new SimpleContainer(new ItemStack(Items.DIAMOND, 8));
        var handler = new InsertOnlyResourceHandler<>(VanillaContainerWrapper.of(protectedInventory));
        SimpleContainer hopper = new SimpleContainer(5);
        assertEquals(0, StorageUtil.move(handler.asStorage(), ContainerStorage.of(hopper, null), item -> true, 1, null));
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, handler.asStorage().getSlot(0).extract(ItemVariant.of(Items.DIAMOND), 1, transaction));
            assertEquals(1, handler.asStorage().insert(ItemVariant.of(Items.DIAMOND), 1, transaction));
            transaction.commit();
        }
        assertEquals(9, protectedInventory.getItem(0).getCount());
        assertTrue(hopper.isEmpty());
    }

    @Test
    void scannerReferenceSlotsCannotBeExtractedByAutomation() {
        SimpleContainer inventory = new SimpleContainer(12);
        inventory.setItem(0, new ItemStack(Items.DIAMOND, 1));
        inventory.setItem(10, new ItemStack(Items.DIAMOND, 4));
        var handler = new ExtractOnlyResourceHandler<>(VanillaContainerWrapper.of(inventory)) {
            @Override
            public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
                return index < 10 ? 0 : super.extract(index, resource, amount, transaction);
            }
        };
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(4, handler.asStorage().extract(ItemVariant.of(Items.DIAMOND), 10, transaction));
            assertEquals(0, handler.asStorage().getSlot(0).extract(ItemVariant.of(Items.DIAMOND), 1, transaction));
            transaction.commit();
        }
        assertEquals(1, inventory.getItem(0).getCount());
        assertTrue(inventory.getItem(10).isEmpty());
    }

    @Test
    void slotInsertionCannotBypassAHandlersValidityCheck() {
        SimpleContainer inventory = new SimpleContainer(1);
        var handler = new DelegatingResourceHandler<>(VanillaContainerWrapper.of(inventory)) {
            @Override
            public boolean isValid(int slot, ItemResource resource) {
                return resource.is(Items.DIAMOND);
            }
        };
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, handler.asStorage().getSlot(0).insert(ItemVariant.of(Items.DIRT), 1, transaction));
            assertEquals(1, handler.asStorage().getSlot(0).insert(ItemVariant.of(Items.DIAMOND), 1, transaction));
            transaction.commit();
        }
        assertTrue(inventory.getItem(0).is(Items.DIAMOND));
    }

    @Test
    void itemComponentsArePreservedAndDifferentVariantsDoNotMerge() {
        ItemStack named = new ItemStack(Items.DIAMOND, 3);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("SMP access token"));
        SimpleContainer inventory = new SimpleContainer(2);
        inventory.setItem(0, named);
        var handler = VanillaContainerWrapper.of(inventory);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(2, handler.asStorage().insert(ItemVariant.of(Items.DIAMOND), 2, transaction));
            transaction.commit();
        }
        assertEquals(3, inventory.getItem(0).getCount());
        assertEquals(Component.literal("SMP access token"), handler.getResource(0).toStack().get(DataComponents.CUSTOM_NAME));
        assertEquals(2, inventory.getItem(1).getCount());
        assertNull(inventory.getItem(1).get(DataComponents.CUSTOM_NAME));
    }

    @Test
    void combinedChestUpdatesBothInventoriesAtomically() {
        SimpleContainer first = new SimpleContainer(new ItemStack(Items.DIAMOND, 2));
        SimpleContainer second = new SimpleContainer(new ItemStack(Items.DIAMOND, 3));
        var combined = new CombinedResourceHandler<>(VanillaContainerWrapper.of(first), VanillaContainerWrapper.of(second));
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(5, combined.asStorage().extract(ItemVariant.of(Items.DIAMOND), 8, transaction));
        }
        assertEquals(2, first.getItem(0).getCount());
        assertEquals(3, second.getItem(0).getCount());
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(5, combined.asStorage().extract(ItemVariant.of(Items.DIAMOND), 8, transaction));
            transaction.commit();
        }
        assertTrue(first.isEmpty());
        assertTrue(second.isEmpty());
    }
}
