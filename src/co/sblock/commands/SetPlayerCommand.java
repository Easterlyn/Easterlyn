package co.sblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import co.sblock.users.ProgressionState;
import co.sblock.users.User;
import co.sblock.users.UserManager;

/**
 * SblockCommand for setting User data.
 * 
 * @author Jikoo
 */
public class SetPlayerCommand extends SblockCommand {

	public SetPlayerCommand() {
		super("setplayer");
		this.setDescription("Set player data manually.");
		this.setUsage("/setplayer <playername> <class|aspect|land|dream|prevloc|progression> <value>");
		this.setPermission("group.horrorterror");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (args == null || args.length < 3) {
			return false;
		}
		User user = UserManager.getUser(Bukkit.getPlayer(args[0]).getUniqueId());
		args[1] = args[1].toLowerCase();
		if(args[1].equals("class"))
			user.setPlayerClass(args[2]);
		else if(args[1].equals("aspect"))
			user.setAspect(args[2]);
		else if(args[1].replaceAll("m(edium_?)?planet", "land").equals("land"))
			user.setMediumPlanet(args[2]);
		else if(args[1].replaceAll("d(ream_?)?planet", "dream").equals("dream"))
			user.setDreamPlanet(args[2]);
		else if(args[1].equals("progression"))
			user.setProgression(ProgressionState.valueOf(args[2].toUpperCase()));
		else if (args[1].equals("prevloc")) {
			user.setPreviousLocation(user.getPlayer().getLocation());
		} else
			return false;
		return true;
	}

	// TODO command completion
}
