package com.fightadvisor.mixin;

import com.fightadvisor.Incognito;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Przez Team.decorateName gra buduje nazwy na scoreboardzie, w tabliscie i nad glowami. */
@Mixin(Team.class)
public class TeamMixin {
	@ModifyVariable(
			method = "decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;",
			at = @At("HEAD"),
			argsOnly = true,
			require = 0)
	private static Text fightadvisor$incognito(Text name) {
		return Incognito.replace(name);
	}
}
