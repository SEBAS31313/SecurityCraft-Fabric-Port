package net.geforcemods.securitycraft.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.entity.camera.CameraController;
import net.geforcemods.securitycraft.entity.camera.SecurityCamera;
import net.minecraft.client.KeyMapping;

/**
 * Custom {@link KeyMapping}s that SecurityCraft uses.
 *
 * @author Geforce
 */
public class KeyBindings {
	private static final List<TickingKeyMapping> TICKING_KEY_MAPPINGS = new ArrayList<>();
	private static KeyMapping.Category category;
	private static KeyMapping cameraZoomIn;
	private static KeyMapping cameraZoomOut;
	private static KeyMapping cameraEmitRedstone;
	private static KeyMapping cameraActivateNightVision;
	private static KeyMapping setDefaultViewingDirection;

	private KeyBindings() {}

	public static void registerKeyMappings() {
		category = KeyMapping.Category.register(SecurityCraft.resLoc(SecurityCraft.MODID));
		cameraZoomIn = register("cameraZoomIn", GLFW.GLFW_KEY_EQUAL);
		cameraZoomOut = register("cameraZoomOut", GLFW.GLFW_KEY_MINUS);
		cameraEmitRedstone = registerTicking("cameraEmitRedstone", GLFW.GLFW_KEY_R, CameraController::toggleRedstone);
		cameraActivateNightVision = registerTicking("cameraActivateNightVision", GLFW.GLFW_KEY_N, CameraController::toggleNightVision);
		setDefaultViewingDirection = registerTicking("setDefaultViewingDirection", GLFW.GLFW_KEY_U, CameraController::setDefaultViewingDirection);
	}

	private static KeyMapping register(String name, int defaultKey) {
		KeyMapping keyMapping = new SCKeyMapping(name, defaultKey);

		net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(keyMapping);
		return keyMapping;
	}

	private static KeyMapping registerTicking(String name, int defaultKey, Consumer<SecurityCamera> action) {
		TickingKeyMapping keyMapping = new TickingKeyMapping(name, defaultKey, action);

		net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(keyMapping);
		TICKING_KEY_MAPPINGS.add(keyMapping);
		return keyMapping;
	}

	public static void tick(SecurityCamera cam) {
		TICKING_KEY_MAPPINGS.forEach(keyMapping -> keyMapping.tick(cam));
	}

	public static KeyMapping cameraZoomIn() {
		return cameraZoomIn;
	}

	public static KeyMapping cameraZoomOut() {
		return cameraZoomOut;
	}

	public static KeyMapping cameraEmitRedstone() {
		return cameraEmitRedstone;
	}

	public static KeyMapping cameraActivateNightVision() {
		return cameraActivateNightVision;
	}

	public static KeyMapping setDefaultViewingDirection() {
		return setDefaultViewingDirection;
	}

	private static class SCKeyMapping extends KeyMapping {
		public SCKeyMapping(String name, int defaultKey) {
			super("key.securitycraft." + name, defaultKey, category);
		}
	}

	private static class TickingKeyMapping extends SCKeyMapping {
		private static final int MAX_COOLDOWN = 30;
		private int cooldown = MAX_COOLDOWN;
		private Consumer<SecurityCamera> action;

		public TickingKeyMapping(String name, int defaultKey, Consumer<SecurityCamera> action) {
			super(name, defaultKey);
			this.action = action;
		}

		public void tick(SecurityCamera t) {
			cooldown--;

			if (consumeClick() && cooldown <= 0) {
				action.accept(t);
				cooldown = MAX_COOLDOWN;
			}
		}
	}
}
