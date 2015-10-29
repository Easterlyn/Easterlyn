package co.sblock.commands.entry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.micromodules.Meteorite;

/**
 * SblockCommand for summoning a meteor.
 * 
 * @author Jikoo
 */
public class MeteorCommand extends SblockCommand {

	private final String[] primaryArgs;

	public MeteorCommand() {
		super("meteor");
		this.setDescription("Summon a meteor with parameters.");
		this.setUsage("/meteor [p:player] [r:radius] [e:explode] [m:material] [b:bore]");
		this.setPermissionLevel("denizen");
		primaryArgs = new String[] {"p:", "r:", "e:", "b:", "m:"};
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Location target = null;
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
			if (sender instanceof Player) {
				Player p = (Player) sender;
				Block block = p.getTargetBlock((HashSet<Material>) null, 128);
				if (block != null) {
					target = block.getLocation();
				}
				if (target == null) {
					sender.sendMessage(Color.BAD + "Block on crosshair too far away! Aim closer or specify target!");
					return false;
				}
			} else {
				sender.sendMessage("Non-players must specify a player target!");
				return false;
			}
		}
		if (radius > 20) {
			radius = 20;
			sender.sendMessage(Color.BAD_EMPHASIS + "Very large meteors cause quite a bit of lag. Keep it down.");
		}
		new Meteorite(target, material, radius, blockDamage, bore).dropMeteorite();
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission())) {
			return ImmutableList.of();
		}
		String current = args[args.length - 1];
		ArrayList<String> matches = new ArrayList<>();
		if (current.length() < 2) {
			current = current.toLowerCase();
			for (String argument : primaryArgs) {
				if (argument.startsWith(current)) {
					matches.add(argument);
				}
			}
			return matches;
		}
		String argCompleting = current.substring(0, 2).toLowerCase();
		current = current.substring(2);
		if (argCompleting.equals("p:")) {
			for (String player : super.tabComplete(sender, alias, new String[] {current})) {
				matches.add("p:" + player);
			}
			return matches;
		}
		if (argCompleting.equals("r:")) {
			matches.add("r:#");
			return matches;
		}
		if (argCompleting.equals("e:") || argCompleting.equals("b:")) {
			String arg = "true";
			if (arg.startsWith(current)) {
				matches.add(argCompleting + arg);
			}
			arg = "false";
			if (arg.startsWith(current)) {
				matches.add(argCompleting + arg);
			}
			return matches;
		}
		if (argCompleting.equals("m:")) {
			current = current.toUpperCase();
			for (Material material : Material.values()) {
				if (material.name().startsWith(current)) {
					matches.add(argCompleting + material.name());
				}
			}
			return matches;
		}
		return matches;
	}
}
