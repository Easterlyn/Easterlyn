package com.easterlyn.chat.ai;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.NickChannel;
import com.easterlyn.chat.message.Message;
import com.easterlyn.chat.message.MessageBuilder;
import com.easterlyn.events.event.SblockAsyncChatEvent;
import com.easterlyn.micromodules.Cooldowns;
import com.easterlyn.utilities.DummyPlayer;
import com.easterlyn.utilities.JSONUtil;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
	private final Language lang;
	private final Pattern anyPattern, exactPattern, whitespacePattern;
	private final Set<Pattern> ignoreMatches;
	private final BaseComponent[] hover;
	private final MessageBuilder noSpam;
	private final DummyPlayer dummy;

	private ChatterBotSession bot;

	public CleverHal(Easterlyn plugin) {
		super(plugin);
		this.cooldowns = plugin.getModule(Cooldowns.class);
		this.lang = plugin.getModule(Language.class);
		
		try {
			ChatterBot chatterBot = new ChatterBotFactory().create(ChatterBotType.CLEVERBOT);
			new BukkitRunnable() {
				@Override
				public void run() {
					if (chatterBot != null) {
						bot = chatterBot.createSession();
					}
				}
			}.runTaskAsynchronously(getPlugin());
		} catch (Exception e) {
			e.printStackTrace();
		}


		anyPattern = Pattern.compile(lang.getValue("chat.ai.cleverbot.trigger"), Pattern.CASE_INSENSITIVE);
		exactPattern = Pattern.compile('^' + anyPattern.pattern() + '$', anyPattern.flags());
		whitespacePattern = Pattern.compile("(^|\\W)" + anyPattern.pattern() + "(\\W|$)", anyPattern.flags());

		ignoreMatches = new HashSet<>();
		for (String split : lang.getValue("chat.ai.cleverbot.ignore").split("\\n")) {
			ignoreMatches.add(Pattern.compile(split, Pattern.CASE_INSENSITIVE));
		}

		hover = TextComponent.fromLegacyText(lang.getValue("chat.ai.cleverbot.hover"));

		noSpam = getHalBase().setMessage(JSONUtil.fromLegacyText(Language.getColor("bot_text")
				+ "To spam with me, join " + lang.getValue("chat.spamChannel") + "."));

		dummy = new DummyPlayer();
	}

	private MessageBuilder getHalBase() {
		String spamChannel = lang.getValue("chat.spamChannel");
		return new MessageBuilder(getPlugin()).setSender(lang.getValue("chat.ai.cleverbot.name"))
				.setNameClick("/join " + spamChannel).setNameHover(hover).setChannelClick("@" + spamChannel + " ");
	}

	@Override
	public boolean handleMessage(Message msg, Collection<Player> recipients) {
		if (bot == null || msg.getChannel() instanceof NickChannel) {
			return true;
		}
		String message = net.md_5.bungee.api.ChatColor.stripColor(msg.getMessage());
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
					msg = "I am playing Minecraft.";
				}
				try {
					msg = bot.think(msg);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				Message message = getHalBase().setChannel(channel)
						.setMessage(Language.getColor("bot_text") + msg).toMessage();
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
