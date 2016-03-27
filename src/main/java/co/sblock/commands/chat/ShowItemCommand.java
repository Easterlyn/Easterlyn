package co.sblock.commands.chat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Chat;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.commands.SblockCommand;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.User;
import co.sblock.users.Users;
import co.sblock.utilities.JSONUtil;
import co.sblock.utilities.TextUtils;

import net.md_5.bungee.api.chat.TextComponent;

/**
 * SblockCommand for displaying item in hand to the server.
 * 
 * @author Jikoo
 */
public class ShowItemCommand extends SblockCommand {

	private final Users users;

	public ShowItemCommand(Sblock plugin) {
		super(plugin, "show");
		this.users = plugin.getModule(Users.class);
		this.setAliases("showitem");
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
			channel = ((Sblock) this.getPlugin()).getModule(Chat.class).getChannelManager().getChannel(args[0]);
			if (channel == null) {
				sender.sendMessage(getLang().getValue("chat.error.invalidChannel").replace("{CHANNEL}", args[0]));
			}
		} else {
			channel = user.getCurrentChannel();
		}

		if (!player.hasPermission("sblock.felt") && "#".equals(user.getCurrentChannel())
				&& !TextUtils.isOnlyAscii(handMeta.getDisplayName())) {
			sender.sendMessage(getLang().getValue("command.show.invalidCharacters"));
		}

		MessageBuilder builder = new MessageBuilder((Sblock) getPlugin()).setChannel(channel)
				.setSender(user).setThirdPerson(true).setMessage(new TextComponent("shows off "),
						JSONUtil.getItemComponent(hand), new TextComponent("."));

		if (!builder.canBuild(true) || !builder.isSenderInChannel(true)) {
			return true;
		}

		Bukkit.getPluginManager().callEvent(new SblockAsyncChatEvent(false, player, builder.toMessage()));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
