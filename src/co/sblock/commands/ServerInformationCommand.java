package co.sblock.commands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

/**
 * SblockCommand for obtaining more detailed server information.
 * 
 * @author Jikoo
 */
public class ServerInformationCommand extends SblockAsynchronousCommand {

	public ServerInformationCommand(String name) {
		super("serverinfo");
		this.setDescription("Detailed info about the server or a world.");
		this.setUsage("/serverinfo [world] [chunk|entity|tile]");
		this.setPermissionLevel("horrorterror");
		this.setPermissionMessage("You're playing on Sblock! What a shock.");
	}

	@Override
	protected boolean onCommand(final CommandSender sender, final String label, final String[] args) {
		final List<World> worlds;
		if (args.length == 0) {
			// Minimal risk calling this async, not worried.
			worlds = Bukkit.getWorlds();
		} else {
			final World world = Bukkit.getWorld(args[0]);
			if (world == null) {
				sender.sendMessage(ChatColor.RED + "No such world " + args[0] + " loaded!");
				return true;
			}
			worlds = new ArrayList<>();
			worlds.add(world);
		}
		final File file = new File(getPlugin().getDataFolder(), "report.txt");
		// Again, minimal risk calling async
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			sender.sendMessage(ChatColor.RED + "IOException creating report.txt");
			return true;
		}
		StringBuilder sb = new StringBuilder();
		// This is slightly risky
		for (World world : worlds) {
			sb.append(world.getName()).append(":\n chunks:");
			for (Chunk chunk : world.getLoadedChunks()) {
				try {
					sb.append("\n  ").append(chunk.getX()).append('_').append(chunk.getZ()).append(":\n   entities:\n");
					for (Entity entity : chunk.getEntities()) {
						sb.append("    ").append(entity.toString()).append('\n');
					}
					sb.append("   tiles:\n");
					for (BlockState tile : chunk.getTileEntities()) {
						sb.append("    ").append(tile.toString()).append('\n');
					}
				} catch (Exception e) {
					// Chunk has probably been unloaded. If this actually happens I'd like to know.
					e.printStackTrace();
				}
			}
		}
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(sb.toString());
		} catch (IOException e) {
			sender.sendMessage(ChatColor.RED + "IOException creating report.txt");
			return true;
		}
		sender.sendMessage(ChatColor.GREEN + "Report written to /plugins/Sblock/report.txt");
		return true;
	}
}
