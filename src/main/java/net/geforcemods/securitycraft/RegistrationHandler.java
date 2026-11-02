package net.geforcemods.securitycraft;

import java.lang.reflect.Field;
import java.util.List;

import net.geforcemods.securitycraft.blockentities.AbstractKeypadFurnaceBlockEntity;
import net.geforcemods.securitycraft.blockentities.BlockPocketManagerBlockEntity;
import net.geforcemods.securitycraft.blockentities.ClaymoreBlockEntity;
import net.geforcemods.securitycraft.blockentities.InventoryScannerBlockEntity;
import net.geforcemods.securitycraft.blockentities.KeypadBarrelBlockEntity;
import net.geforcemods.securitycraft.blockentities.KeypadChestBlockEntity;
import net.geforcemods.securitycraft.blockentities.LaserBlockBlockEntity;
import net.geforcemods.securitycraft.blockentities.ReinforcedChiseledBookshelfBlockEntity;
import net.geforcemods.securitycraft.blockentities.ReinforcedDispenserBlockEntity;
import net.geforcemods.securitycraft.blockentities.ReinforcedDropperBlockEntity;
import net.geforcemods.securitycraft.blockentities.ReinforcedHopperBlockEntity;
import net.geforcemods.securitycraft.blockentities.ReinforcedShelfBlockEntity;
import net.geforcemods.securitycraft.blockentities.SecureTradingStationBlockEntity;
import net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity;
import net.geforcemods.securitycraft.blockentities.TrophySystemBlockEntity;
import net.geforcemods.securitycraft.entity.AbstractSecuritySeaBoat;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.geforcemods.securitycraft.network.client.InteractWithFrame;
import net.geforcemods.securitycraft.network.client.OpenScreen;
import net.geforcemods.securitycraft.network.client.RefreshDisguisableModel;
import net.geforcemods.securitycraft.network.client.SendManualPages;
import net.geforcemods.securitycraft.network.client.SetCameraView;
import net.geforcemods.securitycraft.network.client.SetTrophySystemTarget;
import net.geforcemods.securitycraft.network.client.ToggleAlarmSound;
import net.geforcemods.securitycraft.network.client.UpdateLaserColors;
import net.geforcemods.securitycraft.network.client.UpdateLogger;
import net.geforcemods.securitycraft.network.server.CheckBriefcasePasscode;
import net.geforcemods.securitycraft.network.server.CheckPasscode;
import net.geforcemods.securitycraft.network.server.ClearChangeDetectorServer;
import net.geforcemods.securitycraft.network.server.ClearLoggerServer;
import net.geforcemods.securitycraft.network.server.DismountCamera;
import net.geforcemods.securitycraft.network.server.MountCamera;
import net.geforcemods.securitycraft.network.server.RemoteControlMine;
import net.geforcemods.securitycraft.network.server.RemoveCameraTag;
import net.geforcemods.securitycraft.network.server.RemoveMineFromMRAT;
import net.geforcemods.securitycraft.network.server.RemovePositionFromSSS;
import net.geforcemods.securitycraft.network.server.RemoveSentryFromSRAT;
import net.geforcemods.securitycraft.network.server.RequestSecureTradingStationTransactions;
import net.geforcemods.securitycraft.network.server.SetBriefcasePasscodeAndOwner;
import net.geforcemods.securitycraft.network.server.SetCameraPowered;
import net.geforcemods.securitycraft.network.server.SetDefaultCameraViewingDirection;
import net.geforcemods.securitycraft.network.server.SetGhostSlot;
import net.geforcemods.securitycraft.network.server.SetKeycardUses;
import net.geforcemods.securitycraft.network.server.SetListModuleData;
import net.geforcemods.securitycraft.network.server.SetPasscode;
import net.geforcemods.securitycraft.network.server.SetSentryMode;
import net.geforcemods.securitycraft.network.server.SetStateOnDisguiseModule;
import net.geforcemods.securitycraft.network.server.SyncAlarmSettings;
import net.geforcemods.securitycraft.network.server.SyncBlockChangeDetector;
import net.geforcemods.securitycraft.network.server.SyncBlockPocketManager;
import net.geforcemods.securitycraft.network.server.SyncBlockReinforcer;
import net.geforcemods.securitycraft.network.server.SyncFrame;
import net.geforcemods.securitycraft.network.server.SyncKeycardSettings;
import net.geforcemods.securitycraft.network.server.SyncLaserSideConfig;
import net.geforcemods.securitycraft.network.server.SyncProjector;
import net.geforcemods.securitycraft.network.server.SyncRiftStabilizer;
import net.geforcemods.securitycraft.network.server.SyncSSSSettingsOnServer;
import net.geforcemods.securitycraft.network.server.SyncSecureRedstoneInterface;
import net.geforcemods.securitycraft.network.server.SyncTrophySystem;
import net.geforcemods.securitycraft.network.server.ToggleBlockPocketManager;
import net.geforcemods.securitycraft.network.server.ToggleModule;
import net.geforcemods.securitycraft.network.server.ToggleNightVision;
import net.geforcemods.securitycraft.network.server.ToggleOption;
import net.geforcemods.securitycraft.network.server.UpdateSliderValue;
import net.geforcemods.securitycraft.util.RegisterItemBlock;
import net.geforcemods.securitycraft.util.Reinforced;
import net.geforcemods.securitycraft.util.SCItemGroup;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColorCollection;
import net.geforcemods.securitycraft.platform.registry.DeferredHolder;

public class RegistrationHandler {
	private RegistrationHandler() {}

    public static void registerBlockItemsAndSounds() {

			//register item blocks from annotated fields
			for (Field field : SCContent.class.getFields()) {
				try {
					if (field.isAnnotationPresent(Reinforced.class) && field.getAnnotation(Reinforced.class).registerBlockItem()) {
						SCItemGroup group = field.getAnnotation(Reinforced.class).itemGroup();
						List<DeferredHolder<?, ?>> holders = SCContent.annotatedConstantToStream(field.get(null)).toList();

						for (DeferredHolder<?, ?> holder : holders) {
							Block block = (Block) holder.get();
							Item blockItem = new BlockItem(block, SCContent.setId(holder.getKey().identifier().getPath(), new Item.Properties().fireResistant(), true));

							net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.ITEM, Utils.getRegistryName(block), blockItem);

							if (group != SCItemGroup.MANUAL)
								SCCreativeModeTabs.STACKS_FOR_ITEM_GROUPS.get(group).add(new ItemStackTemplate(blockItem));
						}
					}
					else if (field.isAnnotationPresent(RegisterItemBlock.class)) {
						SCItemGroup group = field.getAnnotation(RegisterItemBlock.class).value();
						List<DeferredHolder<?, ?>> holders = SCContent.annotatedConstantToStream(field.get(null)).toList();

						for (DeferredHolder<?, ?> holder : holders) {
							Block block = (Block) holder.get();
							Item blockItem = new BlockItem(block, SCContent.setId(holder.getKey().identifier().getPath(), new Item.Properties(), true));

							net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.ITEM, Utils.getRegistryName(block), blockItem);

							if (group != SCItemGroup.MANUAL)
								SCCreativeModeTabs.STACKS_FOR_ITEM_GROUPS.get(group).add(new ItemStackTemplate(blockItem));
						}
					}
				}
				catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
        for (SCSounds sound : SCSounds.values())
            net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT, sound.location, sound.event);
    }

    public static void register() {
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(SCContent.SENTRY_ENTITY.get(), Mob.createMobAttributes());
        onRegisterCapabilities();
        net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register((tab, output) ->
            onCreativeModeTabRegister(net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).orElseThrow(), output));
        java.util.Set<net.minecraft.world.level.block.state.BlockState> rodStates = new java.util.HashSet<>();
        SCContent.REINFORCED_LIGHTNING_ROD.forEach(rod -> rodStates.addAll(rod.get().getStateDefinition().getPossibleStates()));
        net.geforcemods.securitycraft.mixin.platform.PoiTypesAccess.securitycraft$registerBlockStates(
            net.minecraft.core.registries.BuiltInRegistries.POINT_OF_INTEREST_TYPE.get(PoiTypes.LIGHTNING_ROD).orElseThrow(), rodStates);
    }

	public static void onRegisterCapabilities() {
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.KEYPAD_BLAST_FURNACE_BLOCK_ENTITY.get(), AbstractKeypadFurnaceBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.KEYPAD_FURNACE_BLOCK_ENTITY.get(), AbstractKeypadFurnaceBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.KEYPAD_SMOKER_BLOCK_ENTITY.get(), AbstractKeypadFurnaceBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.BLOCK_POCKET_MANAGER_BLOCK_ENTITY.get(), BlockPocketManagerBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.CLAYMORE_BLOCK_ENTITY.get(), ClaymoreBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.INVENTORY_SCANNER_BLOCK_ENTITY.get(), InventoryScannerBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.KEYPAD_BARREL_BLOCK_ENTITY.get(), KeypadBarrelBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.KEYPAD_CHEST_BLOCK_ENTITY.get(), (chest, dir) -> KeypadChestBlockEntity.getCapability((KeypadChestBlockEntity) chest, dir));
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.LASER_BLOCK_BLOCK_ENTITY.get(), LaserBlockBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.REINFORCED_HOPPER_BLOCK_ENTITY.get(), ReinforcedHopperBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.TROPHY_SYSTEM_BLOCK_ENTITY.get(), TrophySystemBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.REINFORCED_CHISELED_BOOKSHELF_BLOCK_ENTITY.get(), ReinforcedChiseledBookshelfBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.REINFORCED_DISPENSER_BLOCK_ENTITY.get(), ReinforcedDispenserBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.REINFORCED_SHELF_BLOCK_ENTITY.get(), ReinforcedShelfBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.REINFORCED_DROPPER_BLOCK_ENTITY.get(), ReinforcedDropperBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.SECURITY_CAMERA_BLOCK_ENTITY.get(), SecurityCameraBlockEntity::getCapability);
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerBlockEntity( SCContent.SECURE_TRADING_STATION_BLOCK_ENTITY.get(), SecureTradingStationBlockEntity::getCapability);
		registerSecuritySeaBoatCapabilities(SCContent.OAK_SECURITY_SEA_BOAT_ENTITY.get());
		registerSecuritySeaBoatCapabilities(SCContent.SPRUCE_SECURITY_SEA_BOAT_ENTITY.get());
		registerSecuritySeaBoatCapabilities(SCContent.BIRCH_SECURITY_SEA_BOAT_ENTITY.get());
		registerSecuritySeaBoatCapabilities(SCContent.JUNGLE_SECURITY_SEA_BOAT_ENTITY.get());
		registerSecuritySeaBoatCapabilities(SCContent.ACACIA_SECURITY_SEA_BOAT_ENTITY.get());
		registerSecuritySeaBoatCapabilities(SCContent.DARK_OAK_SECURITY_SEA_BOAT_ENTITY.get());
		registerSecuritySeaBoatCapabilities(SCContent.MANGROVE_SECURITY_SEA_BOAT_ENTITY.get());
		registerSecuritySeaBoatCapabilities(SCContent.CHERRY_SECURITY_SEA_BOAT_ENTITY.get());
		registerSecuritySeaBoatCapabilities(SCContent.PALE_OAK_SECURITY_SEA_BOAT_ENTITY.get());
		registerSecuritySeaBoatCapabilities(SCContent.BAMBOO_SECURITY_SEA_RAFT_ENTITY.get());
	}

	private static void registerSecuritySeaBoatCapabilities(EntityType<? extends AbstractSecuritySeaBoat> boatType) {
		net.geforcemods.securitycraft.platform.ItemStorageRegistration.registerEntity(boatType, AbstractSecuritySeaBoat::getCapability);
	}

	public static void onCreativeModeTabRegister(ResourceKey<CreativeModeTab> tabKey, net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput event) {

		//@formatter:off
		if (tabKey.equals(CreativeModeTabs.REDSTONE_BLOCKS)) {
			event.insertAfter(new ItemStack(Items.LEVER), List.of(new ItemStack(SCContent.REINFORCED_LEVER.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.STONE_BUTTON), List.of(new ItemStack(SCContent.REINFORCED_OAK_BUTTON.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.REINFORCED_OAK_BUTTON.get()), List.of(new ItemStack(SCContent.REINFORCED_STONE_BUTTON.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.REINFORCED_STONE_BUTTON.get()), List.of(new ItemStack(SCContent.PANIC_BUTTON.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.STONE_PRESSURE_PLATE), List.of(new ItemStack(SCContent.REINFORCED_OAK_PRESSURE_PLATE.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.REINFORCED_OAK_PRESSURE_PLATE.get()), List.of(new ItemStack(SCContent.REINFORCED_STONE_PRESSURE_PLATE.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.STICKY_PISTON), List.of(new ItemStack(SCContent.REINFORCED_PISTON.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.REINFORCED_PISTON.get()), List.of(new ItemStack(SCContent.REINFORCED_STICKY_PISTON.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.DROPPER), List.of(new ItemStack(SCContent.REINFORCED_DISPENSER.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.REINFORCED_DISPENSER.get()), List.of(new ItemStack(SCContent.REINFORCED_DROPPER.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.HOPPER), List.of(new ItemStack(SCContent.REINFORCED_HOPPER.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.LECTERN), List.of(new ItemStack(SCContent.REINFORCED_LECTERN.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
		}
		else if (tabKey.equals(CreativeModeTabs.COLORED_BLOCKS)) {
			List<DyeColor> gameplayColorOrder = List.of(
					DyeColor.WHITE,
					DyeColor.LIGHT_GRAY,
					DyeColor.GRAY,
					DyeColor.BLACK,
					DyeColor.BROWN,
					DyeColor.RED,
					DyeColor.ORANGE,
					DyeColor.YELLOW,
					DyeColor.LIME,
					DyeColor.GREEN,
					DyeColor.CYAN,
					DyeColor.LIGHT_BLUE,
					DyeColor.BLUE,
					DyeColor.PURPLE,
					DyeColor.MAGENTA,
					DyeColor.PINK
			);

			registerColoredItems(event, gameplayColorOrder, SCContent.REINFORCED_WOOL);
			registerColoredItems(event, gameplayColorOrder, SCContent.REINFORCED_CARPET);
			event.accept(SCContent.REINFORCED_TERRACOTTA);
			registerColoredItems(event, gameplayColorOrder, SCContent.REINFORCED_DYED_TERRACOTTA);
			registerColoredItems(event, gameplayColorOrder, SCContent.REINFORCED_CONCRETE);
			registerColoredItems(event, gameplayColorOrder, SCContent.REINFORCED_GLAZED_TERRACOTTA);
			event.accept(SCContent.REINFORCED_GLASS.get());
			event.accept(SCContent.REINFORCED_TINTED_GLASS.get());
			registerColoredItems(event, gameplayColorOrder, SCContent.REINFORCED_STAINED_GLASS);
			event.accept(SCContent.REINFORCED_GLASS_PANE.get());
			registerColoredItems(event, gameplayColorOrder, SCContent.REINFORCED_STAINED_GLASS_PANE);
		}
		else if (tabKey.equals(SCCreativeModeTabs.DECORATION_TAB.getKey())) {
			try {
				event.insertAfter(new ItemStack(SCContent.REINFORCED_LADDER.get()), List.of(new ItemStack(SCContent.REINFORCED_SCAFFOLDING_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
				event.insertAfter(new ItemStack(SCContent.REINFORCED_LECTERN.get()), List.of(new ItemStack(SCContent.SECRET_OAK_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			}
			catch (IllegalArgumentException e) {
				event.accept(new ItemStack(SCContent.REINFORCED_SCAFFOLDING_ITEM.get()), TabVisibility.PARENT_AND_SEARCH_TABS);
				event.accept(new ItemStack(SCContent.SECRET_OAK_SIGN_ITEM.get()), TabVisibility.PARENT_AND_SEARCH_TABS);
			}

			event.insertAfter(new ItemStack(SCContent.SECRET_OAK_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_OAK_HANGING_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_OAK_HANGING_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_SPRUCE_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_SPRUCE_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_SPRUCE_HANGING_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_SPRUCE_HANGING_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_BIRCH_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_BIRCH_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_BIRCH_HANGING_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_BIRCH_HANGING_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_JUNGLE_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_JUNGLE_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_JUNGLE_HANGING_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_JUNGLE_HANGING_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_ACACIA_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_ACACIA_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_ACACIA_HANGING_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_ACACIA_HANGING_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_DARK_OAK_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_DARK_OAK_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_DARK_OAK_HANGING_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_DARK_OAK_HANGING_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_MANGROVE_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_MANGROVE_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_MANGROVE_HANGING_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_MANGROVE_HANGING_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_CHERRY_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_CHERRY_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_CHERRY_HANGING_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_CHERRY_HANGING_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_PALE_OAK_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_PALE_OAK_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_PALE_OAK_HANGING_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_PALE_OAK_HANGING_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_BAMBOO_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_BAMBOO_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_BAMBOO_HANGING_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_BAMBOO_HANGING_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_CRIMSON_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_CRIMSON_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_CRIMSON_HANGING_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_CRIMSON_HANGING_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_WARPED_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(SCContent.SECRET_WARPED_SIGN_ITEM.get()), List.of(new ItemStack(SCContent.SECRET_WARPED_HANGING_SIGN_ITEM.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
		}
		else if (tabKey.equals(CreativeModeTabs.OP_BLOCKS)) {
			if (event.getContext().hasPermissions()) {
				event.accept(new ItemStack(SCContent.ADMIN_TOOL.get()), TabVisibility.PARENT_AND_SEARCH_TABS);
				event.accept(new ItemStack(SCContent.CODEBREAKER.get()), TabVisibility.PARENT_AND_SEARCH_TABS);
			}
		}
		else if (tabKey.equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
			event.insertAfter(new ItemStack(Items.OAK_CHEST_BOAT), List.of(new ItemStack(SCContent.OAK_SECURITY_SEA_BOAT.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.SPRUCE_CHEST_BOAT), List.of(new ItemStack(SCContent.SPRUCE_SECURITY_SEA_BOAT.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.BIRCH_CHEST_BOAT), List.of(new ItemStack(SCContent.BIRCH_SECURITY_SEA_BOAT.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.JUNGLE_CHEST_BOAT), List.of(new ItemStack(SCContent.JUNGLE_SECURITY_SEA_BOAT.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.ACACIA_CHEST_BOAT), List.of(new ItemStack(SCContent.ACACIA_SECURITY_SEA_BOAT.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.DARK_OAK_CHEST_BOAT), List.of(new ItemStack(SCContent.DARK_OAK_SECURITY_SEA_BOAT.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.MANGROVE_CHEST_BOAT), List.of(new ItemStack(SCContent.MANGROVE_SECURITY_SEA_BOAT.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.CHERRY_CHEST_BOAT), List.of(new ItemStack(SCContent.CHERRY_SECURITY_SEA_BOAT.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.PALE_OAK_CHEST_BOAT), List.of(new ItemStack(SCContent.PALE_OAK_SECURITY_SEA_BOAT.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
			event.insertAfter(new ItemStack(Items.BAMBOO_CHEST_RAFT), List.of(new ItemStack(SCContent.BAMBOO_SECURITY_SEA_RAFT.get())), TabVisibility.PARENT_AND_SEARCH_TABS);
		}
	}

	private static void registerColoredItems(CreativeModeTab.Output coloredBlocks, List<DyeColor> gameplayColorOrder, ColorCollection<? extends ItemLike> items) {
		gameplayColorOrder.forEach(color -> coloredBlocks.accept(items.pick(color)));
	}
}
