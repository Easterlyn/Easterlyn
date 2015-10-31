package co.sblock.commands.admin;

import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.commands.SblockCommand;
import co.sblock.utilities.LightSource;

/**
 * Basic SblockCommand for creating a light source.
 * 
 * @author Jikoo
 */
public class CreateLightCommand extends SblockCommand {

	public CreateLightCommand() {
		super("createlight");
		this.setDescription("Create a fake light source at the block on your cursor.");
		this.setUsage("Run /createlight while pointing at a block under 10 blocks away");
		this.setAliases("lettherebelight", "fakelight");
		this.setPermissionLevel("denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}

		Player player = (Player) sender;
		List<Block> blocks = player.getLineOfSight((Set<Material>) null, 10);
		if (blocks.isEmpty()) {
			return false;
		}

		LightSource.createLightSource(blocks.get(0).getLocation(), 15);
		player.sendMessage("Light created!");
		return true;
	}

}
