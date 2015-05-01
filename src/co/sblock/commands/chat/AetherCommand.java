package co.sblock.commands.chat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.commands.SblockCommand;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.Users;
import co.sblock.utilities.player.DummyPlayer;

/**
 * SblockCommand for /aether, the command executed to make IRC chat mimic normal channels.
 * 
 * @author Jikoo
 */
public class AetherCommand extends SblockCommand {

	private final ItemStack hover;

	public AetherCommand() {
		super("aether");
		this.setDescription("For usage in console largely. Talks in #Aether.");
		this.setUsage("/aether <text>");
		this.setPermissionLevel("horrorterror");
		this.setPermissionMessage("The aetherial realm eludes your grasp once more.");

		hover = new ItemStack(Material.WEB);
		ItemMeta hoverMeta = hover.getItemMeta();
		hoverMeta.setDisplayName(ChatColor.WHITE + "IRC Chat");
		hoverMeta.setLore(Arrays.asList(new String[] {
				ChatColor.GRAY + "Server: irc.freenode.net",
				ChatColor.GRAY + "Channel: #sblockserver" }));
		hover.setItemMeta(hoverMeta);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Hey Adam, stop faking empty IRC messages.");
			return true;
		}
		Message message = new MessageBuilder().setSender(ChatColor.WHITE + args[0])
				.setMessage(StringUtils.join(args, ' ', 1, args.length))
				.setChannel(ChannelManager.getChannelManager().getChannel("#Aether"))
				.setChannelClick("@# ").setNameClick("@# ").setNameHover(hover).toMessage();

		Set<Player> players = new HashSet<Player>(Bukkit.getOnlinePlayers());
		players.removeIf(p -> Users.getGuaranteedUser(p.getUniqueId()).getSuppression());

		// CHAT: DummyPlayer's sender is always CONSOLE
		Bukkit.getPluginManager().callEvent(new SblockAsyncChatEvent(false, new DummyPlayer(sender), players, message));

		return true;
	}
}
