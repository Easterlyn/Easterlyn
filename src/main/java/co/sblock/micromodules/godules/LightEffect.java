package co.sblock.micromodules.godules;

import org.bukkit.Bukkit;
import org.bukkit.World;

import co.sblock.micromodules.Godule;
import co.sblock.users.UserAspect;

/**
 * Effect for the Light "god" entering the game.
 * 
 * @author Jikoo
 */
public class LightEffect extends AspectEffect {

	public LightEffect(Godule godule) {
		super(godule);
	}

	@Override
	protected void enable() {
		World prime = Bukkit.getWorlds().get(0);
		if (getGodule().isEnabled(UserAspect.VOID)) {
			prime.setGameRuleValue("doDaylightCycle", "true");
		} else {
			prime.setGameRuleValue("doDaylightCycle", "false");
			prime.setTime(12000L);
		}
	}

	@Override
	protected void disable() {
		if (getGodule().isEnabled(UserAspect.VOID)) {
			// Forcibly re-enable Light if present
			getGodule().getAspectEffect(UserAspect.VOID).enable();
		} else {
			Bukkit.getWorlds().get(0).setGameRuleValue("doDaylightCycle", "true");
		}
	}

}
