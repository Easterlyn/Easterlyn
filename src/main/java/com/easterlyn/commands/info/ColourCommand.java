package com.easterlyn.commands.info;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * EasterlynCommand for /color
 * 
 * @author Jikoo
 */
public class ColourCommand extends EasterlynCommand {

	public ColourCommand(Easterlyn plugin) {
		super(plugin, "colour");
		this.setAliases("color");
		this.setDescription("List all colours.");
		this.setUsage("/colour");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		StringBuilder sb = new StringBuilder();
		for (ChatColor c : ChatColor.values()) {
			sb.append(c).append('&').append(c.toString().substring(1)).append(' ');
			sb.append(c.name().toLowerCase()).append(ChatColor.RESET).append(' ');
		}
		sender.sendMessage(sb.toString());
		return true;
	}

	@NotNull
	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
		return ImmutableList.of();
	}
}
