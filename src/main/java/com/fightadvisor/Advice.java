package com.fightadvisor;

import net.minecraft.entity.LivingEntity;

/** Ocena starcia na podstawie HP, zlotych serc i pancerza (widocznych w grze). */
public class Advice {
	public enum Verdict {
		FIGHT("ORAJ!", 0xFF55FF55),
		EVEN("RÓWNO - UWAŻAJ", 0xFFFFFF55),
		RETREAT("WYCOFAJ SIĘ", 0xFFFF5555);

		public final String label;
		public final int color;

		Verdict(String label, int color) {
			this.label = label;
			this.color = color;
		}
	}

	/** Efektywne HP: zycie + absorpcja, skorygowane o pancerz (uproszczony model). */
	public static float effectiveHp(LivingEntity e) {
		float hp = e.getHealth() + e.getAbsorptionAmount();
		float armor = Math.min(20f, e.getArmor());
		float reduction = armor / 25f; // 20 pkt pancerza = 80% redukcji
		return hp / (1f - reduction);
	}

	public static Verdict judge(float myEff, float theirEff) {
		float ratio = myEff / Math.max(theirEff, 0.1f);
		if (ratio >= 1.3f) return Verdict.FIGHT;
		if (ratio >= 0.8f) return Verdict.EVEN;
		return Verdict.RETREAT;
	}
}
