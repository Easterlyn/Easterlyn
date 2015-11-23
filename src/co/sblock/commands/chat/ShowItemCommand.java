package co.sblock.commands.chat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.commands.SblockCommand;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.JSONUtil;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * SblockCommand for displaying item in hand to the server.
 * 
 * @author Jikoo
 */
public class ShowItemCommand extends SblockCommand {

	public ShowItemCommand(Sblock plugin) {
		super(plugin, "show");
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
		ItemStack hand = player.getItemInHand();

		if (hand == null || !hand.hasItemMeta() || !hand.getItemMeta().hasDisplayName() || !hand.getItemMeta().hasEnchants()) {
			sender.sendMessage(Color.BAD + "You do not have anything named and enchanted in your hand!");
			return true;
		}

		OfflineUser user = Users.getGuaranteedUser(((Sblock) getPlugin()), player.getUniqueId());

		TextComponent item = new TextComponent(JSONUtil.fromLegacyText(hand.getItemMeta().getDisplayName()));
		item.setColor(ChatColor.AQUA);
		item.setHoverEvent(JSONUtil.getItemHover(hand));

		MessageBuilder builder = new MessageBuilder((Sblock) getPlugin()).setSender(user)
				.setThirdPerson(true)
				.setMessage(new TextComponent("shows off "), item, new TextComponent("."));

		if (!builder.canBuild(true) || !builder.isSenderInChannel(true)) {
			return true;
		}

		Message message = builder.toMessage();

		Set<Player> players = new HashSet<>();
		message.getChannel().getListening().forEach(uuid -> players.add(Bukkit.getPlayer(uuid)));

		Bukkit.getPluginManager().callEvent(new SblockAsyncChatEvent(false, player, players, message));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
