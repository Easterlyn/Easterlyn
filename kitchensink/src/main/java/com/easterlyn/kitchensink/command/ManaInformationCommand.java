package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.util.EconomyUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;

public class ManaInformationCommand extends BaseCommand {

	@Dependency
	EasterlynCore core;

	@CommandAlias("manadump")
	@Description("Dump all mana costs to a file.")
	@CommandPermission("easterlyn.command.manainformation")
	@Syntax("")
	@CommandCompletion("")
	public void manaInformation(ConsoleCommandSender issuer) {
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
