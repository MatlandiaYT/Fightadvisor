package com.fightadvisor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Male menu otwierane klawiszem V: przycisk wlacz/wylacz oraz zamkniecie. */
public class FightAdvisorScreen extends Screen {

	public FightAdvisorScreen() {
		super(Text.literal("FightAdvisor"));
	}

	private static Text toggleLabel() {
		boolean on = FightAdvisorClient.isEnabled();
		return Text.literal("Podpowiedzi: ")
				.append(Text.literal(on ? "WŁĄCZONE" : "WYŁĄCZONE")
						.formatted(on ? Formatting.GREEN : Formatting.RED));
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int cy = this.height / 2;

		addDrawableChild(ButtonWidget.builder(toggleLabel(), button -> {
			FightAdvisorClient.setEnabled(!FightAdvisorClient.isEnabled());
			button.setMessage(toggleLabel());
		}).dimensions(cx - 100, cy - 12, 200, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Zamknij"), button -> close())
				.dimensions(cx - 100, cy + 14, 200, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 36, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("ORAJ / RÓWNO / WYCOFAJ SIĘ pod celownikiem").formatted(Formatting.GRAY),
				this.width / 2, this.height / 2 - 24, 0xFFAAAAAA);
	}

	/** Gra nie zatrzymuje sie przy otwartym menu (przydatne w walce). */
	@Override
	public boolean shouldPause() {
		return false;
	}
}
