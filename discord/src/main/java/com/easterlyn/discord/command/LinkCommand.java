package com.easterlyn.discord.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.EasterlynDiscord;
import com.easterlyn.util.StringUtil;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("link")
@Description("{@@discord.commands.link.description.self}")
@CommandPermission("easterlyn.command.link.self")
public class LinkCommand extends BaseCommand {

	@Dependency
	private EasterlynCore core;
	@Dependency
	private EasterlynDiscord discord;

	@Default
	@Private
	public void link(Player issuer) {
		String pendingLink = discord.getPendingLink(issuer.getUniqueId());
		String message = core.getLocaleManager().getValue("discord.commands.link.code", core.getLocaleManager().getLocale(issuer),
				"{value}", pendingLink);
		TextComponent container = new TextComponent();
		for (TextComponent component : StringUtil.toJSON(message)) {
			container.addExtra(component);
		}
		container.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, discord.getCommandPrefix() + "link " + pendingLink));
		message = core.getLocaleManager().getValue("discord.commands.link.copy_hint", core.getLocaleManager().getLocale(issuer));
		container.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(StringUtil.toJSON(message).toArray(BaseComponent[]::new))));
		issuer.spigot().sendMessage(container);
	}

	@CommandAlias("link")
	@Description("{@@discord.commands.link.description.force}")
	@CommandPermission("easterlyn.command.link.other")
	@Syntax("<uuid> <snowflake>")
	@CommandCompletion("")
	public void link(CommandSender issuer, UUID uuid, @Single String identifier) {
		discord.getServer().getScheduler().runTaskAsynchronously(discord, () ->
				discord.getFirstMemberSnowflake(identifier).doOnSuccess(snowflake -> {
			if (snowflake != null) {
				discord.addLink(uuid, snowflake);
				core.getLocaleManager().sendMessage(issuer, "discord.command.link.force.success");
			} else {
				core.getLocaleManager().sendMessage(issuer, "discord.command.link.force.failure");
			}
		}));
	}

	@Private
	@CommandAlias("link")
	public void link(Player issuer, @SuppressWarnings("unused") String assistMeICannotBeReliedUponToRead) {
		core.getLocaleManager().sendMessage(issuer, "discord.commands.link.reading_comprehension");
		link(issuer);
	}

}
