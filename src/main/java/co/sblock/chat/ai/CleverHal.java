package co.sblock.chat.ai;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.NickChannel;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.micromodules.Cooldowns;
import co.sblock.utilities.DummyPlayer;
import co.sblock.utilities.JSONUtil;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * A CleverBot implementation using <a
 * href=https://github.com/pierredavidbelanger/chatter-bot-api>chatter-bot-api</a>.
 * 
 * @author Jikoo
 */
public class CleverHal extends HalMessageHandler {

	private final Cooldowns cooldowns;
	private final Pattern anyPattern, exactPattern, whitespacePattern;
	private final ChatterBotSession bot;
	private final Set<Pattern> ignoreMatches;
	private final BaseComponent[] hover;
	private final MessageBuilder noSpam;
	private final DummyPlayer dummy;

	public CleverHal(Sblock plugin) {
		super(plugin);
		this.cooldowns = plugin.getModule(Cooldowns.class);
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
		ignoreMatches.add(Pattern.compile("(baby|bang|due|fuck|pregnant|r(@|4|a)pe|sex)\\s?", Pattern.CASE_INSENSITIVE));
		ignoreMatches.add(Pattern.compile("mo(ther|m+y?\\s?)", Pattern.CASE_INSENSITIVE));
		ignoreMatches.add(Pattern.compile("dad+y?\\s?", Pattern.CASE_INSENSITIVE));

		hover = TextComponent.fromLegacyText(ChatColor.RED + "Artificial Intelligence\n"
				+ Color.BAD_EMPHASIS + "Sblock is not responsible\n" + Color.BAD_EMPHASIS
				+ "for anything Hal says.\n\n" + Color.BAD_EMPHASIS + "Unless it's awesome.\n\n"
				+ Color.COMMAND + "/join #halchat" + Color.BAD + " to spam usage.");

		noSpam = getHalBase().setMessage(JSONUtil.fromLegacyText(ChatColor.RED + "To spam with me, join #halchat."));

		dummy = new DummyPlayer();
	}

	private MessageBuilder getHalBase() {
		return new MessageBuilder(getPlugin()).setSender(ChatColor.DARK_RED + getPlugin().getBotName())
				.setNameClick("/join #halchat").setNameHover(hover).setChannelClick("@#halchat ");
	}

	@Override
	public boolean handleMessage(Message msg, Collection<Player> recipients) {
		if (bot == null || msg.getChannel() instanceof NickChannel) {
			return true;
		}
		String message = ChatColor.stripColor(msg.getMessage());
		if (!whitespacePattern.matcher(message).find()) {
			return true;
		}
		if (exactPattern.matcher(message).matches()) {
			// Set sender on fire or some shit
			if (msg.getSender() != null) {
				this.getHalBase().setChannel(msg.getChannel()).setMessage("What?").toMessage()
						.send(ImmutableList.of(msg.getSender()), false, false);
			}
			return true;
		}
		String channel = msg.getChannel().getName();
		if (!channel.equals("#halchat")) {
			if (cooldowns.getGlobalRemainder("cleverhal" + channel) > 0) {
				if (msg.getSender() == null) {
					return true;
				}
				// Spammy, warn a bitch
				noSpam.setChannel(msg.getChannel()).toMessage()
						.send(ImmutableList.of(msg.getSender().getPlayer()));
				Logger.getLogger("CleverHal").info("Warned " + msg.getSender().getPlayerName() + " about spamming Hal");
			} else {
				cooldowns.addGlobalCooldown("cleverhal" + channel, 2500L);
			}
			if (cooldowns.getGlobalRemainder("pendinghal" + channel) > 0) {
				return true;
			}
			cooldowns.addGlobalCooldown("pendinghal" + channel, 3000L);
		}
		HashSet<UUID> recipientUUIDs = new HashSet<>();
		recipients.forEach(player -> recipientUUIDs.add(player.getUniqueId()));
		triggerResponse(recipientUUIDs, msg.parseReplyChannel(), message);
		return true;
	}
	public void triggerResponse(final Channel channel, final String message) {
		triggerResponse(channel.getListening(), channel, message);
	}

	public void triggerResponse(final Collection<UUID> recipients, final Channel channel, final String message) {
		new BukkitRunnable() {
			@Override
			public void run() {
				String msg = message;
				for (Pattern pattern : ignoreMatches) {
					msg = pattern.matcher(msg).replaceAll("");
				}
				if (msg.isEmpty()) {
					msg = "I am playing on Sblock";
				}
				try {
					msg = bot.think(msg);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				Message message = getHalBase().setChannel(channel)
						.setMessage(ChatColor.RED + msg).toMessage();
				Set<Player> players = new HashSet<>();
				recipients.forEach(uuid -> {
					Player player = Bukkit.getPlayer(uuid);
					if (player != null && player.isOnline()) {
						players.add(player);
					}
				});
				Bukkit.getPluginManager().callEvent(new SblockAsyncChatEvent(true, dummy, players, message, false));
				cooldowns.clearGlobalCooldown("pendinghal" + channel.getName());
			}
		}.runTaskAsynchronously(getPlugin());
	}
}
