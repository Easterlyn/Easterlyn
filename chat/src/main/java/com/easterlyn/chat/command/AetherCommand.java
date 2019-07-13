package com.easterlyn.chat.command;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Single;
import com.easterlyn.EasterlynChat;
import com.easterlyn.EasterlynCore;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.event.UserChatEvent;
import com.easterlyn.command.CommandRank;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.user.AutoUser;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.Colors;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;

public class AetherCommand {

	@Dependency
	EasterlynCore core;

	@Dependency
	EasterlynChat chat;

	@CommandAlias("aether")
	@CommandPermission("easterlyn.command.aether")
	@CommandRank(UserRank.ADMIN)
	public void aether(BukkitCommandIssuer issuer, @Single String name, String text) {
		Map<String, String> userData = new HashMap<>();
		userData.put("name", name);
		userData.put("color", issuer.isPlayer() ? core.getUserManager().getUser(issuer.getUniqueId()).getColor().name() : Colors.RANK_HEAD_ADMIN.name());
		Channel channel = chat.getChannels().get("aether");
		if (channel == null) {
			Bukkit.getServer().getPluginManager().callEvent(new ReportableEvent("Channel #aether not set up when executing /aether!"));
			return;
		}
		new UserChatEvent(new AetherUser(userData), channel, text).send(EasterlynChat.DEFAULT.getMembers());
	}

	class AetherUser extends AutoUser {
		AetherUser(Map<String, String> userData) {
			super(core, userData);
		}

		@Override
		public TextComponent getMention() {
			TextComponent component = new TextComponent("@" + getDisplayName());
			component.setColor(getColor().asBungee());
			component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://easterlyn.com/discord.html"));

			TextComponent line = new TextComponent("#main");
			line.setColor(Colors.CHANNEL.asBungee());
			TextComponent extra = new TextComponent("on Discord");
			extra.setColor(ChatColor.WHITE);
			line.addExtra(extra);
			line.addExtra(extra);
			component.addExtra(line);
			return component;
		}
	}

}
