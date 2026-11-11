package com.fightadvisor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class FightAdvisorClient implements ClientModInitializer {
	private static final long TARGET_MEMORY_MS = 5000;
	private static final double MAX_DISTANCE = 10.0;

	private static KeyBinding toggleKey;
	private static boolean enabled = true;

	// Ustawienia z menu (V). Zapamietywane do zamkniecia gry.
	private static Advice.Mode mode = Advice.Mode.NORMAL;
	private static boolean playersOnly = false;

	public static boolean isEnabled() { return enabled; }
	public static void setEnabled(boolean value) { enabled = value; }

	public static Advice.Mode getMode() { return mode; }
	public static void cycleMode() { mode = mode.next(); }

	public static boolean isPlayersOnly() { return playersOnly; }
	public static void togglePlayersOnly() { playersOnly = !playersOnly; }

	private static LivingEntity lastAttacked = null;
	private static long lastAttackTime = 0;
	private static boolean showingMessage = false;

	@Override
	public void onInitializeClient() {
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.fightadvisor.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.fightadvisor"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// V otwiera menu FightAdvisor
			while (toggleKey.wasPressed()) {
				if (client.player != null && client.currentScreen == null) {
					client.setScreen(new FightAdvisorScreen());
				}
			}
			updateActionBar(client);
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClient && entity instanceof LivingEntity living) {
				lastAttacked = living;
				lastAttackTime = System.currentTimeMillis();
			}
			return ActionResult.PASS;
		});
	}

	/** Werdykt w action barze (pasek nad hotbarem). */
	private static void updateActionBar(MinecraftClient client) {
		PlayerEntity me = client.player;
		LivingEntity target = null;
		if (enabled && me != null) {
			target = pickTarget(client, me);
			if (target != null && playersOnly && !(target instanceof PlayerEntity)) target = null;
		}

		if (target == null) {
			if (showingMessage && client.inGameHud != null) {
				client.inGameHud.setOverlayMessage(Text.empty(), false); // schowaj napis
				showingMessage = false;
			}
			return;
		}

		Advice.Verdict verdict = Advice.judge(Advice.effectiveHp(me), Advice.effectiveHp(target), mode);
		int rgb = verdict.color & 0xFFFFFF;
		client.inGameHud.setOverlayMessage(
				Text.literal(verdict.label).styled(style -> style.withColor(rgb)), false);
		showingMessage = true;
	}

	/** Cel: byt na celowniku, a jesli brak - ostatnio uderzony w ciagu 5 s. */
	private static LivingEntity pickTarget(MinecraftClient client, PlayerEntity me) {
		Entity looked = client.targetedEntity;
		if (looked instanceof LivingEntity living && living.isAlive() && me.distanceTo(living) <= MAX_DISTANCE) {
			return living;
		}
		if (lastAttacked != null) {
			boolean fresh = System.currentTimeMillis() - lastAttackTime <= TARGET_MEMORY_MS;
			if (fresh && lastAttacked.isAlive() && me.distanceTo(lastAttacked) <= MAX_DISTANCE) {
				return lastAttacked;
			}
		}
		return null;
	}
}
