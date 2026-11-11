package com.fightadvisor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * Incognito: podmienia Twoj nick na falszywy WYLACZNIE na Twoim ekranie
 * (tablista, scoreboard, nazwy w grze). Inni gracze nadal widza prawdziwy nick.
 */
public class Incognito {
	public static boolean enabled = false;
	public static String fakeName = "Gracz";

	public static Text replace(Text name) {
		if (!enabled) return name;
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.getSession() == null) return name;

		String real = client.getSession().getUsername();
		if (real != null && name.getString().equals(real)) {
			String fake = fakeName == null || fakeName.isBlank() ? "Gracz" : fakeName;
			return Text.literal(fake).setStyle(name.getStyle());
		}
		return name;
	}
}
