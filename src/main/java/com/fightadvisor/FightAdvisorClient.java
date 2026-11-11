package com.fightadvisor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
	private static int position = 1; // 0 = nad celownikiem, 1 = pod, 2 = nisko

	public static final String[] POSITION_LABELS = {"Nad celownikiem", "Pod celownikiem", "Nisko"};

	public static boolean isEnabled() { return enabled; }
	public static void setEnabled(boolean value) { enabled = value; }

	public static Advice.Mode getMode() { return mode; }
	public static void cycleMode() { mode = mode.next(); }

	public static boolean isPlayersOnly() { return playersOnly; }
	public static void togglePlayersOnly() { playersOnly = !playersOnly; }

	public static int getPosition() { return position; }
	public static void cyclePosition() { position = (position + 1) % POSITION_LABELS.length; }

	private static LivingEntity lastAttacked = null;
	private static long lastAttackTime = 0;

	@Override
	public void onInitializeClient() {
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.fightadvisor.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.fightadvisor"));

		// V otwiera menu FightAdvisor (tam wlaczasz/wylaczasz napis)
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.wasPressed()) {
				if (client.player != null && client.currentScreen == null) {
					client.setScreen(new FightAdvisorScreen());
				}
			}
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClient && entity instanceof LivingEntity living) {
				lastAttacked = living;
				lastAttackTime = System.currentTimeMillis();
			}
			return ActionResult.PASS;
		});

		HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			PlayerEntity me = client.player;
			if (!enabled || me == null || client.options.hudHidden || client.currentScreen != null) return;

			LivingEntity target = pickTarget(client, me);
			if (target == null) return;
			if (playersOnly && !(target instanceof PlayerEntity)) return;

			float myEff = Advice.effectiveHp(me);
			float theirEff = Advice.effectiveHp(target);
			Advice.Verdict verdict = Advice.judge(myEff, theirEff, mode);

			int cx = ctx.getScaledWindowWidth() / 2;
			int h = ctx.getScaledWindowHeight();
			int y = switch (position) {
				case 0 -> h / 2 - 40;
				case 2 -> h - 75;
				default -> h / 2 + 22;
			};

			// Sam napis z werdyktem pod celownikiem
			ctx.drawCenteredTextWithShadow(client.textRenderer, verdict.label, cx, y, verdict.color);
		});
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
