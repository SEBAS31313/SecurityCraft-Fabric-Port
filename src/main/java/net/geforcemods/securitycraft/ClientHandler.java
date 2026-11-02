package net.geforcemods.securitycraft;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.geforcemods.securitycraft.platform.ModelDataAccess;
import net.geforcemods.securitycraft.platform.PayloadContext;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderers;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.OctahedralGroup;

import it.unimi.dsi.fastutil.ints.IntList;
import net.geforcemods.securitycraft.api.IDisguisable;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasscodeProtected;
import net.geforcemods.securitycraft.blockentities.AlarmBlockEntity;
import net.geforcemods.securitycraft.blockentities.FrameBlockEntity;
import net.geforcemods.securitycraft.blockentities.InventoryScannerBlockEntity;
import net.geforcemods.securitycraft.blockentities.LaserBlockBlockEntity;
import net.geforcemods.securitycraft.blockentities.RiftStabilizerBlockEntity;
import net.geforcemods.securitycraft.blockentities.SecureRedstoneInterfaceBlockEntity;
import net.geforcemods.securitycraft.blockentities.SonicSecuritySystemBlockEntity;
import net.geforcemods.securitycraft.blockentities.UsernameLoggerBlockEntity;
import net.geforcemods.securitycraft.blocks.InventoryScannerFieldBlock;
import net.geforcemods.securitycraft.blocks.LaserFieldBlock;
import net.geforcemods.securitycraft.blocks.SecureRedstoneInterfaceBlock;
import net.geforcemods.securitycraft.components.SavedBlockState;
import net.geforcemods.securitycraft.entity.camera.SecurityCamera;
import net.geforcemods.securitycraft.items.properties.BlockLinked;
import net.geforcemods.securitycraft.items.properties.CodebreakerState;
import net.geforcemods.securitycraft.items.properties.KeycardCount;
import net.geforcemods.securitycraft.items.properties.ReinforcedTint;
import net.geforcemods.securitycraft.items.properties.SentryLinked;
import net.geforcemods.securitycraft.misc.LayerToggleHandler;
import net.geforcemods.securitycraft.misc.TintMode;
import net.geforcemods.securitycraft.models.BulletModel;
import net.geforcemods.securitycraft.models.DisguisableBlockStateModel;
import net.geforcemods.securitycraft.models.DisplayCaseModel;
import net.geforcemods.securitycraft.models.IMSBombModel;
import net.geforcemods.securitycraft.models.SecureRedstoneInterfaceBlockStateModel;
import net.geforcemods.securitycraft.models.SecureRedstoneInterfaceDishModel;
import net.geforcemods.securitycraft.models.SecurityCameraModel;
import net.geforcemods.securitycraft.models.SentryModel;
import net.geforcemods.securitycraft.models.SonicSecuritySystemModel;
import net.geforcemods.securitycraft.network.client.InteractWithFrame;
import net.geforcemods.securitycraft.network.client.OpenScreen;
import net.geforcemods.securitycraft.network.client.RefreshDisguisableModel;
import net.geforcemods.securitycraft.network.client.SendManualPages;
import net.geforcemods.securitycraft.network.client.SetCameraView;
import net.geforcemods.securitycraft.network.client.SetTrophySystemTarget;
import net.geforcemods.securitycraft.network.client.ToggleAlarmSound;
import net.geforcemods.securitycraft.network.client.UpdateLaserColors;
import net.geforcemods.securitycraft.network.client.UpdateLogger;
import net.geforcemods.securitycraft.particle.FloorTrapCloudParticle;
import net.geforcemods.securitycraft.particle.InterfaceHighlightParticle;
import net.geforcemods.securitycraft.renderers.BlockPocketManagerRenderer;
import net.geforcemods.securitycraft.renderers.BouncingBettyRenderer;
import net.geforcemods.securitycraft.renderers.BulletRenderer;
import net.geforcemods.securitycraft.renderers.ClaymoreRenderer;
import net.geforcemods.securitycraft.renderers.DisguisableBlockEntityRenderer;
import net.geforcemods.securitycraft.renderers.DisplayCaseRenderer;
import net.geforcemods.securitycraft.renderers.DisplayCaseSpecialRenderer;
import net.geforcemods.securitycraft.renderers.FrameBlockEntityRenderer;
import net.geforcemods.securitycraft.renderers.IMSBombRenderer;
import net.geforcemods.securitycraft.renderers.KeypadChestRenderer;
import net.geforcemods.securitycraft.renderers.ProjectorRenderer;
import net.geforcemods.securitycraft.renderers.ReinforcedPistonHeadRenderer;
import net.geforcemods.securitycraft.renderers.RetinalScannerRenderer;
import net.geforcemods.securitycraft.renderers.SecretHangingSignRenderer;
import net.geforcemods.securitycraft.renderers.SecretSignRenderer;
import net.geforcemods.securitycraft.renderers.SecureRedstoneInterfaceRenderer;
import net.geforcemods.securitycraft.renderers.SecureTradingStationRenderer;
import net.geforcemods.securitycraft.renderers.SecurityCameraRenderer;
import net.geforcemods.securitycraft.renderers.SecurityCameraSpecialRenderer;
import net.geforcemods.securitycraft.renderers.SecuritySeaBoatRenderer;
import net.geforcemods.securitycraft.renderers.SecuritySeaRaftRenderer;
import net.geforcemods.securitycraft.renderers.SentryRenderer;
import net.geforcemods.securitycraft.renderers.SonicSecuritySystemRenderer;
import net.geforcemods.securitycraft.renderers.TrophySystemRenderer;
import net.geforcemods.securitycraft.screen.AlarmScreen;
import net.geforcemods.securitycraft.screen.BlockChangeDetectorScreen;
import net.geforcemods.securitycraft.screen.BlockPocketManagerScreen;
import net.geforcemods.securitycraft.screen.BlockReinforcerScreen;
import net.geforcemods.securitycraft.screen.BriefcasePasscodeScreen;
import net.geforcemods.securitycraft.screen.CameraMonitorScreen;
import net.geforcemods.securitycraft.screen.CheckPasscodeScreen;
import net.geforcemods.securitycraft.screen.CustomizeBlockScreen;
import net.geforcemods.securitycraft.screen.DisguiseModuleScreen;
import net.geforcemods.securitycraft.screen.EditModuleScreen;
import net.geforcemods.securitycraft.screen.FrameScreen;
import net.geforcemods.securitycraft.screen.InventoryScannerScreen;
import net.geforcemods.securitycraft.screen.ItemInventoryScreen;
import net.geforcemods.securitycraft.screen.KeyChangerScreen;
import net.geforcemods.securitycraft.screen.KeycardReaderScreen;
import net.geforcemods.securitycraft.screen.KeypadBlastFurnaceScreen;
import net.geforcemods.securitycraft.screen.KeypadFurnaceScreen;
import net.geforcemods.securitycraft.screen.KeypadSmokerScreen;
import net.geforcemods.securitycraft.screen.LaserBlockScreen;
import net.geforcemods.securitycraft.screen.MineRemoteAccessToolScreen;
import net.geforcemods.securitycraft.screen.ProjectorScreen;
import net.geforcemods.securitycraft.screen.ReinforcedLecternScreen;
import net.geforcemods.securitycraft.screen.RiftStabilizerScreen;
import net.geforcemods.securitycraft.screen.SCManualScreen;
import net.geforcemods.securitycraft.screen.SSSItemScreen;
import net.geforcemods.securitycraft.screen.SecureRedstoneInterfaceScreen;
import net.geforcemods.securitycraft.screen.SecureTradingStationScreen;
import net.geforcemods.securitycraft.screen.SentryRemoteAccessToolScreen;
import net.geforcemods.securitycraft.screen.SetPasscodeScreen;
import net.geforcemods.securitycraft.screen.SingleLensScreen;
import net.geforcemods.securitycraft.screen.SonicSecuritySystemScreen;
import net.geforcemods.securitycraft.screen.TrophySystemScreen;
import net.geforcemods.securitycraft.screen.UsernameLoggerScreen;
import net.geforcemods.securitycraft.screen.components.GuiBlockModelRenderState;
import net.geforcemods.securitycraft.screen.components.GuiBlockModelRenderer;
import net.geforcemods.securitycraft.util.BlockEntityRenderDelegate;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.Reinforced;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.client.renderer.blockentity.ShelfRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class ClientHandler {
	public static final ModelLayerLocation BULLET_LOCATION = new ModelLayerLocation(SecurityCraft.resLoc("bullet"), "main");
	public static final ModelLayerLocation IMS_BOMB_LOCATION = new ModelLayerLocation(SecurityCraft.resLoc("ims_bomb"), "main");
	public static final ModelLayerLocation DISPLAY_CASE_LOCATION = new ModelLayerLocation(SecurityCraft.resLoc("display_case"), "main");
	public static final ModelLayerLocation SENTRY_LOCATION = new ModelLayerLocation(SecurityCraft.resLoc("sentry"), "main");
	public static final ModelLayerLocation SECURE_REDSTONE_INTERFACE_DISH_LAYER_LOCATION = new ModelLayerLocation(SecurityCraft.resLoc("secure_redstone_interface_dish"), "main");
	public static final ModelLayerLocation SECURITY_CAMERA_LOCATION = new ModelLayerLocation(SecurityCraft.resLoc("security_camera"), "main");
	public static final ModelLayerLocation SONIC_SECURITY_SYSTEM_LOCATION = new ModelLayerLocation(SecurityCraft.resLoc("sonic_security_system"), "main");
	public static final BlockEntityRenderDelegate DISGUISED_BLOCK_RENDER_DELEGATE = new BlockEntityRenderDelegate();
	public static final BlockEntityRenderDelegate PROJECTOR_RENDER_DELEGATE = new BlockEntityRenderDelegate();
	public static final Identifier CAMERA_LAYER = SecurityCraft.resLoc("camera_overlay");
	private static Map<Block, Integer> blocksWithReinforcedTint = new HashMap<>();
	private static Map<Block, Integer> blocksWithCustomTint = new HashMap<>();
	//@formatter:off
	private static Supplier<Block[]> disguisableBlocks = Suppliers.memoize(() -> new Block[] {
			SCContent.BLOCK_CHANGE_DETECTOR.get(),
			SCContent.CAGE_TRAP.get(),
			SCContent.FLOOR_TRAP.get(),
			SCContent.INVENTORY_SCANNER.get(),
			SCContent.KEYCARD_READER.get(),
			SCContent.KEYPAD.get(),
			SCContent.KEYPAD_BARREL.get(),
			SCContent.KEYPAD_BLAST_FURNACE.get(),
			SCContent.KEYPAD_CHEST.get(),
			SCContent.KEYPAD_DOOR.get(),
			SCContent.KEYPAD_FURNACE.get(),
			SCContent.KEYPAD_SMOKER.get(),
			SCContent.KEYPAD_TRAPDOOR.get(),
			SCContent.LASER_BLOCK.get(),
			SCContent.PROJECTOR.get(),
			SCContent.PROTECTO.get(),
			SCContent.REINFORCED_DISPENSER.get(),
			SCContent.REINFORCED_DROPPER.get(),
			SCContent.REINFORCED_HOPPER.get(),
			SCContent.REINFORCED_OBSERVER.get(),
			SCContent.RETINAL_SCANNER.get(),
			SCContent.RIFT_STABILIZER.get(),
			SCContent.SCANNER_DOOR.get(),
			SCContent.SCANNER_TRAPDOOR.get(),
			SCContent.SECURE_TRADING_STATION.get(),
			SCContent.SECURITY_CAMERA.get(),
			//Excluded because it has its own custom block state model
			//SCContent.SECURE_REDSTONE_INTERFACE.get(),
			SCContent.SENTRY_DISGUISE.get(),
			SCContent.SONIC_SECURITY_SYSTEM.get(),
			SCContent.TROPHY_SYSTEM.get(),
			SCContent.USERNAME_LOGGER.get()
	});
	//@formatter:on
	private static final Identifier SRI_BASE_MODEL_LOCATION = SecurityCraft.resLoc("block/secure_redstone_interface");
	private static final Identifier SRI_SENDER_ON_MODEL_LOCATION = SRI_BASE_MODEL_LOCATION.withSuffix("_sender_on");
	private static final Identifier SRI_RECEIVER_ON_MODEL_LOCATION = SRI_BASE_MODEL_LOCATION.withSuffix("_receiver_on");
	private static final Map<SRIKey, ExtraModelKey<BlockStateModel>> SRI_MODEL_KEYS = Util.make(new HashMap<>(), map -> {
		Arrays.stream(Direction.values()).forEach(direction -> {
			map.put(new SRIKey(true, direction), ExtraModelKey.create(() -> SRI_SENDER_ON_MODEL_LOCATION + "$" + direction.getName()));
			map.put(new SRIKey(false, direction), ExtraModelKey.create(() -> SRI_RECEIVER_ON_MODEL_LOCATION + "$" + direction.getName()));
		});
	});
	//@formatter:off
    public static final RenderType OVERLAY_LINES = RenderType.create(
			"overlay_lines",
			RenderSetup.builder(RenderPipelines.LINES)
					.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
					.setOutputTarget(OutputTarget.OUTLINE_TARGET)
					.createRenderSetup());
	//@formatter:on

	private ClientHandler() {}

	public static void registerModels() {
		ModelLoadingPlugin.register(plugin -> {
			SRI_MODEL_KEYS.forEach((key, modelKey) -> plugin.addModel(modelKey, SimpleUnbakedExtraModel.blockStateModel(key.location(), key.modelRotation())));
			Set<Block> disguisable = Set.of(disguisableBlocks.get());
			plugin.modifyBlockModelAfterBake().register((model, context) -> {
				BlockState state = context.state();
				if (state.is(SCContent.SECURE_REDSTONE_INTERFACE.get())) {
					SRIKey key = new SRIKey(state.getValue(SecureRedstoneInterfaceBlock.SENDER), state.getValue(SecureRedstoneInterfaceBlock.FACING));
					return new SecureRedstoneInterfaceBlockStateModel(() -> Minecraft.getInstance().getModelManager().getModel(SRI_MODEL_KEYS.get(key)), model);
				}
				return disguisable.contains(state.getBlock()) ? new DisguisableBlockStateModel(model) : model;
			});
		});
	}

	public static void registerFluidModels() {
		FluidRenderingRegistry.register(SCContent.FAKE_WATER.get(), SCContent.FLOWING_FAKE_WATER.get(), FluidStateModelSet.WATER_MODEL);
		FluidRenderingRegistry.register(SCContent.FAKE_LAVA.get(), SCContent.FLOWING_FAKE_LAVA.get(), FluidStateModelSet.LAVA_MODEL);
		net.minecraft.core.registries.BuiltInRegistries.BLOCK.forEach(block -> {
			if (block instanceof net.geforcemods.securitycraft.blocks.reinforced.ReinforcedGlassBlock)
				FluidRenderingRegistry.setBlockTransparency(block, true);
		});
	}

	public static void registerRangeProperties() {
		RangeSelectItemModelProperties.ID_MAPPER.put(SecurityCraft.resLoc("keycard_count"), KeycardCount.MAP_CODEC);
	}

	public static void registerSelectProperties() {
		SelectItemModelProperties.ID_MAPPER.put(SecurityCraft.resLoc("block_linked"), BlockLinked.TYPE);
		SelectItemModelProperties.ID_MAPPER.put(SecurityCraft.resLoc("codebreaker_state"), CodebreakerState.TYPE);
		SelectItemModelProperties.ID_MAPPER.put(SecurityCraft.resLoc("sentry_linked"), SentryLinked.TYPE);
	}

	public static void registerSpecialRenderers() {
		SpecialModelRenderers.ID_MAPPER.put(SecurityCraft.resLoc("display_case"), DisplayCaseSpecialRenderer.Unbaked.MAP_CODEC);
		SpecialModelRenderers.ID_MAPPER.put(SecurityCraft.resLoc("security_camera"), SecurityCameraSpecialRenderer.Unbaked.MAP_CODEC);
	}

	public static void registerItemTints() {
		ItemTintSources.ID_MAPPER.put(SecurityCraft.resLoc("reinforced"), ReinforcedTint.MAP_CODEC);
	}

	public static void registerMenuScreens() {
		MenuScreens.register(SCContent.BLOCK_REINFORCER_MENU.get(), BlockReinforcerScreen::new);
		MenuScreens.register(SCContent.BRIEFCASE_INVENTORY_MENU.get(), ItemInventoryScreen.Briefcase::new);
		MenuScreens.register(SCContent.CUSTOMIZE_BLOCK_MENU.get(), CustomizeBlockScreen::new);
		MenuScreens.register(SCContent.CUSTOMIZE_ENTITY_MENU.get(), CustomizeBlockScreen::new);
		MenuScreens.register(SCContent.DISGUISE_MODULE_MENU.get(), DisguiseModuleScreen::new);
		MenuScreens.register(SCContent.INVENTORY_SCANNER_MENU.get(), InventoryScannerScreen::new);
		MenuScreens.register(SCContent.KEYPAD_FURNACE_MENU.get(), KeypadFurnaceScreen::new);
		MenuScreens.register(SCContent.KEYPAD_SMOKER_MENU.get(), KeypadSmokerScreen::new);
		MenuScreens.register(SCContent.KEYPAD_BLAST_FURNACE_MENU.get(), KeypadBlastFurnaceScreen::new);
		MenuScreens.register(SCContent.KEYCARD_READER_MENU.get(), KeycardReaderScreen::new);
		MenuScreens.register(SCContent.BLOCK_POCKET_MANAGER_MENU.get(), BlockPocketManagerScreen::new);
		MenuScreens.register(SCContent.PROJECTOR_MENU.get(), ProjectorScreen::new);
		MenuScreens.register(SCContent.BLOCK_CHANGE_DETECTOR_MENU.get(), BlockChangeDetectorScreen::new);
		MenuScreens.register(SCContent.KEYCARD_HOLDER_MENU.get(), ItemInventoryScreen.KeycardHolder::new);
		MenuScreens.register(SCContent.TROPHY_SYSTEM_MENU.get(), TrophySystemScreen::new);
		MenuScreens.register(SCContent.SINGLE_LENS_MENU.get(), SingleLensScreen::new);
		MenuScreens.register(SCContent.LASER_BLOCK_MENU.get(), LaserBlockScreen::new);
		MenuScreens.register(SCContent.REINFORCED_LECTERN_MENU.get(), ReinforcedLecternScreen::new);
		MenuScreens.register(SCContent.SECURE_TRADING_STATION_MENU.get(), SecureTradingStationScreen::new);
	}

	public static void registerGuiLayers() {
		HudElementRegistry.addLast(CAMERA_LAYER, (graphics, delta) -> {
			if (!LayerToggleHandler.isDisabled(CAMERA_LAYER))
				SCClientEventHandler.cameraOverlay(graphics, delta);
		});
		LayerToggleHandler.disable(CAMERA_LAYER);
		PictureInPictureRendererRegistry.register(context -> new GuiBlockModelRenderer());
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static <T extends BlockEntity, S extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> void registerBlockEntityRenderer(net.minecraft.world.level.block.entity.BlockEntityType<?> type, net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<T, S> provider) {
		BlockEntityRendererRegistry.register((net.minecraft.world.level.block.entity.BlockEntityType) type, (net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider) provider);
	}

	public static void registerEntityRenderers() {
		EntityRendererRegistry.register(SCContent.BOUNCING_BETTY_ENTITY.get(), BouncingBettyRenderer::new);
		EntityRendererRegistry.register(SCContent.IMS_BOMB_ENTITY.get(), IMSBombRenderer::new);
		EntityRendererRegistry.register(SCContent.SECURITY_CAMERA_ENTITY.get(), NoopRenderer::new);
		EntityRendererRegistry.register(SCContent.SENTRY_ENTITY.get(), SentryRenderer::new);
		EntityRendererRegistry.register(SCContent.BULLET_ENTITY.get(), BulletRenderer::new);
		EntityRendererRegistry.register(SCContent.OAK_SECURITY_SEA_BOAT_ENTITY.get(), ctx -> new SecuritySeaBoatRenderer(ctx, ModelLayers.OAK_CHEST_BOAT));
		EntityRendererRegistry.register(SCContent.SPRUCE_SECURITY_SEA_BOAT_ENTITY.get(), ctx -> new SecuritySeaBoatRenderer(ctx, ModelLayers.SPRUCE_CHEST_BOAT));
		EntityRendererRegistry.register(SCContent.BIRCH_SECURITY_SEA_BOAT_ENTITY.get(), ctx -> new SecuritySeaBoatRenderer(ctx, ModelLayers.BIRCH_CHEST_BOAT));
		EntityRendererRegistry.register(SCContent.JUNGLE_SECURITY_SEA_BOAT_ENTITY.get(), ctx -> new SecuritySeaBoatRenderer(ctx, ModelLayers.JUNGLE_CHEST_BOAT));
		EntityRendererRegistry.register(SCContent.ACACIA_SECURITY_SEA_BOAT_ENTITY.get(), ctx -> new SecuritySeaBoatRenderer(ctx, ModelLayers.ACACIA_CHEST_BOAT));
		EntityRendererRegistry.register(SCContent.DARK_OAK_SECURITY_SEA_BOAT_ENTITY.get(), ctx -> new SecuritySeaBoatRenderer(ctx, ModelLayers.DARK_OAK_CHEST_BOAT));
		EntityRendererRegistry.register(SCContent.MANGROVE_SECURITY_SEA_BOAT_ENTITY.get(), ctx -> new SecuritySeaBoatRenderer(ctx, ModelLayers.MANGROVE_CHEST_BOAT));
		EntityRendererRegistry.register(SCContent.CHERRY_SECURITY_SEA_BOAT_ENTITY.get(), ctx -> new SecuritySeaBoatRenderer(ctx, ModelLayers.CHERRY_CHEST_BOAT));
		EntityRendererRegistry.register(SCContent.PALE_OAK_SECURITY_SEA_BOAT_ENTITY.get(), ctx -> new SecuritySeaBoatRenderer(ctx, ModelLayers.PALE_OAK_CHEST_BOAT));
		EntityRendererRegistry.register(SCContent.BAMBOO_SECURITY_SEA_RAFT_ENTITY.get(), ctx -> new SecuritySeaRaftRenderer(ctx, ModelLayers.BAMBOO_CHEST_RAFT));
		//normal renderers
		registerBlockEntityRenderer(SCContent.BLOCK_POCKET_MANAGER_BLOCK_ENTITY.get(), BlockPocketManagerRenderer::new);
		registerBlockEntityRenderer(SCContent.CLAYMORE_BLOCK_ENTITY.get(), ClaymoreRenderer::new);
		registerBlockEntityRenderer(SCContent.KEYPAD_CHEST_BLOCK_ENTITY.get(), KeypadChestRenderer::new);
		registerBlockEntityRenderer(SCContent.DISPLAY_CASE_BLOCK_ENTITY.get(), ctx -> new DisplayCaseRenderer(ctx, false));
		registerBlockEntityRenderer(SCContent.GLOW_DISPLAY_CASE_BLOCK_ENTITY.get(), ctx -> new DisplayCaseRenderer(ctx, true));
		registerBlockEntityRenderer(SCContent.FRAME_BLOCK_ENTITY.get(), FrameBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.REINFORCED_LECTERN_BLOCK_ENTITY.get(), LecternRenderer::new);
		registerBlockEntityRenderer(SCContent.PROJECTOR_BLOCK_ENTITY.get(), ProjectorRenderer::new);
		registerBlockEntityRenderer(SCContent.REINFORCED_PISTON_BLOCK_ENTITY.get(), ReinforcedPistonHeadRenderer::new);
		registerBlockEntityRenderer(SCContent.REINFORCED_SHELF_BLOCK_ENTITY.get(), ShelfRenderer::new);
		registerBlockEntityRenderer(SCContent.RETINAL_SCANNER_BLOCK_ENTITY.get(), RetinalScannerRenderer::new);
		registerBlockEntityRenderer(SCContent.SECURE_REDSTONE_INTERFACE_BLOCK_ENTITY.get(), SecureRedstoneInterfaceRenderer::new);
		registerBlockEntityRenderer(SCContent.SECURE_TRADING_STATION_BLOCK_ENTITY.get(), SecureTradingStationRenderer::new);
		registerBlockEntityRenderer(SCContent.SECURITY_CAMERA_BLOCK_ENTITY.get(), SecurityCameraRenderer::new);
		registerBlockEntityRenderer(SCContent.SECRET_HANGING_SIGN_BLOCK_ENTITY.get(), SecretHangingSignRenderer::new);
		registerBlockEntityRenderer(SCContent.SECRET_SIGN_BLOCK_ENTITY.get(), SecretSignRenderer::new);
		registerBlockEntityRenderer(SCContent.SONIC_SECURITY_SYSTEM_BLOCK_ENTITY.get(), SonicSecuritySystemRenderer::new);
		registerBlockEntityRenderer(SCContent.TROPHY_SYSTEM_BLOCK_ENTITY.get(), TrophySystemRenderer::new);
		//disguisable block entity renderers
		registerBlockEntityRenderer(SCContent.BLOCK_CHANGE_DETECTOR_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.CAGE_TRAP_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.DISGUISABLE_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.FLOOR_TRAP_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.INVENTORY_SCANNER_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.KEYCARD_READER_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.KEYPAD_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.KEYPAD_BARREL_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.KEYPAD_BLAST_FURNACE_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.KEYPAD_DOOR_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.KEYPAD_TRAPDOOR_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.KEYPAD_FURNACE_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.KEYPAD_SMOKER_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.LASER_BLOCK_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.OBSERVER_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.PROTECTO_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.REINFORCED_DISPENSER_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.REINFORCED_DROPPER_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.REINFORCED_HOPPER_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.RIFT_STABILIZER_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.SCANNER_DOOR_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.SCANNER_TRAPDOOR_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
		registerBlockEntityRenderer(SCContent.USERNAME_LOGGER_BLOCK_ENTITY.get(), DisguisableBlockEntityRenderer::new);
	}

	public static void registerLayerDefinitions() {
		ModelLayerRegistry.registerModelLayer(BULLET_LOCATION, BulletModel::createLayer);
		ModelLayerRegistry.registerModelLayer(IMS_BOMB_LOCATION, IMSBombModel::createLayer);
		ModelLayerRegistry.registerModelLayer(DISPLAY_CASE_LOCATION, DisplayCaseModel::createModelLayer);
		ModelLayerRegistry.registerModelLayer(SENTRY_LOCATION, SentryModel::createLayer);
		ModelLayerRegistry.registerModelLayer(SECURITY_CAMERA_LOCATION, SecurityCameraModel::createLayer);
		ModelLayerRegistry.registerModelLayer(SONIC_SECURITY_SYSTEM_LOCATION, SonicSecuritySystemModel::createLayer);
		ModelLayerRegistry.registerModelLayer(SECURE_REDSTONE_INTERFACE_DISH_LAYER_LOCATION, SecureRedstoneInterfaceDishModel::createLayer);
	}

	public static void registerParticleProviders() {
		ParticleProviderRegistry.getInstance().register(SCContent.FLOOR_TRAP_CLOUD.get(), FloorTrapCloudParticle.Provider::new);
		ParticleProviderRegistry.getInstance().register(SCContent.INTERFACE_HIGHLIGHT.get(), InterfaceHighlightParticle.Provider::new);
	}

	public static void registerRenderPipelines() {
		RenderPipelines.register(FrameBlockEntityRenderer.FRAME_PIPELINE);
	}


	public static void registerClientPayloadHandlers() {
		ClientPlayNetworking.registerGlobalReceiver(InteractWithFrame.TYPE, (payload, context) -> payload.handle(new PayloadContext(context.player(), context.client()::execute)));
		ClientPlayNetworking.registerGlobalReceiver(OpenScreen.TYPE, (payload, context) -> payload.handle(new PayloadContext(context.player(), context.client()::execute)));
		ClientPlayNetworking.registerGlobalReceiver(RefreshDisguisableModel.TYPE, (payload, context) -> payload.handle(new PayloadContext(context.player(), context.client()::execute)));
		ClientPlayNetworking.registerGlobalReceiver(SendManualPages.TYPE, (payload, context) -> payload.handle(new PayloadContext(context.player(), context.client()::execute)));
		ClientPlayNetworking.registerGlobalReceiver(SetCameraView.TYPE, (payload, context) -> payload.handle(new PayloadContext(context.player(), context.client()::execute)));
		ClientPlayNetworking.registerGlobalReceiver(SetTrophySystemTarget.TYPE, (payload, context) -> payload.handle(new PayloadContext(context.player(), context.client()::execute)));
		ClientPlayNetworking.registerGlobalReceiver(ToggleAlarmSound.TYPE, (payload, context) -> payload.handle(new PayloadContext(context.player(), context.client()::execute)));
		ClientPlayNetworking.registerGlobalReceiver(UpdateLaserColors.TYPE, (payload, context) -> payload.handle(new PayloadContext(context.player(), context.client()::execute)));
		ClientPlayNetworking.registerGlobalReceiver(UpdateLogger.TYPE, (payload, context) -> payload.handle(new PayloadContext(context.player(), context.client()::execute)));
	}

	public static void registerFeatureRenderers() {
		FeatureRendererRegistry.register(FrameBlockEntityRenderer.FeatureRenderer.TYPE, FrameBlockEntityRenderer.FeatureRenderer::new);
	}

	private static void initTint() {
		for (Field field : SCContent.class.getFields()) {
			if (field.isAnnotationPresent(Reinforced.class)) {
				try {
					Reinforced reinforced = field.getAnnotation(Reinforced.class);
					List<Block> blocks = SCContent.annotatedConstantToStream(field.get(null)).map(e -> (Block) e.get()).toList();

					for (Block block : blocks) {
						int customTint = reinforced.customTint();

						if (reinforced.hasReinforcedTint())
							blocksWithReinforcedTint.put(block, customTint);
						else if (customTint != 0xFFFFFFFF)
							blocksWithCustomTint.put(block, customTint);
					}
				}
				catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		blocksWithReinforcedTint.put(SCContent.BLOCK_POCKET_MANAGER.get(), SCContent.CRYSTAL_QUARTZ_TINT);
		blocksWithReinforcedTint.put(SCContent.BLOCK_POCKET_WALL.get(), SCContent.CRYSTAL_QUARTZ_TINT);
		blocksWithCustomTint.put(SCContent.CRYSTAL_QUARTZ_SLAB.get(), SCContent.CRYSTAL_QUARTZ_TINT);
		blocksWithCustomTint.put(SCContent.SMOOTH_CRYSTAL_QUARTZ.get(), SCContent.CRYSTAL_QUARTZ_TINT);
		blocksWithCustomTint.put(SCContent.CHISELED_CRYSTAL_QUARTZ.get(), SCContent.CRYSTAL_QUARTZ_TINT);
		blocksWithCustomTint.put(SCContent.CRYSTAL_QUARTZ_BLOCK.get(), SCContent.CRYSTAL_QUARTZ_TINT);
		blocksWithCustomTint.put(SCContent.CRYSTAL_QUARTZ_BRICKS.get(), SCContent.CRYSTAL_QUARTZ_TINT);
		blocksWithCustomTint.put(SCContent.CRYSTAL_QUARTZ_PILLAR.get(), SCContent.CRYSTAL_QUARTZ_TINT);
		blocksWithCustomTint.put(SCContent.CRYSTAL_QUARTZ_STAIRS.get(), SCContent.CRYSTAL_QUARTZ_TINT);
		blocksWithCustomTint.put(SCContent.SMOOTH_CRYSTAL_QUARTZ_SLAB.get(), SCContent.CRYSTAL_QUARTZ_TINT);
		blocksWithCustomTint.put(SCContent.SMOOTH_CRYSTAL_QUARTZ_STAIRS.get(), SCContent.CRYSTAL_QUARTZ_TINT);
	}

	public static void registerBlockTints() {
		initTint();
		final Map<Integer, List<BlockTintSource>> mixCache = new HashMap<>();
		blocksWithReinforcedTint.forEach((block, tint) -> BlockColorRegistry.register(mixCache.computeIfAbsent(tint, t -> List.of(mixedReinforcedTintSource(t))), block));
		blocksWithCustomTint.forEach((block, tint) -> BlockColorRegistry.register(List.of(BlockTintSources.constant(tint)), block));

		BlockColorRegistry.register(List.of(reinforcedTintSource(), mixTintSourceWithReinforcedTint(BlockTintSources.grassBlock())), SCContent.REINFORCED_GRASS_BLOCK.get());
		BlockColorRegistry.register(List.of(reinforcedTintSource(), BlockTintSources.water()), SCContent.REINFORCED_WATER_CAULDRON.get());
		BlockColorRegistry.register(List.of(laserFieldTintSource()), SCContent.LASER_FIELD.get());
		BlockColorRegistry.register(List.of(inventoryScannerFieldTintSource()), SCContent.INVENTORY_SCANNER_FIELD.get());
	}

	public static int iterateFields(BlockAndTintGetter level, BlockPos pos, Direction direction, int range, Block block, Predicate<BlockEntity> beTest, Function<BlockEntity, ItemStack> lensGetter) {
		try {
			return iterateFieldsInternal(level, pos, direction, range, block, beTest, lensGetter);
		}
		catch (Exception e1) {
			direction = direction.getOpposite();

			try {
				return iterateFieldsInternal(level, pos, direction, range, block, beTest, lensGetter);
			}
			catch (Exception e2) {}
		}

		return -1;
	}

	private static int iterateFieldsInternal(BlockAndTintGetter level, BlockPos pos, Direction direction, int range, Block block, Predicate<BlockEntity> beTest, Function<BlockEntity, ItemStack> lensGetter) throws ArrayIndexOutOfBoundsException {
		MutableBlockPos mutablePos = new MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());

		for (int i = 0; i < range; i++) {
			if (level.getBlockState(mutablePos).is(block)) {
				BlockEntity be = level.getBlockEntity(mutablePos);

				if (beTest.test(be)) {
					ItemStack stack = lensGetter.apply(be);

					if (stack.has(DataComponents.DYED_COLOR))
						return ARGB.opaque(stack.get(DataComponents.DYED_COLOR).rgb());

					break;
				}
			}

			mutablePos.move(direction);
		}

		return -1;
	}

	private static BlockTintSource reinforcedTintSource() {
		return mixedReinforcedTintSource(0xFFFFFFFF);
	}

	private static BlockTintSource mixedReinforcedTintSource(int tint) {
		return new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return mixWithReinforcedTintIfEnabled(tint, null);
			}

			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
				return mixWithReinforcedTintIfEnabled(tint, level.getBlockEntity(pos) instanceof IOwnable ownable ? ownable : null);
			}
		};
	}

	public static BlockTintSource mixTintSourceWithReinforcedTint(BlockTintSource parent) {
		return new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return mixWithReinforcedTintIfEnabled(parent.color(state), null);
			}

			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
				return mixWithReinforcedTintIfEnabled(parent.colorInWorld(state, level, pos), level.getBlockEntity(pos) instanceof IOwnable ownable ? ownable : null);
			}

			@Override
			public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
				return mixWithReinforcedTintIfEnabled(parent.colorAsTerrainParticle(state, level, pos), level.getBlockEntity(pos) instanceof IOwnable ownable ? ownable : null);
			}

			@Override
			public Set<Property<?>> relevantProperties() {
				return parent.relevantProperties();
			}
		};
	}

	public static BlockTintSource laserFieldTintSource() {
		return new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return -1;
			}

			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
				Direction direction = LaserFieldBlock.getFieldDirection(state);

				return iterateFields(level, pos, direction, ConfigHandler.SERVER.laserBlockRange.get(), SCContent.LASER_BLOCK.get(), LaserBlockBlockEntity.class::isInstance, be -> ((LaserBlockBlockEntity) be).getLensContainer().getItem(direction.getOpposite().ordinal()));
			}
		};
	}

	public static BlockTintSource inventoryScannerFieldTintSource() {
		return new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return -1;
			}

			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
				Direction direction = state.getValue(InventoryScannerFieldBlock.FACING);

				return iterateFields(level, pos, direction, ConfigHandler.SERVER.inventoryScannerRange.get(), SCContent.INVENTORY_SCANNER.get(), InventoryScannerBlockEntity.class::isInstance, be -> ((InventoryScannerBlockEntity) be).getLensContainer().getItem(0));
			}
		};
	}

	public static int mixWithReinforcedTintIfEnabled(int tint, IOwnable ownable) {
		if (ownable == null)
			return ARGB.multiply(tint, 0xFF000000 | TintMode.color());

		return TintMode.tint(Minecraft.getInstance().player, tint, ownable);
	}

	public static Player getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	public static Level getClientLevel() {
		return Minecraft.getInstance().level;
	}

	public static void displayMRATScreen(ItemStack stack) {
		Minecraft.getInstance().gui.setScreen(new MineRemoteAccessToolScreen(stack));
	}

	public static void displaySRATScreen(ItemStack stack) {
		Minecraft.getInstance().gui.setScreen(new SentryRemoteAccessToolScreen(stack));
	}

	public static void displayEditModuleScreen(ItemStack stack) {
		Minecraft.getInstance().gui.setScreen(new EditModuleScreen(stack));
	}

	public static void displayCameraMonitorScreen(ItemStack stack) {
		Minecraft.getInstance().gui.setScreen(new CameraMonitorScreen(stack));
	}

	public static void displayFrameScreen(FrameBlockEntity be, boolean readOnly) {
		Minecraft.getInstance().gui.setScreen(new FrameScreen(readOnly, be));
	}

	public static void displaySCManualScreen() {
		Minecraft.getInstance().gui.setScreen(new SCManualScreen());
	}

	public static void displaySonicSecuritySystemScreen(SonicSecuritySystemBlockEntity be) {
		Minecraft.getInstance().gui.setScreen(new SonicSecuritySystemScreen(be));
	}

	public static void displayBriefcasePasscodeScreen(Component title) {
		Minecraft.getInstance().gui.setScreen(new BriefcasePasscodeScreen(title, false));
	}

	public static void displayBriefcaseSetupScreen(Component title) {
		Minecraft.getInstance().gui.setScreen(new BriefcasePasscodeScreen(title, true));
	}

	public static void displayUsernameLoggerScreen(UsernameLoggerBlockEntity be) {
		Minecraft.getInstance().gui.setScreen(new UsernameLoggerScreen(be));
	}

	public static void displayUniversalKeyChangerScreen(BlockEntity be) {
		Minecraft.getInstance().gui.setScreen(new KeyChangerScreen((IPasscodeProtected) be));
	}

	public static void displayUniversalKeyChangerScreen(Entity entity) {
		Minecraft.getInstance().gui.setScreen(new KeyChangerScreen((IPasscodeProtected) entity));
	}

	public static void displayCheckPasscodeScreen(BlockEntity be) {
		Component displayName = be instanceof Nameable nameable ? nameable.getDisplayName() : Component.translatable(be.getBlockState().getBlock().getDescriptionId());

		Minecraft.getInstance().gui.setScreen(new CheckPasscodeScreen((IPasscodeProtected) be, displayName));
	}

	public static void displayCheckPasscodeScreen(Entity entity) {
		Minecraft.getInstance().gui.setScreen(new CheckPasscodeScreen((IPasscodeProtected) entity, entity.getDisplayName()));
	}

	public static void displaySetPasscodeScreen(BlockEntity be) {
		Component displayName = be instanceof Nameable nameable ? nameable.getDisplayName() : Component.translatable(be.getBlockState().getBlock().getDescriptionId());

		Minecraft.getInstance().gui.setScreen(new SetPasscodeScreen((IPasscodeProtected) be, displayName));
	}

	public static void displaySetPasscodeScreen(Entity entity) {
		Minecraft.getInstance().gui.setScreen(new SetPasscodeScreen((IPasscodeProtected) entity, entity.getDisplayName()));
	}

	public static void displaySSSItemScreen(ItemStack stack) {
		Minecraft.getInstance().gui.setScreen(new SSSItemScreen(stack));
	}

	public static void displayRiftStabilizerScreen(RiftStabilizerBlockEntity be) {
		Minecraft.getInstance().gui.setScreen(new RiftStabilizerScreen(be));
	}

	public static void displaySecureRedstoneInterfaceScreen(SecureRedstoneInterfaceBlockEntity be) {
		Minecraft.getInstance().gui.setScreen(new SecureRedstoneInterfaceScreen(be));
	}

	public static void displayAlarmScreen(AlarmBlockEntity be) {
		Minecraft.getInstance().gui.setScreen(new AlarmScreen(be, be.getSound().location()));
	}

	public static void refreshModelData(BlockEntity be) {
		BlockPos pos = be.getBlockPos();

		ClientUtils.recompileChunk(pos); //Recompiles the render chunk at the changed position
	}

	public static boolean isPlayerMountedOnCamera() {
		return Minecraft.getInstance().getCameraEntity() instanceof SecurityCamera;
	}

	public static void putDisguisedBeRenderer(BlockEntity disguisableBlockEntity, ItemStack stack) {
		DISGUISED_BLOCK_RENDER_DELEGATE.putDelegateFor(disguisableBlockEntity, stack.getOrDefault(SCContent.SAVED_BLOCK_STATE.get(), SavedBlockState.EMPTY).state(), stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyOne());
	}

	public static void updateBlockColorAroundPosition(BlockPos pos) {
		Minecraft.getInstance().levelExtractor.blockChanged(pos, 0);
	}

	private record SRIKey(boolean sender, Direction direction) {
		public Identifier location() {
			return sender ? SRI_SENDER_ON_MODEL_LOCATION : SRI_RECEIVER_ON_MODEL_LOCATION;
		}

		public BlockModelRotation modelRotation() {
			return BlockModelRotation.get(switch (direction) {
				case DOWN -> OctahedralGroup.BLOCK_ROT_X_180;
				case UP -> OctahedralGroup.IDENTITY;
				case NORTH -> OctahedralGroup.BLOCK_ROT_X_90;
				case SOUTH -> OctahedralGroup.BLOCK_ROT_X_90.compose(OctahedralGroup.BLOCK_ROT_Y_180);
				case WEST -> OctahedralGroup.BLOCK_ROT_X_90.compose(OctahedralGroup.BLOCK_ROT_Y_270);
				case EAST -> OctahedralGroup.BLOCK_ROT_X_90.compose(OctahedralGroup.BLOCK_ROT_Y_90);
			});
		}
	}
}
