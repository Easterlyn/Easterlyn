package com.easterlyn.user;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import com.easterlyn.EasterlynCore;
import com.easterlyn.event.UserLoadEvent;
import com.easterlyn.event.UserUnloadEvent;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.PlayerUtil;
import com.easterlyn.util.StringUtil;
import com.easterlyn.util.event.SimpleListener;
import com.easterlyn.util.tuple.Pair;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Manager for loading users.
 *
 * @author Jikoo
 */
public class UserManager {

	private final LoadingCache<UUID, User> userCache;

	public UserManager(EasterlynCore plugin) {
		this.userCache = CacheBuilder.newBuilder()
				.expireAfterAccess(30L, TimeUnit.MINUTES)
				.removalListener(notification -> {
			User user = (User) notification.getValue();
			plugin.getServer().getPluginManager().callEvent(new UserUnloadEvent(user));
			user.save();
			PermissionUtil.releasePermissionData(user.getUniqueId());
		}).build(new CacheLoader<UUID, User>() {
			@Override
			public User load(@NotNull final UUID uuid) {
				User user = User.load(plugin, uuid);
				plugin.getServer().getPluginManager().callEvent(new UserLoadEvent(user));
				return user;
			}
		});

		PlayerQuitEvent.getHandlerList().register(new SimpleListener<>(PlayerQuitEvent.class,
				playerQuitEvent -> plugin.getServer().getScheduler().runTaskAsynchronously(
						plugin, () -> {
							// Keep permissions loaded if userdata is still loaded
							if (userCache.getIfPresent(playerQuitEvent.getPlayer().getUniqueId()) != null) {
								PermissionUtil.loadPermissionData(playerQuitEvent.getPlayer().getUniqueId());
							}
						}
				), plugin));

		StringUtil.addSectionHandler(string -> new StringUtil.MultiMatcher<User>(Bukkit.getOnlinePlayers().stream()
				.map(Player::getUniqueId).map(UserManager.this::getUser)
				.map(user -> new Pair<>(user, user.getMentionPattern().matcher(string))).collect(Collectors.toSet())) {
			@Override
			protected TextComponent[] handleMatch(Matcher matcher, User user, int start, int end, TextComponent previousComponent) {
				if (matcher.group(2) == null || matcher.group(2).isEmpty()) {
					return new TextComponent[] {user.getMention()};
				}
				return new TextComponent[] {user.getMention(), new TextComponent(matcher.group(2))};
			}
		});
	}

	public User getUser(UUID uuid) {
		return userCache.getUnchecked(uuid);
	}

	public void registerCommandContext(EasterlynCore plugin) {
		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(User.class, context -> {
			if (context.hasFlag("self")) {
				if (context.getSender() instanceof Player) {
					return getUser(((Player) context.getSender()).getUniqueId());
				}
				throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE);
			}
			String potentialIdentifier = context.getFirstArg();
			try {
				return getUser(UUID.fromString(potentialIdentifier));
			} catch (IllegalArgumentException ignored) {}
			// TODO other flag should ignore self, requires PlayerUtil modification
			Player player;
			try {
				player = PlayerUtil.matchPlayer(context.getSender(), potentialIdentifier, context.hasFlag("offline"), plugin);
			} catch (IllegalAccessException e) {
				context.getSender().sendMessage("Called PlayerUtil#matchPlayer on the main thread while executing! Please /report this message.");
				e.printStackTrace();
				player = PlayerUtil.matchOnlinePlayer(context.getSender(), potentialIdentifier);
			}
			if (player != null) {
				context.popFirstArg();
				return getUser(player.getUniqueId());
			}
			if (context.hasFlag("other") || !(context.getSender() instanceof Player)) {
				throw new InvalidCommandArgument(MessageKeys.COULD_NOT_FIND_PLAYER);
			}
			return getUser(((Player) context.getSender()).getUniqueId());
		});
	}

	public void clearCache() {
		userCache.invalidateAll();
		userCache.cleanUp();
	}

}
