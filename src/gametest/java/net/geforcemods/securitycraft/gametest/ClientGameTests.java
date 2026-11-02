package net.geforcemods.securitycraft.gametest;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.geforcemods.securitycraft.ClientHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.blockentities.FrameBlockEntity;
import net.geforcemods.securitycraft.blockentities.KeypadBlockEntity;
import net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity;
import net.geforcemods.securitycraft.blocks.KeypadBlock;
import net.geforcemods.securitycraft.components.NamedPositions;
import net.geforcemods.securitycraft.entity.camera.FrameFeedHandler;
import net.geforcemods.securitycraft.entity.camera.SecurityCamera;
import net.geforcemods.securitycraft.entity.sentry.Sentry;
import net.geforcemods.securitycraft.items.CameraMonitorItem;
import net.geforcemods.securitycraft.items.TaserItem;
import net.geforcemods.securitycraft.network.server.CheckPasscode;
import net.geforcemods.securitycraft.network.server.DismountCamera;
import net.geforcemods.securitycraft.network.server.MountCamera;
import net.geforcemods.securitycraft.network.server.SetPasscode;
import net.geforcemods.securitycraft.platform.Packets;
import net.geforcemods.securitycraft.screen.CameraMonitorScreen;
import net.geforcemods.securitycraft.screen.CheckPasscodeScreen;
import net.geforcemods.securitycraft.screen.SCManualScreen;
import net.geforcemods.securitycraft.screen.SetPasscodeScreen;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Native game tests: actual network payloads, block entity sync, camera rendering and screen extraction. */
public final class ClientGameTests implements FabricClientGameTest {
    private static final BlockPos CAMERA = new BlockPos(0, 82, 0);
    private static final BlockPos KEYPAD = new BlockPos(-2, 82, 0);
    private static final BlockPos FRAME = new BlockPos(2, 82, 0);
    private static final BlockPos DISPLAY = new BlockPos(-4, 81, 0);

    @Override
    public void runTest(ClientGameTestContext context) {
        String shaderSource = System.getProperty("sc.compat.shader");
        if (shaderSource != null) context.runOnClient(client -> {
            try {
            var source = java.nio.file.Path.of(shaderSource);
            var target = net.fabricmc.loader.api.FabricLoader.getInstance().getGameDir().resolve("shaderpacks").resolve(source.getFileName());
            java.nio.file.Files.createDirectories(target.getParent());
            java.nio.file.Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Class<?> iris = Class.forName("net.irisshaders.iris.Iris");
            Object config = iris.getMethod("getIrisConfig").invoke(null);
            config.getClass().getMethod("setShaderPackName", String.class).invoke(config, source.getFileName().toString());
            config.getClass().getMethod("setShadersEnabled", boolean.class).invoke(config, true);
            config.getClass().getMethod("save").invoke(config);
            iris.getMethod("reload").invoke(null);
            } catch (Exception error) { throw new RuntimeException("Could not activate compatibility shader pack", error); }
        });
        Properties properties = new Properties();
        properties.setProperty("server-ip", "127.0.0.1");
        properties.setProperty("server-port", Integer.toString(availableLoopbackPort()));
        try (TestDedicatedServerContext server = context.worldBuilder().createServer(properties)) {
            TestServerConnection connection = server.connect();
            prepareFixture(server, connection);
            context.waitTick();
            context.waitTicks(5);
            context.waitFor(client -> client.level != null && client.gui.screen() == null);
            context.waitTicks(20);
            context.waitFor(client -> client.level.getBlockEntity(CAMERA) instanceof SecurityCameraBlockEntity
                    && client.level.getBlockEntity(KEYPAD) instanceof KeypadBlockEntity);

            assertModelsPresent(context);
            context.takeScreenshot("full-content-cameras-sentry-display");
            testPasscodeNetwork(context, server, connection);
            testCameraNetwork(context, server, connection);
            testFrameFeed(context, server, connection);
            testScreens(context, server, connection);
            testTaserRendering(context, server, connection);
            if (shaderSource != null) context.runOnClient(client -> {
                try {
                    Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                    Object instance = api.getMethod("getInstance").invoke(null);
                    check((Boolean)api.getMethod("isShaderPackInUse").invoke(instance), "Solas must still be active after cameras, frame feeds and UI rendering");
                    Class<?> iris = Class.forName("net.irisshaders.iris.Iris");
                    Object manager = iris.getMethod("getPipelineManager").invoke(null);
                    Object pipeline = ((java.util.Optional<?>)manager.getClass().getMethod("getPipeline").invoke(manager)).orElse(null);
                    check(pipeline != null && pipeline.getClass().getSimpleName().equals("IrisRenderingPipeline"), "Shader pack selection alone is insufficient: Iris must not have fallen back to the vanilla pipeline");
                } catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
            });
        }
    }

    private static int availableLoopbackPort() {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0, 0, java.net.InetAddress.getLoopbackAddress())) {
            return socket.getLocalPort();
        } catch (java.io.IOException exception) {
            throw new java.io.UncheckedIOException(exception);
        }
    }

    private static void prepareFixture(TestDedicatedServerContext server, TestServerConnection connection) {
        server.runOnServer(mcServer -> {
            var level = mcServer.overworld();
            var player = mcServer.getPlayerList().getPlayers().get(0);
            player.setGameMode(GameType.CREATIVE);
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++)
                    level.setBlockAndUpdate(new BlockPos(x, 80, z), Blocks.STONE.defaultBlockState());
            }
            for (BlockPos pos : new BlockPos[] { CAMERA, KEYPAD, FRAME })
                level.setBlockAndUpdate(pos.south(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(CAMERA, SCContent.SECURITY_CAMERA.get().defaultBlockState());
            level.setBlockAndUpdate(KEYPAD, SCContent.KEYPAD.get().defaultBlockState());
            level.setBlockAndUpdate(FRAME, SCContent.FRAME.get().defaultBlockState());
            level.setBlockAndUpdate(DISPLAY, SCContent.DISPLAY_CASE.get().defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(4, 81, 0), SCContent.SECURE_REDSTONE_INTERFACE.get().defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(-5, 81, 0), SCContent.REINFORCED_GLASS.get().defaultBlockState());
            for (BlockPos pos : new BlockPos[] { CAMERA, KEYPAD, FRAME, DISPLAY }) {
                ((IOwnable) level.getBlockEntity(pos)).setOwner(player.getUUID().toString(), player.getGameProfile().name());
                level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            }
            var camera = (SecurityCameraBlockEntity) level.getBlockEntity(CAMERA);
            camera.tick(level, CAMERA, camera.getBlockState());
            ItemStack monitor = new ItemStack(SCContent.CAMERA_MONITOR.get());
            CameraMonitorItem.DEFAULT_NAMED_POSITIONS.add(SCContent.BOUND_CAMERAS, monitor,
                    GlobalPos.of(level.dimension(), CAMERA), Optional.of("Fabric test camera"));
            player.setItemInHand(InteractionHand.MAIN_HAND, monitor);
            ((FrameBlockEntity) level.getBlockEntity(FRAME)).applyCameraPositions(monitor);
            Sentry sentry = SCContent.SENTRY_ENTITY.get().create(level, EntitySpawnReason.COMMAND);
            check(sentry != null, "Sentry entity type must instantiate");
            sentry.setOwner(player.getUUID().toString(), player.getGameProfile().name());
            sentry.setPos(3.5D, 81.0D, 1.5D);
            sentry.toggleMode(player, Sentry.SentryMode.IDLE.ordinal(), false);
            level.addFreshEntity(sentry);
            player.connection.teleport(0.5D, 81.0D, -5.5D, 0.0F, 10.0F);
            player.inventoryMenu.broadcastChanges();
        });
    }

    private static void assertModelsPresent(ClientGameTestContext context) {
        context.runOnClient(client -> {
            var models = client.getModelManager().getBlockStateModelSet();
            int count = 0;
            for (var block : BuiltInRegistries.BLOCK) {
                if (!BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("securitycraft"))
                    continue;
                count++;
                for (var state : block.getStateDefinition().getPossibleStates()) {
                    if (state.getRenderShape() == net.minecraft.world.level.block.RenderShape.INVISIBLE) continue;
                    check(models.get(state) != models.missingModel(), "Missing model for " + state);
                }
            }
            check(count > 400, "The full original block roster must be registered");
        });
    }

    private static void testPasscodeNetwork(ClientGameTestContext context, TestDedicatedServerContext server,
            TestServerConnection connection) {
        context.runOnClient(client -> {
            var keypad = (KeypadBlockEntity) client.level.getBlockEntity(KEYPAD);
            check(keypad.isOwnedBy(client.player), "Placing owner must synchronize to the actual client");
            Packets.sendToServer(new SetPasscode(KEYPAD, "2468"));
        });
        context.waitTicks(5);
        waitForServer(context, server, mcServer -> ((KeypadBlockEntity) mcServer.overworld().getBlockEntity(KEYPAD)).getPasscode() != null, 400);
        byte[] serverHash = server.computeOnServer(mcServer -> ((KeypadBlockEntity) mcServer.overworld().getBlockEntity(KEYPAD)).getPasscode().clone());
        context.waitTicks(5);
        context.runOnClient(client -> {
            var keypad = (KeypadBlockEntity) client.level.getBlockEntity(KEYPAD);
            check(!Arrays.equals(serverHash, keypad.getPasscode()), "The server's passcode hash must not be sent to clients");
            Packets.sendToServer(new CheckPasscode(KEYPAD, "2468"));
        });
        context.waitTicks(5);
        waitForServer(context, server, mcServer -> mcServer.overworld().getBlockState(KEYPAD).getValue(KeypadBlock.POWERED), 400);
    }

    private static void testCameraNetwork(ClientGameTestContext context, TestDedicatedServerContext server,
            TestServerConnection connection) {
        Vec3 bodyPosition = server.computeOnServer(mcServer -> mcServer.getPlayerList().getPlayers().get(0).position());
        context.runOnClient(client -> {
            client.gui.setScreen(null);
            Packets.sendToServer(new MountCamera(CAMERA));
        });
        context.waitTicks(5);
        context.waitTicks(5);
        context.waitFor(client -> client.getCameraEntity() instanceof SecurityCamera);
        check(server.computeOnServer(mcServer -> mcServer.getPlayerList().getPlayers().get(0).position().equals(bodyPosition)),
                "Viewing a camera must leave the player's body in place");
        context.waitTicks(10);
        context.takeScreenshot("mounted-camera-overlay");
        context.runOnClient(client -> Packets.sendToServer(new DismountCamera()));
        context.waitTicks(5);
        context.waitTicks(5);
        context.waitFor(client -> client.getCameraEntity() == client.player);
    }

    private static void testFrameFeed(ClientGameTestContext context, TestDedicatedServerContext server,
            TestServerConnection connection) {
        context.runOnClient(client -> {
            var frame = (FrameBlockEntity) client.level.getBlockEntity(FRAME);
            check(!frame.getCameraPositions().isEmpty(), "Camera monitor links must synchronize into the frame");
            frame.setCameraOnClientAndUpdate(GlobalPos.of(client.level.dimension(), CAMERA));
        });
        context.waitTicks(5);
        context.waitTicks(5);
        context.waitFor(client -> FrameFeedHandler.hasFeeds());
        context.waitTicks(20);
        context.takeScreenshot("active-frame-camera-feed");
        server.runOnServer(mcServer -> ((FrameBlockEntity) mcServer.overworld().getBlockEntity(FRAME)).unsetCurrentCameraOnServer());
        context.waitTick();
        context.waitTicks(5);
    }

    private static void testScreens(ClientGameTestContext context, TestDedicatedServerContext server,
            TestServerConnection connection) {
        captureScreen(context, () -> new CameraMonitorScreen(new ItemStack(SCContent.CAMERA_MONITOR.get())), "camera-monitor-screen");
        Screen passcode = context.computeOnClient(client -> new SetPasscodeScreen(
                (KeypadBlockEntity) client.level.getBlockEntity(KEYPAD), Component.literal("Fabric keypad")));
        showAndAssertScreen(context, passcode);
        context.getInput().typeChars("1357");
        context.waitTicks(2);
        context.runOnClient(client -> check(client.gui.screen() == passcode, "Passcode screen must stay open while entering a code"));
        context.takeScreenshot("keypad-passcode-screen");
        context.clickScreenButton(context.computeOnClient(client -> Component.translatable("gui.securitycraft:passcode.save").getString()));
        context.waitForScreen(CheckPasscodeScreen.class);
        context.setScreen(() -> null);
        context.waitTicks(3);
        server.runOnServer(mcServer -> {
            var player = mcServer.getPlayerList().getPlayers().get(0);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SCContent.SC_MANUAL.get()));
            player.inventoryMenu.broadcastChanges();
        });
        context.waitFor(client -> client.player.getMainHandItem().is(SCContent.SC_MANUAL.get()));
        captureScreen(context, SCManualScreen::new, "manual-recipes-screen");
    }

    private static void testTaserRendering(ClientGameTestContext context, TestDedicatedServerContext server,
            TestServerConnection connection) {
        server.runOnServer(mcServer -> {
            mcServer.getPlayerList().getPlayers().get(0).setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SCContent.TASER.get()));
            mcServer.getPlayerList().getPlayers().get(0).inventoryMenu.broadcastChanges();
        });
        context.waitTicks(5);
        context.waitFor(client -> client.player.getMainHandItem().getItem() instanceof TaserItem);
        context.waitTicks(10);
        context.takeScreenshot("taser-first-person");
        context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
        context.waitTicks(5);
        context.takeScreenshot("taser-third-person-pose");
        context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));
    }

    private static void captureScreen(ClientGameTestContext context, Supplier<Screen> screen, String name) {
        Screen expected = context.computeOnClient(client -> screen.get());
        showAndAssertScreen(context, expected);
        context.takeScreenshot(name);
        context.setScreen(() -> null);
    }

    private static void showAndAssertScreen(ClientGameTestContext context, Screen expected) {
        context.setScreen(() -> expected);
        context.waitForScreen(expected.getClass());
        context.waitTicks(3);
        context.runOnClient(client -> {
            check(client.gui.screen() == expected, "Expected screen must remain active after ticks: " + expected.getClass().getSimpleName());
            check(!expected.children().isEmpty(), "The screen must expose interactive widgets: " + expected.getClass().getSimpleName());
        });
    }

    private static void waitForServer(ClientGameTestContext context, TestDedicatedServerContext server,
            java.util.function.Predicate<net.minecraft.server.MinecraftServer> condition, int maxTicks) {
        for (int tick = 0; tick < maxTicks; tick++) {
            if (server.computeOnServer(condition::test))
                return;
            context.waitTick();
        }
        throw new AssertionError("Server condition timed out after " + maxTicks + " ticks");
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }
}
