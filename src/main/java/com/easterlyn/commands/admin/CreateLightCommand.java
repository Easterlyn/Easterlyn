package com.easterlyn.commands.admin;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.LightSource;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Basic EasterlynCommand for creating a light source.
 *
 * @author Jikoo
 */
public class CreateLightCommand extends EasterlynCommand {

	public CreateLightCommand(Easterlyn plugin) {
		super(plugin, "createlight");
		this.setAliases("lettherebelight", "fakelight");
		this.setDescription("Create a fake light source at the block on your cursor.");
		this.setPermissionLevel(UserRank.ADMIN);
		this.setUsage("Run /createlight while pointing at a block under 10 blocks away");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}

		Player player = (Player) sender;
		List<Block> blocks = player.getLineOfSight(null, 10);
		if (blocks.isEmpty()) {
			return false;
		}

		LightSource.createLightSource(blocks.get(0).getLocation(), 15);
		player.sendMessage("Light created!");
		return true;
	}

}
