package co.sblock.commands.chat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.commands.SblockCommand;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.User;
import co.sblock.users.Users;
import co.sblock.utilities.JSONUtil;

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
		this.setDescription("Displays an item in chat.");
		this.setUsage("/show");
		this.setAliases("showitem");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}

		Player player = (Player) sender;
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (hand == null || !hand.hasItemMeta() || !hand.getItemMeta().hasDisplayName() || !hand.getItemMeta().hasEnchants()) {
			sender.sendMessage(Color.BAD + "You do not have anything named and enchanted in your main hand!");
			return true;
		}

		User user = users.getUser(player.getUniqueId());

		MessageBuilder builder = new MessageBuilder((Sblock) getPlugin())
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
