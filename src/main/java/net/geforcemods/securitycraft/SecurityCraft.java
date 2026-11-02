package net.geforcemods.securitycraft;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;

import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.api.SecurityCraftAPI;
import net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity;
import net.geforcemods.securitycraft.blocks.AbstractKeypadFurnaceBlock;
import net.geforcemods.securitycraft.blocks.InventoryScannerBlock;
import net.geforcemods.securitycraft.blocks.KeypadBarrelBlock;
import net.geforcemods.securitycraft.blocks.KeypadBlock;
import net.geforcemods.securitycraft.blocks.KeypadChestBlock;
import net.geforcemods.securitycraft.blocks.KeypadTrapDoorBlock;
import net.geforcemods.securitycraft.blocks.SecureRedstoneInterfaceBlock;
import net.geforcemods.securitycraft.blocks.mines.IMSBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedHopperBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedPressurePlateBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedRedstoneBlock;
import net.geforcemods.securitycraft.commands.SCCommand;
import net.geforcemods.securitycraft.compat.distanthorizons.DistantHorizonsCompat;
import net.geforcemods.securitycraft.items.SCManualItem;
import net.geforcemods.securitycraft.misc.BlockEntityTracker;
import net.geforcemods.securitycraft.misc.CommonDoorActivator;
import net.geforcemods.securitycraft.misc.ConfigAttackTargetCheck;
import net.geforcemods.securitycraft.misc.PageGroup;
import net.geforcemods.securitycraft.misc.SCManualPage;
import net.geforcemods.securitycraft.util.HasManualPage;
import net.geforcemods.securitycraft.util.Reinforced;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SecurityCraft implements net.fabricmc.api.ModInitializer {
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MODID = "securitycraft";
	public static final Random RANDOM = new Random();
    public static final net.geforcemods.securitycraft.platform.CameraTicketController CAMERA_TICKET_CONTROLLER = new net.geforcemods.securitycraft.platform.CameraTicketController();

    @Override public void onInitialize() {
        net.geforcemods.securitycraft.platform.PlatformLifecycle.register();
        ConfigHandler.registerServer();
        SCContent.DATA_COMPONENTS.register();
        SCContent.DATA_SERIALIZERS.register();
        SCContent.FLUIDS.register();
        SCContent.BLOCKS.register();
        SCContent.ENTITY_TYPES.register();
        SCContent.BLOCK_ENTITY_TYPES.register();
        SCContent.ITEMS.register();
        RegistrationHandler.registerBlockItemsAndSounds();
        SCContent.COMMAND_ARGUMENT_TYPES.register();
        SCContent.GAME_RULES.register();
        SCContent.LOOT_ITEM_CONDITION_TYPES.register();
        SCContent.MENU_TYPES.register();
        SCContent.PARTICLE_TYPES.register();
        SCContent.RECIPE_SERIALIZERS.register();
        SCCreativeModeTabs.CREATIVE_MODE_TABS.register();
        RegistrationHandler.register();
        net.geforcemods.securitycraft.misc.ReinforcedCauldronInteractions.register();
        registerDefaultApiHandlers();
        net.geforcemods.securitycraft.platform.NetworkRegistration.registerPayloads();
        net.geforcemods.securitycraft.platform.NetworkRegistration.registerServerReceivers();
        net.geforcemods.securitycraft.platform.CommonEventHooks.register();
        net.geforcemods.securitycraft.platform.FirstUseItem.register();
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> SCCommand.register(dispatcher));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> collectSCContentData(server, true));
        net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(net.minecraft.server.packs.PackType.SERVER_DATA).registerReloadListener(resLoc("collect_sc_content_data"),
            (state, prepare, barrier, apply) -> CompletableFuture.completedFuture(null).thenCompose(barrier::wait).thenRunAsync(() -> collectSCContentData(true), apply));
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("distanthorizons")) DistantHorizonsCompat.registerEvent();
    }

    private static void registerDefaultApiHandlers() {
        SecurityCraftAPI.registerExtractionBlock(new ReinforcedHopperBlock.ExtractionBlock());
        SecurityCraftAPI.registerExtractionBlock(new IMSBlock.ExtractionBlock());
        SecurityCraftAPI.registerPasscodeConvertible(new KeypadBlock.Convertible());
        SecurityCraftAPI.registerPasscodeConvertible(new KeypadBarrelBlock.Convertible());
        SecurityCraftAPI.registerPasscodeConvertible(new KeypadChestBlock.Convertible());
        SecurityCraftAPI.registerPasscodeConvertible(new KeypadTrapDoorBlock.Convertible());
        SecurityCraftAPI.registerPasscodeConvertible(new AbstractKeypadFurnaceBlock.Convertible(Blocks.FURNACE, SCContent.KEYPAD_FURNACE.get()));
        SecurityCraftAPI.registerPasscodeConvertible(new AbstractKeypadFurnaceBlock.Convertible(Blocks.SMOKER, SCContent.KEYPAD_SMOKER.get()));
        SecurityCraftAPI.registerPasscodeConvertible(new AbstractKeypadFurnaceBlock.Convertible(Blocks.BLAST_FURNACE, SCContent.KEYPAD_BLAST_FURNACE.get()));
        SecurityCraftAPI.registerSentryAttackTargetCheck(new ConfigAttackTargetCheck());
        SecurityCraftAPI.registerDoorActivator(new CommonDoorActivator());
        SecurityCraftAPI.registerDoorActivator(new InventoryScannerBlock.DoorActivator());
        SecurityCraftAPI.registerDoorActivator(new ReinforcedPressurePlateBlock.DoorActivator());
        SecurityCraftAPI.registerDoorActivator(new ReinforcedRedstoneBlock.DoorActivator());
        SecurityCraftAPI.registerDoorActivator(new SecureRedstoneInterfaceBlock.DoorActivator());
        SecurityCraftAPI.finishRegistration();
    }

	public static void collectSCContentData(boolean addPages) {
		collectSCContentData(net.geforcemods.securitycraft.platform.PlatformLifecycle.getServer(), addPages);
	}

	public static void collectSCContentData(MinecraftServer server, boolean addPages) {
		if (addPages && server == null)
			return;

		Map<PageGroup, List<ItemStackTemplate>> groupStacks = new EnumMap<>(PageGroup.class);

		IReinforcedBlock.VANILLA_TO_SECURITYCRAFT.clear();
		IReinforcedBlock.SECURITYCRAFT_TO_VANILLA.clear();

		if (addPages)
			SCManualItem.PAGES.clear();

		for (Field field : SCContent.class.getFields()) {
			try {
				if (field.isAnnotationPresent(Reinforced.class)) {
					List<Block> blocks = SCContent.annotatedConstantToStream(field.get(null)).map(e -> (Block) e.get()).toList();

					for (Block block : blocks) {
						IReinforcedBlock rb = (IReinforcedBlock) block;

						IReinforcedBlock.VANILLA_TO_SECURITYCRAFT.put(rb.getVanillaBlock(), block);
						IReinforcedBlock.SECURITYCRAFT_TO_VANILLA.put(block, rb.getVanillaBlock());
					}
				}

				if (field.isAnnotationPresent(HasManualPage.class)) {
					List<Item> items = SCContent.annotatedConstantToStream(field.get(null)).map(e -> ((ItemLike) e.get()).asItem()).toList();
					HasManualPage hmp = field.getAnnotation(HasManualPage.class);

					for (Item item : items) {
						PageGroup group = hmp.value();
						boolean wasNotAdded = false;
						Component title = Component.empty();
						String key = "help.";

						if (group != PageGroup.NONE) {
							if (!groupStacks.containsKey(group)) {
								groupStacks.put(group, new ArrayList<>());
								title = Utils.localize(group.getTitle());
								key += group.getSpecialInfoKey();
								wasNotAdded = true;
							}

							if (item != Items.AIR)
								groupStacks.get(group).add(new ItemStackTemplate(item));
						}
						else {
							title = Utils.localize(item.getDescriptionId());
							key += item.getDescriptionId().substring(5) + ".info";
						}

						if (addPages && (group == PageGroup.NONE || wasNotAdded))
							SCManualItem.PAGES.add(new SCManualPage(item, group, title, Component.translatable(key.replace("..", ".")), hmp.designedBy(), hmp.hasRecipeDescription(), Suppliers.memoize(() -> SCManualItem.findRecipes(server, item, group))));
					}
				}
			}
			catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		groupStacks.forEach(PageGroup::setItems);
	}

    public static String getVersion() {
        return "v" + net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(MODID).orElseThrow().getMetadata().getVersion().getFriendlyString();
    }

	public static Identifier resLoc(String path) {
		return Identifier.fromNamespaceAndPath(MODID, path);
	}

	public static Identifier mcResLoc(String path) {
		return Identifier.withDefaultNamespace(path);
	}
}
