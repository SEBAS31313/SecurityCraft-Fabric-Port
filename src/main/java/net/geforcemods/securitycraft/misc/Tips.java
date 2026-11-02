package net.geforcemods.securitycraft.misc;

import java.util.HashMap;
import java.util.Map;

import net.geforcemods.securitycraft.ClientHandler;
import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class Tips {
	public static final Map<String, String> TIPS_WITH_LINK = new HashMap<>();

	static {
		TIPS_WITH_LINK.put("patreon", "https://www.patreon.com/Geforce");
		TIPS_WITH_LINK.put("discord", "https://discord.gg/U8DvBAW");
	}

	private Tips() {}

	public static void onLoggingIn() {
		if (!ConfigHandler.SERVER.disableThanksMessage.get() && ConfigHandler.CLIENT.sayThanksMessage.get()) {
			//@formatter:off
			String tipKey = getRandomTip();
			MutableComponent message = Component.literal("[")
					.append(Component.literal("SecurityCraft").withStyle(ChatFormatting.GOLD))
					.append(Component.literal("] "))
					.append(Utils.localize("messages.securitycraft:thanks",
							SecurityCraft.getVersion(),
							Utils.localize("messages.securitycraft:tip"),
							Utils.localize(tipKey)));
			//@formatter:on

			if (TIPS_WITH_LINK.containsKey(tipKey.split("\\.")[2]))
				message = message.append(Component.literal(TIPS_WITH_LINK.get(tipKey.split("\\.")[2])).withStyle(style -> style.withUnderlined(true).withClickEvent(new net.minecraft.network.chat.ClickEvent.OpenUrl(java.net.URI.create(TIPS_WITH_LINK.get(tipKey.split("\\.")[2]))))));

			ClientHandler.getClientPlayer().sendSystemMessage(message);
		}
	}

	private static String getRandomTip() {
		//@formatter:off
		String[] tips = {
				"messages.securitycraft:tip.scHelp",
				"messages.securitycraft:tip.patreon",
				"messages.securitycraft:tip.discord",
				"messages.securitycraft:tip.scserver"
		};
		//@formatter:on

		return tips[SecurityCraft.RANDOM.nextInt(tips.length)];
	}

}
