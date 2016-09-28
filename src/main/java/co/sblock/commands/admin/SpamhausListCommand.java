package co.sblock.commands.admin;

import co.sblock.Sblock;
import co.sblock.commands.SblockAsynchronousCommand;
import co.sblock.events.Events;
import co.sblock.utilities.TextUtils;

import org.bukkit.command.CommandSender;

/**
 * SblockAsynchronousCommand for manipulating the whitelist of IPs that are not checked with Spamhaus.
 * 
 * @author Jikoo
 */
public class SpamhausListCommand extends SblockAsynchronousCommand {

	private final Events events;

	public SpamhausListCommand(Sblock plugin) {
		super(plugin, "spamhauslist");
		this.setAliases("spamlist", "sl");
		this.setPermissionLevel("horrorterror");
		this.events = plugin.getModule(Events.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 2 || !TextUtils.IP_PATTERN.matcher(args[1]).find()) {
			return false;
		}
		args[0] = args[0].toLowerCase();
		if (args[0].equals("add")) {
			if (!events.getSpamhausWhitelist().contains(args[1])) {
				events.getSpamhausWhitelist().add(args[1]);
			}
			sender.sendMessage(this.getLang().getValue("command.spamhauslist.add").replace("{IP}", args[1]));
			return true;
		}
		if (args[0].equals("remove")) {
			events.getSpamhausWhitelist().remove(args[1]);
			sender.sendMessage(this.getLang().getValue("command.spamhauslist.remove").replace("{IP}", args[1]));
			return true;
		}
		return false;
	}

}
