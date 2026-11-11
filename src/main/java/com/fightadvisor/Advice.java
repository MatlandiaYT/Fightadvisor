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

	/** Tryb ostroznosci: im ostrozniejszy, tym wiekszej przewagi wymaga zanim powie ORAJ. */
	public enum Mode {
		CAUTIOUS("Ostrożny", 1.6f, 1.0f),
		NORMAL("Normalny", 1.3f, 0.8f),
		BOLD("Odważny", 1.0f, 0.6f);

		public final String label;
		public final float fightRatio;   // od tej przewagi: ORAJ
		public final float retreatRatio; // ponizej tej: WYCOFAJ SIE

		Mode(String label, float fightRatio, float retreatRatio) {
			this.label = label;
			this.fightRatio = fightRatio;
			this.retreatRatio = retreatRatio;
		}

		public Mode next() {
			Mode[] all = values();
			return all[(ordinal() + 1) % all.length];
		}
	}

	public static Verdict judge(float myEff, float theirEff, Mode mode) {
		float ratio = myEff / Math.max(theirEff, 0.1f);
		if (ratio >= mode.fightRatio) return Verdict.FIGHT;
		if (ratio >= mode.retreatRatio) return Verdict.EVEN;
		return Verdict.RETREAT;
	}
}
