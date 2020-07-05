package com.easterlyn.chat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynChat;
import com.easterlyn.EasterlynCore;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.event.UserChatEvent;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.user.AutoUser;
import com.easterlyn.util.Colors;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class AetherCommand extends BaseCommand {

	@Dependency
	EasterlynCore core;

	@Dependency
	EasterlynChat chat;

	@CommandAlias("aether")
	@Description("{@@chat.commands.aether.description}")
	@CommandPermission("easterlyn.command.aether")
	@Syntax("<name> <message content>")
	@CommandCompletion("")
	public void aether(BukkitCommandIssuer issuer, @Single String name, String text) {
		Map<String, String> userData = new HashMap<>();
		userData.put("name", name);
		userData.put("color", issuer.isPlayer() ? core.getUserManager().getUser(issuer.getUniqueId()).getColor().name() : Colors.RANK_HEAD_ADMIN.name());
		Channel channel = chat.getChannels().get("aether");
		if (channel == null) {
			ReportableEvent.call("Channel #aether not set up when executing /aether!");
			return;
		}
		// TODO async
		new UserChatEvent(new AetherUser(userData), channel, text).send(EasterlynChat.DEFAULT.getMembers());
	}

	class AetherUser extends AutoUser {
		AetherUser(Map<String, String> userData) {
			super(core, userData);
		}

		@Override
		public TextComponent getMention() {
			TextComponent component = new TextComponent("@" + getDisplayName());
			component.setColor(getColor());
			component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://discord.easterlyn.com"));

			TextComponent line = new TextComponent("#main");
			line.setColor(Colors.CHANNEL);
			TextComponent extra = new TextComponent("on Discord");
			extra.setColor(ChatColor.WHITE);
			line.addExtra(extra);
			line.addExtra(extra);
			component.addExtra(line);
			return component;
		}
	}

}
