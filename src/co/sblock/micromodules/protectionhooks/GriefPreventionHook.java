package co.sblock.micromodules.protectionhooks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

/**
 * Hook for the protection plugin <a href=http://dev.bukkit.org/bukkit-plugins/grief-prevention/>GriefPrevention</a>.
 * 
 * @author Jikoo
 */
public class GriefPreventionHook extends ProtectionHook {

	public GriefPreventionHook() {
		super("GriefPrevention");
	}

	@Override
	public boolean isProctected(Location location) {
		if (!isHookUsable()) {
			return false;
		}
		return GriefPrevention.instance.dataStore.getClaimAt(location, true, null) != null;
	}

	@Override
	public boolean canUseButtonsAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
		if (claim == null) {
			return true;
		}
		return claim.allowAccess(player) == null;
	}

	@Override
	public boolean canBuildAt(Player player, Location location) {
		if (!isHookUsable()) {
			return true;
		}
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
		if (claim == null) {
			return true;
		}
		return claim.allowBuild(player, Material.DIAMOND_BLOCK) == null;
	}

}
