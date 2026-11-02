package net.geforcemods.securitycraft.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;







public class SecurityCraftAPI {
	private static List<IExtractionBlock> registeredExtractionBlocks = new ArrayList<>();
	private static List<IPasscodeConvertible> registeredPasscodeConvertibles = new ArrayList<>();
	private static List<IAttackTargetCheck> registeredSentryAttackTargetChecks = new ArrayList<>();
	private static List<IDoorActivator> registeredDoorActivators = new ArrayList<>();
	public static final String IMC_EXTRACTION_BLOCK_MSG = "registerExtractionBlock";
	public static final String IMC_PASSCODE_CONVERTIBLE_MSG = "registerPasscodeConvertible";
	public static final String IMC_SENTRY_ATTACK_TARGET_MSG = "registerSentryAttackTargetCheck";
	public static final String IMC_DOOR_ACTIVATOR_MSG = "registerDoorActivator";

	private SecurityCraftAPI() {}

    public static void registerExtractionBlock(IExtractionBlock value) { registeredExtractionBlocks.add(java.util.Objects.requireNonNull(value)); }
    public static void registerPasscodeConvertible(IPasscodeConvertible value) { registeredPasscodeConvertibles.add(java.util.Objects.requireNonNull(value)); }
    public static void registerSentryAttackTargetCheck(IAttackTargetCheck value) { registeredSentryAttackTargetChecks.add(java.util.Objects.requireNonNull(value)); }
    public static void registerDoorActivator(IDoorActivator value) { registeredDoorActivators.add(java.util.Objects.requireNonNull(value)); }
    public static void finishRegistration() {
        registeredExtractionBlocks=List.copyOf(registeredExtractionBlocks);
        registeredPasscodeConvertibles=List.copyOf(registeredPasscodeConvertibles);
        registeredSentryAttackTargetChecks=List.copyOf(registeredSentryAttackTargetChecks);
        registeredDoorActivators=List.copyOf(registeredDoorActivators);
    }

	public static List<IExtractionBlock> getRegisteredExtractionBlocks() {
		return registeredExtractionBlocks;
	}

	public static List<IPasscodeConvertible> getRegisteredPasscodeConvertibles() {
		return registeredPasscodeConvertibles;
	}

	public static List<IAttackTargetCheck> getRegisteredSentryAttackTargetChecks() {
		return registeredSentryAttackTargetChecks;
	}

	public static List<IDoorActivator> getRegisteredDoorActivators() {
		return registeredDoorActivators;
	}
}
