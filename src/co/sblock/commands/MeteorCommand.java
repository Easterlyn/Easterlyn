package co.sblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.utilities.meteors.Meteorite;

/**
 * SblockCommand for summoning a meteor.
 * 
 * @author Jikoo
 */
public class MeteorCommand extends SblockCommand {

	public MeteorCommand() {
		super("meteor");
		this.setDescription("Summon a meteor with parameters.");
		this.setUsage("/meteor <p:player> <r:radius> <e:explode> <c:countdown> <m:material> <b:bore>");
		this.setPermission("group.denizen");
		this.setPermissionMessage("By the order of the Jarl, stop right there!");
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		Location target = null;
		if (sender instanceof Player) {
			Player p = (Player) sender;
			target = p.getTargetBlock(null, 128).getLocation();
		}
		int radius = -1;
		String material = "";
		boolean blockDamage = false;
		int bore = -1;
		for (String s : args) {
			// lighter than multiple .equalsIgnoreCase
			s = s.toLowerCase();
			if (s.substring(0, 2).equals("p:")) {
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
		if (target == null) {
			sender.sendMessage("Non-players must specify a player target!");
			return false;
		}
		new Meteorite(target, material, radius, blockDamage, bore).dropMeteorite();
		return true;
	}

}
