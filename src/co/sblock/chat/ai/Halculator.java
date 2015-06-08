package co.sblock.chat.ai;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;

import net.md_5.bungee.api.ChatColor;

/**
 * 
 * 
 * @author Jikoo
 */
public class Halculator extends HalMessageHandler {

	private final com.fathzer.soft.javaluator.DoubleEvaluator eval;
	private final ItemStack hover;

	public Halculator() {
		eval = new com.fathzer.soft.javaluator.DoubleEvaluator();
		hover = new ItemStack(Material.REDSTONE_COMPARATOR);
		ItemMeta meta = hover.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Calculator");
		meta.setLore(Arrays.asList(new String[] {ChatColor.DARK_RED + "For long or multiple", ChatColor.DARK_RED + "equations, use /halc"}));
		hover.setItemMeta(meta);
	}

	public String evhaluate(String input) {
		input = input.toLowerCase().replace('x', '*');
		try {
			double ans = eval.evaluate(input);
			String answer;
			// Remove trailing zeroes without precision loss
			if (ans == (long) ans) {
				answer = String.format("%d", (long) ans);
			} else {
				answer = String.format("%s", ans);
			}
			return input + " = " + answer;
		} catch (IllegalArgumentException e) {
			if (input.matches("\\A.*m(y|[aeu]h?).*((di|co)c?k|pe(en|nis)|(we[ie]n|(schl|d)ong)(er)?|willy|(trouser )?snake|lizard).*\\Z")) {
				return "Sorry, your equation is too tiny for me to read.";
			} else if (input.matches("\\A.*life.*universe.*everything*\\Z")) {
				return input + " = 42";
			} else {
				return "Sorry, I can't read that equation!";
			}
		}
	}

	@Override
	public boolean handleMessage(Message message, Collection<Player> recipients) {
		String msg = message.getCleanedMessage().toLowerCase();
		if (!msg.startsWith("halc ") && !msg.startsWith("halculate ") && !msg.startsWith("evhal ") && !msg.startsWith("evhaluate ")) {
			return false;
		}
		msg = msg.substring(msg.indexOf(' ')).trim();
		final Message hal = new MessageBuilder().setSender(ChatColor.DARK_RED + "Lil Hal")
				.setMessage(ChatColor.RED + evhaluate(msg)).setChannel(message.getChannel())
				.setNameHover(hover).setNameClick("/halc ").toMessage();
		hal.send(recipients);
		return true;
	}
}
