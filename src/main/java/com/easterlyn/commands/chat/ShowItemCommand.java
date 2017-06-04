package com.easterlyn.commands.chat;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.message.MessageBuilder;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.events.event.EasterlynAsyncChatEvent;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.JSONUtil;
import com.easterlyn.utilities.TextUtils;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * EasterlynCommand for displaying item in hand to the server.
 * 
 * @author Jikoo
 */
public class ShowItemCommand extends EasterlynCommand {

	private final Users users;

	public ShowItemCommand(Easterlyn plugin) {
		super(plugin, "show");
		this.setAliases("showitem");
		this.users = plugin.getModule(Users.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}

		Player player = (Player) sender;
		ItemStack hand = player.getInventory().getItemInMainHand();

		ItemMeta handMeta;
		if (hand == null || !hand.hasItemMeta()
				|| !(handMeta = hand.getItemMeta()).hasDisplayName() || !handMeta.hasEnchants()) {
			sender.sendMessage(getLang().getValue("command.show.requirements"));
			return true;
		}

		User user = users.getUser(player.getUniqueId());
		Channel channel;
		if (args.length > 0) {
			channel = ((Easterlyn) this.getPlugin()).getModule(Chat.class).getChannelManager().getChannel(args[0]);
			if (channel == null) {
				sender.sendMessage(getLang().getValue("chat.error.invalidChannel").replace("{CHANNEL}", args[0]));
			}
		} else {
			channel = user.getCurrentChannel();
		}

		if (!player.hasPermission("easterlyn.chat.unfiltered") && "#".equals(user.getCurrentChannel().getName())
				&& !TextUtils.isOnlyAscii(handMeta.getDisplayName())) {
			sender.sendMessage(getLang().getValue("command.show.invalidCharacters"));
		}

		MessageBuilder builder = new MessageBuilder((Easterlyn) getPlugin()).setChannel(channel)
				.setSender(user).setThirdPerson(true).setMessage(new TextComponent("shows off "),
						JSONUtil.getItemComponent(hand), new TextComponent("."));

		if (!builder.canBuild(true) || !builder.isSenderInChannel(true)) {
			return true;
		}

		Bukkit.getPluginManager().callEvent(new EasterlynAsyncChatEvent(false, player, builder.toMessage()));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
