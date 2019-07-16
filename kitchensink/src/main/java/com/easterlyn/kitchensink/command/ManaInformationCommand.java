package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CommandRank;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.EconomyUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.bukkit.Material;

public class ManaInformationCommand extends BaseCommand {

	@Dependency
	EasterlynCore core;

	@CommandAlias("manainformation")
	@Default
	@Description("Dump all mana costs to a file.")
	@CommandPermission("easterlyn.command.manainformation")
	@CommandRank(UserRank.DANGER_DANGER_HIGH_VOLTAGE)
	public void manaInformation(BukkitCommandIssuer issuer) {
		if (issuer.isPlayer()) {
			issuer.sendError(MessageKeys.ERROR_PREFIX, "{message}", "Command only executable by console.");
		}

		StringBuilder sb = new StringBuilder();
		for (Material material : Material.values()) {
			if (!material.isItem()) {
				continue;
			}

			sb.append(material.name()).append(": ").append(EconomyUtil.getMappings().get(material)).append('\n');
		}

		File file = new File(core.getDataFolder(), "mana.txt");

		try (FileWriter writer = new FileWriter(file)) {
			writer.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		issuer.sendMessage("Report written to " + file.getPath());
	}

}
