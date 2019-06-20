package com.easterlyn.users;

import com.easterlyn.Easterlyn;
import com.easterlyn.event.PlayerNameChangeEvent;
import com.easterlyn.event.UserCreationEvent;
import com.easterlyn.event.UserLoadEvent;
import com.easterlyn.util.PlayerUtil;
import com.easterlyn.util.StringUtil;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Data storage for a user.
 *
 * @author Jikoo
 */
public class User {

	private final Easterlyn plugin;
	private final UUID uuid;
	private final YamlConfiguration storage;
	private final Map<String, Object> tempStore;

	private User(@NotNull Easterlyn plugin, @NotNull UUID uuid, @NotNull YamlConfiguration storage) {
		this.plugin = plugin;
		this.uuid = uuid;
		this.storage = storage;
		tempStore = new ConcurrentHashMap<>();
	}

	@NotNull
	public UUID getUniqueId() {
		return uuid;
	}

	@Nullable
	public Player getPlayer() {
		try {
			return PlayerUtil.getPlayer(plugin, getUniqueId());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void sendMessage(String message) {
		sendMessage(StringUtil.fromLegacyText(message));
	}

	public void sendMessage(BaseComponent... components) {
		Player player = getPlayer();
		if (player != null) {
			player.sendMessage(components);
		}
	}

	public YamlConfiguration getStorage() {
		return storage;
	}

	public Map<String, Object> getTemporaryStorage() {
		return tempStore;
	}

	/**
	 * Updates the user's ability to fly.
	 */
	public void updateFlight() {
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = getPlayer();
				if (player == null) {
					storage.set("flying", false);
					return;
				}
				boolean allowFlight = player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
				if (!allowFlight && player.hasPermission("easterlyn.command.fly.safe")) {
					allowFlight = player.getLocation().add(0, -1, 0).getBlock().getType().isSolid();
					// TODO use bounding box?
				}
				player.setAllowFlight(allowFlight);
				player.setFlying(allowFlight);
				getStorage().set("flying", allowFlight);
			}
		}.runTaskLater(plugin, 10L);
	}

	/**
	 * The String representation of the Player's total time in game.
	 *
	 * @return the Player's time in game
	 */
	private String getTimePlayed() {
		Player player = getPlayer();
		if (player == null) {
			return "0 days, 00:00";
		}
		int time = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60;
		int days = time / (24 * 60);
		time -= days * 24 * 60;
		int hours = time / (60);
		time -= hours * 60;
		DecimalFormat decimalFormat = new DecimalFormat("00");
		return days + " days, " + decimalFormat.format(hours) + ':' + decimalFormat.format(time);
	}

	void save() {
		File file = new File(plugin.getDataFolder().getPath() + File.separatorChar + "users", getUniqueId().toString() + ".yml");
		try {
			getStorage().save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save data for " + getUniqueId(), e);
		}
	}

	static User load(Easterlyn plugin, final UUID uuid) {
		PluginManager pluginManager = plugin.getServer().getPluginManager();
		File file = new File(plugin.getDataFolder().getPath() + File.separatorChar + "users", uuid.toString() + ".yml");
		if (file.exists()) {
			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
			User user = new User(plugin, uuid, yaml);
			Player player = user.getPlayer();

			if (player != null && player.getAddress() != null) {
				yaml.set("ip", player.getAddress().getHostString());
				String previousName = yaml.getString("name");
				if (previousName != null && !previousName.equals(player.getName())) {
					yaml.set("previousName", previousName);
					yaml.set("name", player.getName());
					pluginManager.callEvent(new PlayerNameChangeEvent(player, previousName, player.getName()));
				}
			}

			pluginManager.callEvent(new UserLoadEvent(user));
			return user;
		}

		Player player = Bukkit.getPlayer(uuid);

		User user = new User(plugin, uuid, new YamlConfiguration());
		if (player != null) {
			user.getStorage().set("name", player.getName());
			if (player.getAddress() != null) {
				user.getStorage().set("ip", player.getAddress().getHostString());
			}

			if (!player.hasPlayedBefore()) {
				pluginManager.callEvent(new UserCreationEvent(user));
			}
		}

		pluginManager.callEvent(new UserLoadEvent(user));
		return user;
	}

}
