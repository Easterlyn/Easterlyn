package com.easterlyn.chat.listener;

import com.easterlyn.EasterlynChat;
import com.easterlyn.EasterlynCore;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.event.UserChatEvent;
import com.easterlyn.event.UserCreationEvent;
import com.easterlyn.user.AutoUser;
import com.easterlyn.user.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class ChannelManagementListener implements Listener {

	private final EasterlynChat chat;

	public ChannelManagementListener(EasterlynChat chat) {
		this.chat = chat;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		RegisteredServiceProvider<EasterlynCore> easterlynRSP = chat.getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (easterlynRSP == null) {
			event.getPlayer().sendMessage("Easterlyn core plugin is not enabled! Please report this to @Staff on Discord immediately!");
			return;
		}

		event.setCancelled(true);

		User user = easterlynRSP.getProvider().getUserManager().getUser(event.getPlayer().getUniqueId());
		event.getPlayer().setDisplayName(user.getDisplayName());

		Channel channel = null;

		// #channel message parsing
		if (event.getMessage().length() > 0 && event.getMessage().charAt(0) == '#') {
			int space = event.getMessage().indexOf(' ');
			if (space == -1) {
				space = event.getMessage().length();
			}
			String channelName = event.getMessage().substring(1, space);
			if (space == event.getMessage().length()) {
				user.sendMessage("What are you trying to say in #" + channelName + "?");
				return;
			}
			channel = chat.getChannels().get(channelName);
			if (channel == null) {
				user.sendMessage("Invalid channel. Create it with `/channel create #" + channelName + "`!");
				return;
			}
			event.setMessage(event.getMessage().substring(space));
		}

		// User's channel
		if (channel == null) {
			channel = chat.getChannels().get(user.getStorage().getString(EasterlynChat.USER_CURRENT));
			if (channel == null) {
				user.sendMessage("No current channel set! Focus on the main channel with `/join #`!");
				return;
			}
		}

		new UserChatEvent(user, channel, event.getMessage()).send();
	}

	@EventHandler
	public void onUserCreate(UserCreationEvent event) {
		RegisteredServiceProvider<EasterlynCore> easterlynProvider = chat.getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (easterlynProvider != null) {
			ConfigurationSection userSection = chat.getConfig().getConfigurationSection("auto_user");
			Map<String, String> userData = new HashMap<>();
			if (userSection != null) {
				userSection.getKeys(false).forEach(key -> userData.put(key, userSection.getString(key)));
			}
			new UserChatEvent(new AutoUser(easterlynProvider.getProvider(), userData), EasterlynChat.DEFAULT,
					event.getUser().getDisplayName() + " is new! Please welcome them.");
		}
		event.getUser().getStorage().set(EasterlynChat.USER_CURRENT, EasterlynChat.DEFAULT.getName());
		event.getUser().getStorage().set(EasterlynChat.USER_CHANNELS, Collections.singletonList(EasterlynChat.DEFAULT.getName()));
		EasterlynChat.DEFAULT.getMembers().add(event.getUser().getUniqueId());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		RegisteredServiceProvider<EasterlynCore> easterlynProvider = chat.getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (easterlynProvider == null) {
			return;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		User user = easterlynProvider.getProvider().getUserManager().getUser(event.getPlayer().getUniqueId());
		event.getPlayer().setDisplayName(user.getDisplayName());
		String joinMessage = user.getDisplayName() + " joined {channels}at " + dateFormat.format(new Date());

		List<String> savedChannels = user.getStorage().getStringList(EasterlynChat.USER_CHANNELS);
		List<String> channels = savedChannels.stream().filter(channelName -> {
			Channel channel = chat.getChannels().get(channelName);
			if (channel == null) {
				return false;
			}
			if (!channel.isPrivate() || channel.isWhitelisted(user)) {
				channel.getMembers().add(user.getUniqueId());
				return true;
			}
			return false;
		}).collect(Collectors.toList());

		if (channels.size() != savedChannels.size()) {
			user.getStorage().set(EasterlynChat.USER_CHANNELS, channels);
			if (!channels.contains(user.getStorage().getString(EasterlynChat.USER_CURRENT))) {
				user.getStorage().set(EasterlynChat.USER_CURRENT, null);
			}
		}

		event.setJoinMessage("");

		chat.getServer().getOnlinePlayers().forEach(player -> {
			User otherUser;
			if (player.getUniqueId().equals(user.getUniqueId())) {
				otherUser = user;
			} else {
				otherUser = easterlynProvider.getProvider().getUserManager().getUser(player.getUniqueId());
			}

			List<String> commonChannels = new ArrayList<>(otherUser.getStorage().getStringList(EasterlynChat.USER_CHANNELS));
			commonChannels.retainAll(channels);
			StringBuilder commonBuilder = new StringBuilder();
			Iterator<String> channelIterator = commonChannels.iterator();

			while (channelIterator.hasNext()) {
				String channelName = channelIterator.next();
				if (!channelIterator.hasNext() && commonBuilder.length() > 0) {
					commonBuilder.append("and ");
				}
				commonBuilder.append('#').append(channelName);
				if (channelIterator.hasNext()) {
					commonBuilder.append(',');
				}
				commonBuilder.append(' ');
			}

			// TODO construct rich instead? Not hard, just annoying.
			otherUser.sendMessage(joinMessage.replace("{channels}", commonBuilder.toString()));
		});
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		RegisteredServiceProvider<EasterlynCore> easterlynProvider = chat.getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (easterlynProvider == null) {
			return;
		}

		User user = easterlynProvider.getProvider().getUserManager().getUser(event.getPlayer().getUniqueId());
		List<String> savedChannels = user.getStorage().getStringList(EasterlynChat.USER_CHANNELS);

		List<String> channels = savedChannels.stream().filter(channelName -> {
			Channel channel = chat.getChannels().get(channelName);
			if (channel == null) {
				return false;
			}
			channel.getMembers().remove(user.getUniqueId());
			return true;
		}).collect(Collectors.toList());

		if (channels.size() != savedChannels.size()) {
			user.getStorage().set(EasterlynChat.USER_CHANNELS, channels);
			if (!channels.contains(user.getStorage().getString(EasterlynChat.USER_CURRENT))) {
				user.getStorage().set(EasterlynChat.USER_CURRENT, null);
			}
		}
	}

}
