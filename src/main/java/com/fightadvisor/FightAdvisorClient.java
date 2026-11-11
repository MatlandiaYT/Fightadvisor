package com.fightadvisor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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

	private static LivingEntity lastAttacked = null;
	private static long lastAttackTime = 0;

	@Override
	public void onInitializeClient() {
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.fightadvisor.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.fightadvisor"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.wasPressed()) {
				enabled = !enabled;
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
			int x = cx - PANEL_W / 2;
			int y = ctx.getScaledWindowHeight() / 2 + 22;

			// Panel: tlo + ramka w kolorze werdyktu + pasek akcentu u gory
			ctx.fill(x, y, x + PANEL_W, y + PANEL_H, 0xB0101018);
			drawBorder(ctx, x, y, PANEL_W, PANEL_H, verdict.color);
			ctx.fill(x + 1, y + 1, x + PANEL_W - 1, y + 3, verdict.color);

			// Werdykt
			ctx.drawCenteredTextWithShadow(client.textRenderer, verdict.label, cx, y + 7, verdict.color);

			// Paski HP: Ty / przeciwnik
			drawHpBar(ctx, client, "TY", me, x + 6, y + 21);
			drawHpBar(ctx, client, shorten(target.getName().getString(), 6), target, x + 6, y + 33);
		});
	}

	private static final int PANEL_W = 150;
	private static final int PANEL_H = 47;

	/** Cienka ramka (1 px) wokol prostokata. */
	private static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
		ctx.fill(x, y, x + w, y + 1, color);               // gora
		ctx.fill(x, y + h - 1, x + w, y + h, color);       // dol
		ctx.fill(x, y, x + 1, y + h, color);               // lewo
		ctx.fill(x + w - 1, y, x + w, y + h, color);       // prawo
	}

	/** Wiersz: nazwa | pasek HP (zloty podklad = absorpcja) | liczba HP. */
	private static void drawHpBar(DrawContext ctx, MinecraftClient client, String label,
	                              LivingEntity e, int x, int y) {
		float max = Math.max(1f, e.getMaxHealth());
		float hp = Math.max(0f, e.getHealth());
		float abs = Math.max(0f, e.getAbsorptionAmount());

		int barX = x + 38;
		int barW = 76;
		int barH = 7;
		float frac = Math.min(1f, hp / max);

		int fillColor = frac > 0.6f ? 0xFF55FF55 : frac > 0.3f ? 0xFFFFFF55 : 0xFFFF5555;

		ctx.drawTextWithShadow(client.textRenderer, label, x, y, 0xFFFFFFFF);

		ctx.fill(barX - 1, y - 1, barX + barW + 1, y + barH + 1, 0xFF000000);   // obramowanie paska
		ctx.fill(barX, y, barX + barW, y + barH, 0xFF3A3A44);                    // tlo paska
		ctx.fill(barX, y, barX + Math.round(barW * frac), y + barH, fillColor);  // zdrowie
		if (abs > 0f) {
			int absW = Math.round(barW * Math.min(1f, abs / max));
			ctx.fill(barX, y + barH - 2, barX + absW, y + barH, 0xFFFFCC33);     // absorpcja (zloty)
		}

		String num = String.format("%.0f", hp + abs);
		ctx.drawTextWithShadow(client.textRenderer, num, barX + barW + 5, y, 0xFFFFFFFF);
	}

	private static String shorten(String s, int max) {
		return s.length() <= max ? s : s.substring(0, max - 1) + ".";
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
