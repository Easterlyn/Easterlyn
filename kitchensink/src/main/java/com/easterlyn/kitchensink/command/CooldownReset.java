package com.easterlyn.kitchensink.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import com.easterlyn.kitchensink.combo.BackCommand;
import com.easterlyn.kitchensink.combo.DeathPointCommand;
import com.easterlyn.user.User;

@CommandAlias("cooldownreset|cdr|resetcooldown|rcd")
@Description("Reset cooldowns!")
@CommandPermission("easterlyn.command.cooldownreset")
public class CooldownReset {

	@Default
	@Private
	public void resetAll(User user) {
		user.sendMessage("Cooldowns reset!");
		user.getStorage().set(BackCommand.BACK_COOLDOWN, null);
		user.getStorage().set(DeathPointCommand.DEATH_COOLDOWN, null);
	}

	@Subcommand("back")
	public void resetBack(User user) {
		user.sendMessage("Cooldown reset for `/back`!");
		user.getStorage().set(BackCommand.BACK_COOLDOWN, null);
	}

	@Subcommand("death")
	public void resetDeath(User user) {
		user.sendMessage("Cooldown reset for `/death`!");
		user.getStorage().set(DeathPointCommand.DEATH_COOLDOWN, null);
	}

}
