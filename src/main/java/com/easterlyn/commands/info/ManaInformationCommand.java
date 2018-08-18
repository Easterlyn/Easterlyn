package com.easterlyn.commands.info;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.CruxiteDowel;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynAsynchronousCommand;
import com.easterlyn.users.UserRank;
import com.google.common.collect.ImmutableList;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * ManaInformationCommand
 *
 * @author Jikoo
 */
public class ManaInformationCommand extends EasterlynAsynchronousCommand {


	public ManaInformationCommand(Easterlyn plugin) {
		super(plugin, "manainfo");
		this.setDescription("List all calculated mana costs. Requires file access.");
		this.setPermissionLevel(UserRank.DANGER_DANGER_HIGH_VOLTAGE);
		this.setPermissionMessage("Try /mana!");
	}

	@Override
	protected boolean onCommand(final CommandSender sender, final String label, final String[] args) {
		StringBuilder sb = new StringBuilder();
		// This is slightly risky
		for (Material material : Material.values()) {
			if (material.isLegacy()) {
				continue;
			}
			sb.append(material.name()).append(": ").append(CruxiteDowel.getMana().get(material)).append('\n');
		}
		final File file = new File(getPlugin().getDataFolder(), "mana.txt");
		// Again, minimal risk calling async
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			sender.sendMessage(Language.getColor("bad") + "IOException creating mana.txt");
			return true;
		}
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(sb.toString());
		} catch (IOException e) {
			sender.sendMessage(Language.getColor("bad") + "IOException creating mana.txt");
			return true;
		}
		sender.sendMessage(Language.getColor("good") + "Report written to plugins/Easterlyn/mana.txt");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}

}
