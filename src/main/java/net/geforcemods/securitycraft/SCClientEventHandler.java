package net.geforcemods.securitycraft;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.geforcemods.securitycraft.misc.LayerToggleHandler;
import net.minecraft.server.packs.PackType;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

import net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity;
import net.geforcemods.securitycraft.blockentities.BlockChangeDetectorBlockEntity.ChangeEntry;
import net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity;
import net.geforcemods.securitycraft.blocks.SecurityCameraBlock;
import net.geforcemods.securitycraft.entity.camera.CameraClientChunkCacheExtension;
import net.geforcemods.securitycraft.entity.camera.CameraViewAreaExtension;
import net.geforcemods.securitycraft.entity.camera.FrameFeedHandler;
import net.geforcemods.securitycraft.misc.BlockEntityTracker;
import net.geforcemods.securitycraft.misc.CameraRedstoneModuleState;
import net.geforcemods.securitycraft.misc.KeyBindings;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.misc.StillValid;
import net.geforcemods.securitycraft.renderers.FrameBlockEntityRenderer;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public class SCClientEventHandler {
	public static final Identifier BACKGROUND_SPRITE = SecurityCraft.resLoc("hud/camera/background");
	public static final Identifier LIVE_SPRITE = SecurityCraft.resLoc("hud/camera/live");
	public static final Identifier NIGHT_VISION_INACTIVE_SPRITE = SecurityCraft.resLoc("hud/camera/night_vision_inactive");
	public static final Identifier REDSTONE_MODULE_NOT_PRESENT_SPRITE = SecurityCraft.resLoc("hud/camera/redstone_module_not_present");
	public static final Identifier REDSTONE_MODULE_PRESENT_SPRITE = SecurityCraft.resLoc("hud/camera/redstone_module_present");
	public static final Identifier NIGHT_VISION = SecurityCraft.mcResLoc("textures/mob_effect/night_vision.png");
	public static final ItemStackTemplate REDSTONE = new ItemStackTemplate(Items.REDSTONE);
	private static final Component REDSTONE_NOTE = Utils.localize("gui.securitycraft:camera.toggleRedstoneNote");
	private static final Component SMART_MODULE_NOTE = Utils.localize("gui.securitycraft:camera.smartModuleNote");
	//@formatter:off
	private static final CameraKeyInfoEntry[] CAMERA_KEY_INFO_LIST = {
			new CameraKeyInfoEntry(() -> true, options -> Utils.localize("gui.securitycraft:camera.lookAround", options.keyUp.getTranslatedKeyMessage(), options.keyLeft.getTranslatedKeyMessage(), options.keyDown.getTranslatedKeyMessage(), options.keyRight.getTranslatedKeyMessage()), _ -> true),
			new CameraKeyInfoEntry(() -> true, options -> Utils.localize("gui.securitycraft:camera.exit", options.keyShift.getTranslatedKeyMessage()), _ -> true),
			new CameraKeyInfoEntry(() -> true, _ -> Utils.localize("gui.securitycraft:camera.zoom", KeyBindings.cameraZoomIn().getTranslatedKeyMessage(), KeyBindings.cameraZoomOut().getTranslatedKeyMessage()), _-> true),
			new CameraKeyInfoEntry(ConfigHandler.SERVER.allowCameraNightVision::get, _ -> Utils.localize("gui.securitycraft:camera.activateNightVision", KeyBindings.cameraActivateNightVision().getTranslatedKeyMessage()), _ -> true),
			new CameraKeyInfoEntry(() -> true, _ -> Utils.localize("gui.securitycraft:camera.toggleRedstone", KeyBindings.cameraEmitRedstone().getTranslatedKeyMessage()), be -> be.isModuleEnabled(ModuleType.REDSTONE)),
			new CameraKeyInfoEntry(() -> true, _ -> REDSTONE_NOTE, be -> be.isModuleEnabled(ModuleType.REDSTONE)),
			new CameraKeyInfoEntry(() -> true, _ -> Utils.localize("gui.securitycraft:camera.setDefaultViewingDirection", KeyBindings.setDefaultViewingDirection().getTranslatedKeyMessage()), be -> be.isModuleEnabled(ModuleType.SMART)),
			new CameraKeyInfoEntry(() -> true, _ -> SMART_MODULE_NOTE, be -> be.isModuleEnabled(ModuleType.SMART))
	};
	//@formatter:on
	private static final List<net.geforcemods.securitycraft.platform.registry.DeferredHolder<DataComponentType<?>, ? extends DataComponentType<? extends TooltipProvider>>> COMPONENTS_WITH_GLOBAL_TOOLTIP = List.of(SCContent.OWNER_DATA, SCContent.NOTES);
	private static float cameraInfoMessageTime;

	private SCClientEventHandler() {}

	public static void register() {
		Identifier invalidator = SecurityCraft.resLoc("frame_noise_texture_invalidator");
		ResourceLoader resources = ResourceLoader.get(PackType.CLIENT_RESOURCES);
		resources.registerReloadListener(invalidator, (state, background, barrier, apply) -> barrier.wait(null).thenRunAsync(FrameBlockEntityRenderer::invalidateNoiseBackground, apply));
		resources.addListenerOrdering(ResourceReloaderKeys.AFTER_VANILLA, invalidator);
		ClientTickEvents.END_CLIENT_TICK.register(SCClientEventHandler::onClientTickPost);
		LevelRenderEvents.COLLECT_SUBMITS.register(SCClientEventHandler::onRenderLevelStage);
		ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> CameraViewAreaExtension.onChunkUnload(chunk.getPos().x(), chunk.getPos().z()));
		ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> clearCameraState());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clearCameraState());
		ItemTooltipCallback.EVENT.register(SCClientEventHandler::onItemTooltip);
	}

	private static void clearCameraState() {
		FrameFeedHandler.removeAllFeeds();
		CameraClientChunkCacheExtension.clear();
		CameraViewAreaExtension.clear();
		LayerToggleHandler.reset();
	}

	public static void onClientTickPost(Minecraft mc) {
		if (cameraInfoMessageTime >= 0)
			cameraInfoMessageTime--;

		if (mc != null && mc.gui.screen() instanceof StillValid screen && !screen.stillValid(mc.player))
			mc.gui.setScreen(null);
	}

	public static void onRenderLevelStage(LevelRenderContext event) {
		Vec3 camPos = event.levelState().cameraRenderState.pos;
		PoseStack pose = event.poseStack();
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;

		if (level == null || mc.player == null)
			return;

		for (BlockPos bcdPos : BlockEntityTracker.BLOCK_CHANGE_DETECTOR.getTrackedBlockEntities(level)) {
			BlockEntity be = level.getBlockEntity(bcdPos);

			if (be instanceof BlockChangeDetectorBlockEntity bcd && bcd.isShowingHighlights() && bcd.isOwnedBy(mc.player)) {
				int packedColor = bcd.getColor();

				for (ChangeEntry changeEntry : bcd.getFilteredEntries()) {
					BlockPos pos = changeEntry.pos();

					pose.pushPose();
					pose.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z); //TODO is there a better way to get the SubmitNodeStorage?
					event.submitNodeCollector().submitShapeOutline(pose, Shapes.block(), ClientHandler.OVERLAY_LINES, packedColor, 1.0F, true);
					pose.popPose();
				}
			}
		}
	}

	public static void onItemTooltip(ItemStack stack, TooltipContext ctx, TooltipFlag flag, List<Component> tooltip) {
		TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
		int nextIndex = 0;

		for (int i = 0; i < COMPONENTS_WITH_GLOBAL_TOOLTIP.size(); i++) {
			DataComponentType<? extends TooltipProvider> type = COMPONENTS_WITH_GLOBAL_TOOLTIP.get(i).get();
			TooltipProvider tooltipProvider = stack.get(type);

			if (tooltipProvider != null && display.shows(type)) {
				final int index = nextIndex++ + 1; //add in order of global tooltip components list, after item name

				tooltipProvider.addToTooltip(ctx, line -> {
					if (index >= tooltip.size())
						tooltip.add(line);
					else
						tooltip.add(index, line);
				}, flag, stack.getComponents());
			}
		}
	}

	public static void cameraOverlay(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();

		if (mc.gui.hud.isHidden())
			return;

		Level level = mc.level;
		BlockPos pos = mc.getCameraEntity().blockPosition();

		if (!(level.getBlockEntity(pos) instanceof SecurityCameraBlockEntity be))
			return;

		Window window = mc.getWindow();
		int scaledWidth = window.getGuiScaledWidth();
		int scaledHeight = window.getGuiScaledHeight();
		Font font = Minecraft.getInstance().font;
		Options options = Minecraft.getInstance().options;
		BlockState state = level.getBlockState(pos);
		long dayTime = Minecraft.getInstance().level.getOverworldClockTime();
		int hours24 = (int) ((float) dayTime / 1000L + 6L) % 24;
		int hours = hours24 % 12;
		int minutes = (int) (dayTime / 16.666666F % 60.0F);
		String time = String.format("%02d:%02d %s", Integer.valueOf(hours < 1 ? 12 : hours), Integer.valueOf(minutes), hours24 < 12 ? "AM" : "PM");
		int timeY = 25;

		if (be.hasCustomName()) {
			Component cameraName = be.getCustomName();

			guiGraphics.text(font, cameraName, scaledWidth - font.width(cameraName) - 8, 25, CommonColors.WHITE, true);
			timeY += 10;
		}

		guiGraphics.text(font, time, scaledWidth - font.width(time) - 4, timeY, CommonColors.WHITE, true);

		if (cameraInfoMessageTime >= 0) {
			float fadeOutPartialTick = Math.max(cameraInfoMessageTime + 1.0F - deltaTracker.getGameTimeDeltaPartialTick(false), 1.0F);
			int alpha = (int) Math.ceil(255.0F * Math.min(20.0F, fadeOutPartialTick) / 20.0F);
			int heightOffset = 10;

			for (int i = CAMERA_KEY_INFO_LIST.length - 1; i >= 0; i--) {
				CameraKeyInfoEntry entry = CAMERA_KEY_INFO_LIST[i];

				if (entry.enabled().get()) {
					entry.drawString(options, guiGraphics, font, scaledWidth, scaledHeight, heightOffset, be, alpha);
					heightOffset += 10;
				}
			}
		}

		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 5, 0, 90, 20);
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIVE_SPRITE, window.getGuiScaledWidth() - 70, 5, 65, 16);

		if (!mc.player.hasEffect(MobEffects.NIGHT_VISION))
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, NIGHT_VISION_INACTIVE_SPRITE, 28, 4, 16, 9);
		else
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, NIGHT_VISION, 27, -1, 0, 0, 18, 18, 18, 18);

		if (state.getSignal(level, pos, state.getValue(SecurityCameraBlock.FACING)) == 0) {
			if (!be.isModuleEnabled(ModuleType.REDSTONE))
				CameraRedstoneModuleState.NOT_INSTALLED.extractRenderState(guiGraphics, 12, 2);
			else
				CameraRedstoneModuleState.DEACTIVATED.extractRenderState(guiGraphics, 12, 2);
		}
		else
			CameraRedstoneModuleState.ACTIVATED.extractRenderState(guiGraphics, 12, 2);
	}

	public static void resetCameraInfoMessageTime() {
		cameraInfoMessageTime = 200;
	}

	public record CameraKeyInfoEntry(Supplier<Boolean> enabled, Function<Options, Component> text, Predicate<SecurityCameraBlockEntity> whiteText) {
		public void drawString(Options options, GuiGraphicsExtractor guiGraphics, Font font, int scaledWidth, int scaledHeight, int heightOffset, SecurityCameraBlockEntity be, int alpha) {
			Component text = text().apply(options);
			int textColor = whiteText().test(be) ? 0xFFFFFF : 0xFF3377;

			guiGraphics.text(font, text, scaledWidth - font.width(text) - 8, scaledHeight - heightOffset, textColor + (alpha << 24), true);
		}
	}
}
