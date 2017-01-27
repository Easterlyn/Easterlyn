package com.easterlyn.commands.admin;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.Language;
import com.easterlyn.chat.message.MessageBuilder;
import com.easterlyn.commands.SblockCommand;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * SblockCommand for announcing RSS feed updates
 * 
 * @author Jikoo
 */
public class RSSUpdateCommand extends SblockCommand {

	public RSSUpdateCommand(Easterlyn plugin) {
		super(plugin, "rssupdate");
		this.setDescription("Announce an RSS feed update.");
		this.setPermissionLevel(UserRank.HORRORTERROR);
		this.setUsage("/rssupdate <feed name> <url> <title>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!getPlugin().getConfig().getBoolean("rss-enabled")) {
			return true;
		}
		if (args.length < 3) {
			return false;
		}

		BaseComponent[] text = TextComponent.fromLegacyText(
				Language.getColor("link_color").toString() + Language.getColor("link_format")
				+ StringUtils.join(args, ' ', 2, args.length));
		ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, args[1]);
		HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				TextComponent.fromLegacyText(Language.getColor("link_color").toString()
						+ Language.getColor("link_format") + args[1]));
		for (BaseComponent component : text) {
			component.setClickEvent(click);
			component.setHoverEvent(hover);
		}

		new MessageBuilder((Easterlyn) getPlugin()).setSender(Language.getColor("neutral") + args[0])
				.setNameHover(Language.getColor("neutral") + "RSS Feed")
				.setChannel(((Easterlyn) getPlugin()).getModule(Chat.class).getChannelManager().getChannel("#"))
				.setMessage(new TextComponent(text)).toMessage().send(Bukkit.getOnlinePlayers(), false);

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
