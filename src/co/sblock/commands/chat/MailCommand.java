package co.sblock.commands.chat;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.commands.SblockCommand;
import co.sblock.machines.type.computer.Program;
import co.sblock.machines.type.computer.Programs;

/**
 * SblockCommand for using the GUI mail client without opening a Computer.
 * 
 * @author Jikoo
 */
public class MailCommand extends SblockCommand {

	private final Program email;

	public MailCommand() {
		super("mail");
		setDescription("Check your email.");
		setUsage("/mail");
		setAliases("email");
		this.email = Programs.getProgramByName("Email");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		email.execute((Player) sender, null);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}

}
