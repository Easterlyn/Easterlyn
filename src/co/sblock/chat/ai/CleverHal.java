package co.sblock.chat.ai;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.NickChannel;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.users.OfflineUser;
import co.sblock.utilities.Cooldowns;
import co.sblock.utilities.JSONUtil;
import co.sblock.utilities.RegexUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * A CleverBot implementation using <a
 * href=https://github.com/pierredavidbelanger/chatter-bot-api>chatter-bot-api</a>.
 * 
 * @author Jikoo
 */
public class CleverHal implements HalMessageHandler {

	private final Pattern anyPattern, exactPattern, whitespacePattern;
	private final ChatterBotSession bot;
	private final Set<Pattern> ignoreMatches;
	private final BaseComponent[] hover;
	private final MessageBuilder noSpam;

	public CleverHal() {
		ChatterBot chatterBot = null;
		try {
			chatterBot = new ChatterBotFactory().create(ChatterBotType.CLEVERBOT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (chatterBot != null) {
			bot = chatterBot.createSession();
		} else {
			bot = null;
		}

		anyPattern = Pattern.compile("(hal|dirk)", Pattern.CASE_INSENSITIVE);
		exactPattern = Pattern.compile('^' + anyPattern.pattern() + '$', anyPattern.flags());
		whitespacePattern = Pattern.compile("(^|\\W)" + anyPattern.pattern() + "(\\W|$)", anyPattern.flags());

		ignoreMatches = new HashSet<>();
		ignoreMatches.add(whitespacePattern);
		ignoreMatches.add(RegexUtils.URL_PATTERN);
		ignoreMatches.add(Pattern.compile("^.*b(e|a)n(j(ie|y))? ?z(u|e)?rf(les?)?.*$", Pattern.CASE_INSENSITIVE));
		ignoreMatches.add(Pattern.compile("^halc(ulate)? .*$", Pattern.CASE_INSENSITIVE));
		ignoreMatches.add(Pattern.compile("^evhal(uate)? .*$", Pattern.CASE_INSENSITIVE));
		ignoreMatches.add(Pattern.compile("^.*dad(dy)?.*$", Pattern.CASE_INSENSITIVE));

		hover = TextComponent.fromLegacyText(ChatColor.RED + "Artificial Intelligence\n"
				+ Color.BAD_EMPHASIS + "Sblock is not responsible\n" + Color.BAD_EMPHASIS
				+ "for anything Hal says.\n\n" + Color.BAD_EMPHASIS + "Unless it's awesome.\n\n"
				+ Color.COMMAND + "/join #halchat" + Color.BAD + " to spam usage.");

		noSpam = new MessageBuilder().setSender(ChatColor.DARK_RED + "Lil Hal")
				.setNameClick("/join #halchat").setNameHover(hover).setChannelClick("@#halchat ")
				.setMessage( JSONUtil.fromLegacyText(ChatColor.RED + "To spam with me, join #halchat."));
	}

	@Override
	public boolean handleMessage(Message msg, Collection<Player> recipients) {
		if (bot == null || msg.getSender() == null || msg.getChannel() instanceof NickChannel) {
			return true;
		}
		OfflineUser sender = msg.getSender();
		Player senderPlayer = sender.getPlayer();
		if (senderPlayer == null) {
			return true;
		}
		String message = ChatColor.stripColor(msg.getMessage());
		if (!whitespacePattern.matcher(message).find()) {
			return true;
		}
		if (exactPattern.matcher(message).matches()) {
			// Set sender on fire or some shit
			msg.getSender().sendMessage(Color.HAL.replaceFirst("#", msg.getChannel().getName()) + "What?");
			return true;
		}
		String channel = msg.getChannel().getName();
		if (!channel.equals("#halchat")) {
			
			Cooldowns cooldowns = Cooldowns.getInstance();
			if (cooldowns.getGlobalRemainder("pendinghal" + channel) > 0) {
				return true;
			}
			cooldowns.addGlobalCooldown("pendinghal" + channel, 3000L);
			if (cooldowns.getGlobalRemainder("cleverhal" + channel) > 0) {
				// Spammy, warn a bitch
				noSpam.setChannel(msg.getChannel());
				noSpam.toMessage().send(Arrays.asList(senderPlayer));
				Logger.getLogger("MegaHal").info("Warned " + msg.getSender().getPlayerName() + " about spamming Hal");
				return true;
			} else {
				cooldowns.addGlobalCooldown("cleverhal" + channel, 2500L);
			}
		}
		HashSet<UUID> recipientUUIDs = new HashSet<>();
		recipients.forEach(player -> recipientUUIDs.add(player.getUniqueId()));
		triggerResponse(recipientUUIDs, msg.getChannel(), message);
		return true;
	}
	public void triggerResponse(final Channel channel, final String message) {
		triggerResponse(channel.getListening(), channel, message);
	}

	public void triggerResponse(final Collection<UUID> recipients, final Channel channel, final String message) {
		new BukkitRunnable() {
			@Override
			public void run() {
				String msg;
				try {
					msg = bot.think(message);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				new MessageBuilder().setSender(ChatColor.DARK_RED + "Lil Hal")
						.setMessage(ChatColor.RED + msg).setChannel(channel)
						.setChannelClick("@#halchat ").setNameClick("/join #halchat")
						.setNameHover(hover).toMessage().send(recipients);
				Cooldowns.getInstance().clearGlobalCooldown("pendinghal" + channel.getName());
			}
		}.runTaskAsynchronously(Sblock.getInstance());
	}
}
