package net.geforcemods.securitycraft.platform;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.geforcemods.securitycraft.network.client.*;
import net.geforcemods.securitycraft.network.server.*;

/** Registers payload wire formats on both sides without installing client handlers on a server. */
public final class NetworkRegistration {
    private NetworkRegistration() {}
    public static void registerPayloads() {
        PayloadTypeRegistry.clientboundPlay().register(InteractWithFrame.TYPE, InteractWithFrame.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenScreen.TYPE, OpenScreen.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RefreshDisguisableModel.TYPE, RefreshDisguisableModel.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SendManualPages.TYPE, SendManualPages.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SetCameraView.TYPE, SetCameraView.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SetTrophySystemTarget.TYPE, SetTrophySystemTarget.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ToggleAlarmSound.TYPE, ToggleAlarmSound.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(UpdateLaserColors.TYPE, UpdateLaserColors.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(UpdateLogger.TYPE, UpdateLogger.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CheckBriefcasePasscode.TYPE, CheckBriefcasePasscode.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CheckPasscode.TYPE, CheckPasscode.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClearChangeDetectorServer.TYPE, ClearChangeDetectorServer.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClearLoggerServer.TYPE, ClearLoggerServer.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DismountCamera.TYPE, DismountCamera.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MountCamera.TYPE, MountCamera.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RemoteControlMine.TYPE, RemoteControlMine.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RemoveCameraTag.TYPE, RemoveCameraTag.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RemoveMineFromMRAT.TYPE, RemoveMineFromMRAT.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RemovePositionFromSSS.TYPE, RemovePositionFromSSS.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RemoveSentryFromSRAT.TYPE, RemoveSentryFromSRAT.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RequestSecureTradingStationTransactions.TYPE, RequestSecureTradingStationTransactions.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetBriefcasePasscodeAndOwner.TYPE, SetBriefcasePasscodeAndOwner.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetCameraPowered.TYPE, SetCameraPowered.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetDefaultCameraViewingDirection.TYPE, SetDefaultCameraViewingDirection.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetGhostSlot.TYPE, SetGhostSlot.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetKeycardUses.TYPE, SetKeycardUses.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetListModuleData.TYPE, SetListModuleData.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetPasscode.TYPE, SetPasscode.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetSentryMode.TYPE, SetSentryMode.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetStateOnDisguiseModule.TYPE, SetStateOnDisguiseModule.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncAlarmSettings.TYPE, SyncAlarmSettings.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncBlockChangeDetector.TYPE, SyncBlockChangeDetector.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncBlockPocketManager.TYPE, SyncBlockPocketManager.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncBlockReinforcer.TYPE, SyncBlockReinforcer.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncFrame.TYPE, SyncFrame.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncKeycardSettings.TYPE, SyncKeycardSettings.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncLaserSideConfig.TYPE, SyncLaserSideConfig.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncProjector.TYPE, SyncProjector.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncRiftStabilizer.TYPE, SyncRiftStabilizer.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncSSSSettingsOnServer.TYPE, SyncSSSSettingsOnServer.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncSecureRedstoneInterface.TYPE, SyncSecureRedstoneInterface.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncTrophySystem.TYPE, SyncTrophySystem.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ToggleBlockPocketManager.TYPE, ToggleBlockPocketManager.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ToggleModule.TYPE, ToggleModule.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ToggleNightVision.TYPE, ToggleNightVision.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ToggleOption.TYPE, ToggleOption.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UpdateSliderValue.TYPE, UpdateSliderValue.STREAM_CODEC);
    }
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(CheckBriefcasePasscode.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(CheckPasscode.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(ClearChangeDetectorServer.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(ClearLoggerServer.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(DismountCamera.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(MountCamera.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(RemoteControlMine.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(RemoveCameraTag.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(RemoveMineFromMRAT.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(RemovePositionFromSSS.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(RemoveSentryFromSRAT.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(RequestSecureTradingStationTransactions.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SetBriefcasePasscodeAndOwner.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SetCameraPowered.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SetDefaultCameraViewingDirection.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SetGhostSlot.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SetKeycardUses.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SetListModuleData.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SetPasscode.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SetSentryMode.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SetStateOnDisguiseModule.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncAlarmSettings.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncBlockChangeDetector.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncBlockPocketManager.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncBlockReinforcer.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncFrame.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncKeycardSettings.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncLaserSideConfig.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncProjector.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncRiftStabilizer.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncSSSSettingsOnServer.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncSecureRedstoneInterface.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(SyncTrophySystem.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(ToggleBlockPocketManager.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(ToggleModule.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(ToggleNightVision.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(ToggleOption.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
        ServerPlayNetworking.registerGlobalReceiver(UpdateSliderValue.TYPE, (payload, context) -> context.server().execute(() -> payload.handle(new PayloadContext(context.player(), context.server()))));
    }
}
