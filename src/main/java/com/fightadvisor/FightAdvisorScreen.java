package com.fightadvisor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Menu otwierane klawiszem V: wlacz/wylacz, tryb, tylko gracze, pozycja napisu. */
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

	private static Text modeLabel() {
		return Text.literal("Tryb: ")
				.append(Text.literal(FightAdvisorClient.getMode().label).formatted(Formatting.YELLOW));
	}

	private static Text playersLabel() {
		boolean on = FightAdvisorClient.isPlayersOnly();
		return Text.literal("Tylko gracze: ")
				.append(Text.literal(on ? "TAK" : "NIE").formatted(on ? Formatting.GREEN : Formatting.GRAY));
	}

	private static Text positionLabel() {
		return Text.literal("Pozycja: ")
				.append(Text.literal(FightAdvisorClient.POSITION_LABELS[FightAdvisorClient.getPosition()])
						.formatted(Formatting.AQUA));
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int top = this.height / 2 - 46;
		int step = 24;

		addDrawableChild(ButtonWidget.builder(toggleLabel(), button -> {
			FightAdvisorClient.setEnabled(!FightAdvisorClient.isEnabled());
			button.setMessage(toggleLabel());
		}).dimensions(cx - 100, top, 200, 20).build());

		addDrawableChild(ButtonWidget.builder(modeLabel(), button -> {
			FightAdvisorClient.cycleMode();
			button.setMessage(modeLabel());
		}).dimensions(cx - 100, top + step, 200, 20).build());

		addDrawableChild(ButtonWidget.builder(playersLabel(), button -> {
			FightAdvisorClient.togglePlayersOnly();
			button.setMessage(playersLabel());
		}).dimensions(cx - 100, top + step * 2, 200, 20).build());

		addDrawableChild(ButtonWidget.builder(positionLabel(), button -> {
			FightAdvisorClient.cyclePosition();
			button.setMessage(positionLabel());
		}).dimensions(cx - 100, top + step * 3, 200, 20).build());

		addDrawableChild(ButtonWidget.builder(Text.literal("Zamknij"), button -> close())
				.dimensions(cx - 100, top + step * 4 + 6, 200, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		int top = this.height / 2 - 46;
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, top - 24, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("ORAJ / RÓWNO / WYCOFAJ SIĘ").formatted(Formatting.GRAY),
				this.width / 2, top - 12, 0xFFAAAAAA);
	}

	/** Gra nie zatrzymuje sie przy otwartym menu (przydatne w walce). */
	@Override
	public boolean shouldPause() {
		return false;
	}
}
