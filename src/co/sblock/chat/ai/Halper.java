package co.sblock.chat.ai;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.Color;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;

import net.md_5.bungee.api.ChatColor;

/**
 * HalMessageHandler for replying to the channel #help.
 * 
 * @author Jikoo
 */
public class Halper extends HalMessageHandler {

	private final HashMap<Pattern, Message> responses;

	public Halper() {

		// Create ItemStack for all hover text
		ItemStack hover = new ItemStack(Material.DIODE);
		ItemMeta meta = hover.getItemMeta();
		meta.setDisplayName(Color.GOOD + "    Automated Help");
		meta.setLore(Arrays.asList("", Color.GOOD + "  Please " + Color.COMMAND + "/report" + Color.GOOD + " any",
				Color.GOOD + "suggestions to improve",
				Color.GOOD + "   these responses.   "));
		hover.setItemMeta(meta);

		MessageBuilder builder = new MessageBuilder().setSender(ChatColor.DARK_RED + "Lil Hal")
				.setChannel(ChannelManager.getChannelManager().getChannel("#help"));

		responses = new HashMap<>();

		// Match "create my own channel" etc.
		builder.setMessage(ChatColor.RED + "To create a channel, use " + Color.COMMAND + "/channel new" + ChatColor.RED
				+ ". For example, " + Color.COMMAND + "/channel new #WeLoveHal PUBLIC NORMAL" + ChatColor.RED
				+ ". Press tab to auto-complete options if you're confused!");
		responses.put(Pattern.compile("(start|make|create).*cha(t|nnel)"), builder.toMessage());

		// Match "my stuff was griefed" etc.
		builder.setMessage(ChatColor.RED + "Please " + Color.COMMAND + "/report" + ChatColor.RED
				+ " any suspected grief - a moderator will be able to check it out later!");
		responses.put(Pattern.compile("(grief|br(o|ea)k|gone|st(ea|o)l|missing)"), builder.toMessage());

		// Match anything, should be added last.
		builder.setMessage(ChatColor.RED + "Sorry, I don't have any matching help. Help us out by "
				+ Color.COMMAND + "/report" + ChatColor.RED + "ing if you feel it's necessary!");
		responses.put(Pattern.compile("."), builder.toMessage());
	}

	/**
	 * Handles and responds to messages sent to the channel #help.
	 * 
	 * @see co.sblock.chat.ai.HalMessageHandler#handleMessage(co.sblock.chat.message.Message, java.util.Collection)
	 */
	@Override
	public boolean handleMessage(Message message, Collection<Player> recipients) {
		if (!message.getChannel().getName().equals("#help")) {
			return false;
		}
		String msg = ChatColor.stripColor(message.getMessage()).toLowerCase();
		for (Entry<Pattern, Message> entry : responses.entrySet()) {
			if (entry.getKey().matcher(msg).find()) {
				entry.getValue().send(recipients);
				return true;
			}
		}
		return true;
	}

}
