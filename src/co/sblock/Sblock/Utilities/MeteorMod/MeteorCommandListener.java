package co.sblock.Sblock.Utilities.MeteorMod;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;

public class MeteorCommandListener implements CommandListener {
	private ArrayList<Meteorite> meteorites = MeteorMod.getMeteorites();

	@SuppressWarnings("deprecation")
	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean meteormod(CommandSender sender, String arg){
		String[] args = arg.split(" ");
		Player p = (Player) sender;
		Player pTarget = null;
		Location target = null;
		int radius = -1;
		int countdown = -1;
		String material = "";
		boolean blockDamage = false;
		if ((sender instanceof Player && sender.hasPermission("meteor.launch")))	{
			target = p.getTargetBlock(null, 128).getLocation();
			if (args.length == 0)	{
				sender.sendMessage("Cleaning up..");
				for (Meteorite m : meteorites)
					m.doHandlerUnregister();
				return false;
			}
			for (String s : args)	{
				if (s.substring(0, 2).equalsIgnoreCase("p:"))	{			//set target (player or crosshairs)
					pTarget = Bukkit.getPlayer(s.substring(2));
					target = pTarget.getLocation();
				}
				else if (s.substring(0, 2).equalsIgnoreCase("r:"))	{			//set radius
					radius = Integer.parseInt(s.substring(2));
				}
				else if (s.substring(0, 2).equalsIgnoreCase("e:"))	{			//set explosion block damage
					if (s.substring(2).equalsIgnoreCase("true") || s.substring(2).equalsIgnoreCase("false"))	
						blockDamage = Boolean.parseBoolean(s.substring(2));						
				}
				else if (s.substring(0, 2).equalsIgnoreCase("c:"))	{			//set countdown timer
					countdown = Integer.parseInt(s.substring(2));
				}
				else if (s.substring(0, 2).equalsIgnoreCase("m:"))	{
					material = s.substring(2);
				}
			}
			meteorites.add(new Meteorite(MeteorMod.getInstance(), pTarget, target, material, radius, countdown, blockDamage));
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@SblockCommand(consoleFriendly = false)
	public boolean meteor(CommandSender sender)	{
		Player p = (Player) sender;
		Player pTarget = null;
		Location target = null;
		int radius = -1;
		int countdown = -1;
		String material = "";
		boolean blockDamage = false;
		if (sender.hasPermission("meteor.launch"))	{
			target = p.getTargetBlock(null, 128).getLocation();
			meteorites.add(new Meteorite(MeteorMod.getInstance(), pTarget, target, material, radius, countdown, blockDamage));
			return true;
		}
		return false;
	}
}
