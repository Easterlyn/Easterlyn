package com.easterlyn.user;

import com.easterlyn.EasterlynCore;
import com.easterlyn.event.UserLoadEvent;
import com.easterlyn.event.UserUnloadEvent;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.StringUtil;
import com.easterlyn.util.event.SimpleListener;
import com.easterlyn.util.text.ParsedText;
import com.easterlyn.util.text.QuoteConsumer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
							User user = userCache.getIfPresent(playerQuitEvent.getPlayer().getUniqueId());
							if (user != null) {
								// Save on quit as well as unload just in case
								user.save();
								// Keep permissions loaded if userdata is still loaded
								PermissionUtil.loadPermissionData(playerQuitEvent.getPlayer().getUniqueId());
							}
						}
				), plugin));

		StringUtil.addQuoteConsumer(new QuoteConsumer() {
			@Override
			public Iterable<Pattern> getPatterns() {
				return userCache.asMap().keySet().stream().map(UserManager.this::getUser)
						.map(User::getMentionPattern).collect(Collectors.toSet());
			}

			@Override
			public @Nullable Supplier<Matcher> handleQuote(String quote) {
				for (User loaded : userCache.asMap().values()) {
					Pattern mentionPattern = loaded.getMentionPattern();
					Matcher matcher = mentionPattern.matcher(quote);
					if (!matcher.find()) {
						continue;
					}
					return new UserMatcher() {
						@Override
						public User getUser() {
							return loaded;
						}

						@Override
						public Matcher get() {
							return matcher;
						}
					};
				}
				return null;
			}

			@Override
			public void addComponents(@NotNull ParsedText components, @NotNull Supplier<Matcher> matcherSupplier) {
				if (!(matcherSupplier instanceof UserMatcher)) {
					components.addText(matcherSupplier.get().group());
					return;
				}

				Matcher matcher = matcherSupplier.get();
				User user = ((UserMatcher) matcherSupplier).getUser();

				components.addComponent(user.getMention());
				if (matcher.group(2) != null && !matcher.group(2).isEmpty()) {
					components.addText(matcher.group(2));
				}
			}
		});
	}

	private interface UserMatcher extends Supplier<Matcher> {

		User getUser();

	}

	public User getUser(UUID uuid) {
		return userCache.getUnchecked(uuid);
	}

	// TODO getUser(CommandSender)

	public void clearCache() {
		userCache.invalidateAll();
		userCache.cleanUp();
	}

}
