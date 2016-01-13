package co.sblock.micromodules.godules;

import org.bukkit.Bukkit;
import org.bukkit.World;

import co.sblock.micromodules.Godule;
import co.sblock.users.UserAspect;

/**
 * Effect for the Void "god" entering the game.
 * 
 * @author Jikoo
 */
public class VoidEffect extends AspectEffect {

	@Override
	protected void enable() {
		World prime = Bukkit.getWorlds().get(0);
		if (Godule.getInstance().isEnabled(UserAspect.VOID)) {
			prime.setGameRuleValue("doDaylightCycle", "true");
		} else {
			prime.setGameRuleValue("doDaylightCycle", "false");
			prime.setTime(24000L);
		}
	}

	@Override
	protected void disable() {
		if (Godule.getInstance().isEnabled(UserAspect.LIGHT)) {
			// Forcibly re-enable Light if present
			Godule.getInstance().getAspectEffect(UserAspect.LIGHT).enable();
		} else {
			Bukkit.getWorlds().get(0).setGameRuleValue("doDaylightCycle", "true");
		}
	}

}
