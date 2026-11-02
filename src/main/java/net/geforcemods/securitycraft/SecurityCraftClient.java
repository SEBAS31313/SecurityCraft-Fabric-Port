package net.geforcemods.securitycraft;

import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.geforcemods.securitycraft.entity.camera.CameraController;
import net.geforcemods.securitycraft.entity.camera.FrameFeedHandler;
import net.geforcemods.securitycraft.items.properties.HitCheck;
import net.geforcemods.securitycraft.misc.KeyBindings;
import net.geforcemods.securitycraft.misc.LayerToggleHandler;
import net.geforcemods.securitycraft.misc.Tips;
import net.geforcemods.securitycraft.platform.Packets;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

/** Fabric client entrypoint; no client registration is loaded by the dedicated server. */
public class SecurityCraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ConfigHandler.registerClient();
		ConfigScreenFactoryRegistry.INSTANCE.register(SecurityCraft.MODID, ConfigurationScreen::new);
		Packets.installClientSender(ClientPlayNetworking::send);
		HitCheck.bootstrap();
		KeyBindings.registerKeyMappings();
		ClientHandler.registerModels();
		ClientHandler.registerFluidModels();
		ClientHandler.registerRangeProperties();
		ClientHandler.registerSelectProperties();
		ClientHandler.registerSpecialRenderers();
		ClientHandler.registerItemTints();
		ClientHandler.registerMenuScreens();
		ClientHandler.registerGuiLayers();
		ClientHandler.registerLayerDefinitions();
		ClientHandler.registerEntityRenderers();
		ClientHandler.registerParticleProviders();
		ClientHandler.registerRenderPipelines();
		ClientHandler.registerFeatureRenderers();
		ClientHandler.registerBlockTints();
		ClientHandler.registerClientPayloadHandlers();
		LayerToggleHandler.register();
		SCClientEventHandler.register();
		ClientTickEvents.START_CLIENT_TICK.register(client -> CameraController.onClientTickPre());
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			CameraController.onClientTickPost();
			FrameFeedHandler.onClientTickPost();
		});
		LevelExtractionEvents.END_EXTRACTION.register(context -> FrameFeedHandler.onExtractLevelRenderState(context.camera().getCullFrustum()));
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> Tips.onLoggingIn());
	}
}
