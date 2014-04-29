package co.sblock.utilities.meteors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.CommandListener;
import co.sblock.SblockCommand;

/**
 * @author Dublek, Jikoo
 */
public class MeteorCommandListener implements CommandListener {

	/**
	 * Main MeteorMod Command.
	 * 
	 * @param sender the CommandSender
	 * @param arg the Command arguments
	 * @return true if Command was used correctly
	 */
	@SuppressWarnings("deprecation")
	@SblockCommand(description = "Summon a meteor with parameters.", permission = "meteor.launch",
			usage = "/meteor <u:user> <r:radius> <e:explode> <c:countdown> <m:material>")
	public boolean meteor(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		Location target = null;
		int radius = -1;
		String material = "";
		boolean blockDamage = false;
		int bore = -1;
		target = p.getTargetBlock(null, 128).getLocation();
		for (String s : args) {
			// lighter than multiple .equalsIgnoreCase
			s = s.toLowerCase();
			if (s.substring(0, 2).equals("u:")) {
				// set target (player or crosshairs)
				Player pTarget = Bukkit.getPlayer(s.substring(2));
				if (pTarget != null) {
					target = pTarget.getLocation();
				}
			} else if (s.substring(0, 2).equals("r:")) {
				// set radius
				radius = Integer.parseInt(s.substring(2));
			} else if (s.substring(0, 2).equals("e:")) {
				// set explosion block damage
				blockDamage = s.substring(2).equals("true");
			} else if (s.substring(0, 2).equals("m:")) {
				material = s.substring(2).toUpperCase();
			} else if (s.subSequence(0, 2).equals("b:")) {
				// set meteor to bore mode (default behavior: bore if not highest block)
				bore = s.substring(2).equals("true") ? 1 : 0;
			}
		}
		new Meteorite(target, material, radius, blockDamage, bore).dropMeteorite();
		return true;
	}
}
