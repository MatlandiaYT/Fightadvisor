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

	public static boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean value) {
		enabled = value;
	}

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

			float myEff = Advice.effectiveHp(me);
			float theirEff = Advice.effectiveHp(target);
			Advice.Verdict verdict = Advice.judge(myEff, theirEff);

			int cx = ctx.getScaledWindowWidth() / 2;
			int y = ctx.getScaledWindowHeight() / 2 + 22;

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
