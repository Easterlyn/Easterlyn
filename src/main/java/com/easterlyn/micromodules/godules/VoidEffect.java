package com.easterlyn.micromodules.godules;

import com.easterlyn.micromodules.Godule;
import com.easterlyn.users.UserAspect;

import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Effect for the Void "god" entering the game.
 * 
 * @author Jikoo
 */
public class VoidEffect extends AspectEffect {

	public VoidEffect(Godule godule) {
		super(godule);
	}

	@Override
	protected void enable() {
		World prime = Bukkit.getWorlds().get(0);
		if (getGodule().isEnabled(UserAspect.VOID)) {
			prime.setGameRuleValue("doDaylightCycle", "true");
		} else {
			prime.setGameRuleValue("doDaylightCycle", "false");
			prime.setTime(24000L);
		}
	}

	@Override
	protected void disable() {
		if (getGodule().isEnabled(UserAspect.LIGHT)) {
			// Forcibly re-enable Light if present
			getGodule().getAspectEffect(UserAspect.LIGHT).enable();
		} else {
			Bukkit.getWorlds().get(0).setGameRuleValue("doDaylightCycle", "true");
		}
	}

}
