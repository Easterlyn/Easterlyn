package co.sblock.commands.admin;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Chat;
import co.sblock.chat.Language;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.commands.SblockCommand;

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

	public RSSUpdateCommand(Sblock plugin) {
		super(plugin, "rssupdate");
		this.setDescription("Announce an RSS feed update.");
		this.setUsage("/rssupdate <feed name> <url> <title>");
		this.setPermissionLevel("horrorterror");
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
						+ Language.getColor("link_format") + args[0]));
		for (BaseComponent component : text) {
			component.setClickEvent(click);
			component.setHoverEvent(hover);
		}

		new MessageBuilder((Sblock) getPlugin()).setSender(Language.getColor("neutral") + args[0])
				.setNameHover(Language.getColor("neutral") + "RSS Feed")
				.setChannel(((Sblock) getPlugin()).getModule(Chat.class).getChannelManager().getChannel("#"))
				.setMessage(new TextComponent(text)).toMessage().send(Bukkit.getOnlinePlayers(), false);

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
