package co.sblock.chat.ai;

import java.util.Collection;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;

import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.utilities.JSONUtil;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * HalMessageHandler for math functions.
 * 
 * @author Jikoo
 */
public class Halculator extends HalMessageHandler {

	private final BaseComponent[] hover;

	public Halculator() {
		hover = JSONUtil.fromLegacyText(ChatColor.RED + "Calculator\n"
				+ ChatColor.DARK_RED + "For long or multiple\nequations, use /halc");
	}

	public String evhaluate(String input) {
		input = input.toLowerCase().replace('x', '*');
		try {
			double ans = Expression.compile(input).evaluate();
			String answer;
			if (ans == (long) ans) {
				answer = String.format("%d", (long) ans);
			} else {
				answer = String.format("%s", ans);
			}
			return input + " = " + answer;
		} catch (ExpressionException e) {
			if (input.matches("\\A.*m(y|[aeu]h?).*((di|co)c?k|pe(en|nis)|(we[ie]n|(schl|d)ong)(er)?|willy|(trouser ?)?snake|lizard).*\\Z")) {
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
		String msg = ChatColor.stripColor(message.getMessage()).toLowerCase();
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
