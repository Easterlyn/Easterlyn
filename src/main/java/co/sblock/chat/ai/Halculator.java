package co.sblock.chat.ai;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.utilities.JSONUtil;

import net.md_5.bungee.api.ChatColor;

/**
 * HalMessageHandler for math functions.
 * 
 * @author Jikoo
 */
public class Halculator extends HalMessageHandler {

	private final MessageBuilder hal;
	private final Map<Pattern, String> customReplies;
	private final Map<UUID, Double> lastAnswer;

	public Halculator(Sblock plugin) {
		super(plugin);
		Language lang = plugin.getModule(Language.class);
		this.hal = new MessageBuilder(plugin).setSender(lang.getValue("chat.ai.calculator.name")).setNameClick("/halc ")
				.setNameHover(JSONUtil.fromLegacyText(lang.getValue("chat.ai.calculator.hover")));
		this.customReplies = new HashMap<>();
		for (String reply : lang.getValue("chat.ai.calculator.replies").split("\n")) {
			String[] split = reply.split("\\{RESPONSE\\}");
			customReplies.put(Pattern.compile(split[0], Pattern.CASE_INSENSITIVE), split[1]);
		}
		this.lastAnswer = new HashMap<>();
	}

	public String evhaluate(UUID uuid, String input) {
		input = input.toLowerCase().replace('x', '*');
		try {
			if (uuid != null && this.lastAnswer.containsKey(uuid)) {
				input = input.replace("ans", String.valueOf(this.lastAnswer.get(uuid)));
			}
			double ans = Expression.compile(input).evaluate();
			if (uuid != null) {
				lastAnswer.put(uuid, ans);
			}
			String answer;
			if (ans == (int) ans) {
				answer = String.format("%d", (int) ans);
			} else {
				answer = String.format("%s", ans);
			}
			return input + " = " + answer;
		} catch (ExpressionException e) {
			for (Entry<Pattern, String> entry : this.customReplies.entrySet()) {
				if (entry.getKey().matcher(input).find()) {
					return entry.getValue().replace("{INPUT}", input);
				}
			}
			return "Sorry, I can't read that equation!";
		}
	}

	@Override
	public boolean handleMessage(Message message, Collection<Player> recipients) {
		String msg = ChatColor.stripColor(message.getMessage()).toLowerCase();
		if (!msg.startsWith("halc ") && !msg.startsWith("halculate ") && !msg.startsWith("evhal ") && !msg.startsWith("evhaluate ")) {
			return false;
		}
		msg = msg.substring(msg.indexOf(' ')).trim();
		UUID uuid = message.getSender() != null ? message.getSender().getUUID() : null;
		final Message halMsg = hal.setMessage(ChatColor.RED + evhaluate(uuid, msg))
				.setChannel(message.getChannel()).toMessage();
		halMsg.send(recipients);
		return true;
	}
}
