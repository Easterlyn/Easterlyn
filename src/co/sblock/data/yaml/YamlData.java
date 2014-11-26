package co.sblock.data.yaml;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.data.SblockData;
import co.sblock.machines.MachineManager;
import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.MachineType;
import co.sblock.users.ProgressionState;
import co.sblock.users.Region;
import co.sblock.users.User;
import co.sblock.users.UserAspect;
import co.sblock.users.UserClass;
import co.sblock.users.User.UserBuilder;
import co.sblock.users.UserManager;
import co.sblock.utilities.Log;

/**
 * 
 * 
 * @author Jikoo
 */
public class YamlData extends SblockData {

	@Override
	public boolean enable() {
		return true;
	}

	@Override
	public void disable() {}

	@Override
	protected Connection connection() {
		return null;
	}

	@Override
	public void saveUserData(UUID uuid) {
		File file;
		try {
			file = new File(Sblock.getInstance().getUserDataFolder(), uuid.toString() + ".yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to save data for " + uuid, e);
		}
		Player player = Bukkit.getPlayer(uuid);
		User user = UserManager.getUser(uuid);
		if (player == null || user == null) {
			return;
			// We got problems
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		yaml.set("name", player.getName());
		yaml.set("nickname", player.getDisplayName());
		yaml.set("ip", user.getUserIP());
		yaml.set("location", BukkitSerializer.locationToString(player.getLocation()));
		yaml.set("region", user.getCurrentRegion().getDisplayName());
		yaml.set("previousLocation", BukkitSerializer.locationToString(user.getPreviousLocation()));
		yaml.set("previousRegion", null);
		yaml.set("flying", user.canFly());
		yaml.set("classpect.class", user.getUserClass().getDisplayName());
		yaml.set("classpect.aspect", user.getAspect().getDisplayName());
		yaml.set("classpect.dream", user.getDreamPlanet().getDisplayName());
		yaml.set("classpect.medium", user.getMediumPlanet().getDisplayName());
		yaml.set("progression.progression", user.getProgression().name());
		yaml.set("progression.programs", user.getPrograms());
		yaml.set("progression.server", user.getServer() != null ? user.getServer().toString() : null);
		yaml.set("progression.client", user.getClient() != null ? user.getClient().toString() : null);
		yaml.set("chat.current", user.getCurrent() != null ? user.getCurrent().getName() : "#");
		yaml.set("chat.listening", user.getListening());
		yaml.set("chat.muted", user.isMute());
		yaml.set("chat.suppressing", user.isSuppressing());
		yaml.set("chat.ignoring", null);
		yaml.set("chat.highlights", null);
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save data for " + uuid, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadUserData(UUID uuid) {
		UserBuilder builder = new UserBuilder();
		File file;
		try {
			file = new File(Sblock.getInstance().getUserDataFolder(), uuid.toString() + ".yml");
			if (!file.exists()) {
				getLogger().warning("File " + uuid.toString() + ".yml does not exist!");
				// Do first login
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load data for " + uuid, e);
		}
		Player player = Bukkit.getPlayer(uuid);
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		player.setDisplayName(yaml.getString("nickname"));
		builder.setIPAddr(yaml.getString("ip"));
		builder.setPreviousLocationFromString(yaml.getString("previousLocation"));
		//yaml.getString("previousRegion");
		builder.setUserClass(UserClass.getClass(yaml.getString("classpect.class")));
		builder.setAspect(UserAspect.getAspect(yaml.getString("classpect.aspect")));
		Region dream = Region.getRegion(yaml.getString("classpect.dream"));
		builder.setDreamPlanet(dream);
		Region current = Region.getRegion(player.getWorld().getName());
		if (current.isDream()) {
			current = dream;
		}
		builder.setCurrentRegion(current);
		builder.setMediumPlanet(Region.getRegion(yaml.getString("classpect.medium")));
		builder.setProgression(ProgressionState.valueOf(yaml.getString("progression.progression")));
		builder.setPrograms((HashSet<Integer>) yaml.get("progression.programs"));
		if (yaml.getString("progression.server") != null) {
			builder.setServer(UUID.fromString(yaml.getString("progression.server")));
		}
		if (yaml.getString("progression.client") != null) {
			builder.setClient(UUID.fromString(yaml.getString("progression.client")));
		}
		builder.setCurrentChannel(yaml.getString("chat.current"));
		builder.setListening((HashSet<String>) yaml.get("chat.listening"));
		builder.setGlobalMute(new AtomicBoolean(yaml.getBoolean("chat.muted")));
		builder.setSuppress(new AtomicBoolean(yaml.getBoolean("chat.suppressing")));
		//(Set<String>) yaml.get("chat.ignoring");
		User user = builder.build(uuid);
		user.updateFlight();
		user.updateCurrentRegion(current);
		user.loginAddListening(user.getListening().toArray(new String[0])); // TODO change method when db rework is over
		UserManager.addUser(user);
	}

	@Override
	public void startOfflineLookup(CommandSender sender, String name) {
		// TODO some sort of index
	}

	/* (non-Javadoc)
	 * @see co.sblock.data.SblockData#deleteUser(java.util.UUID)
	 */
	@Override
	public void deleteUser(UUID uuid) {
		File file;
		try {
			file = new File(Sblock.getInstance().getUserDataFolder(), uuid.toString() + ".yml");
			if (file.exists()) {
				file.delete();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to delete data for " + uuid, e);
		}
	}

	@Override
	public void saveChannelData(Channel channel) {
		getLogger().info("saving channel");
		File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "ChatChannels.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to save data for channel " + channel.getName(), e);
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		String name = channel.getName().replace("#", "ThisIsNotAComment");
		yaml.set(name + ".owner", channel.getOwner().toString());
		yaml.set(name + ".type", channel.getType().name());
		yaml.set(name + ".access", channel.getAccess().name());
		HashSet<String> set = new HashSet<>();
		for (UUID uuid : channel.getModList()) {
			set.add(uuid.toString());
		}
		yaml.set(name + ".mods", set);
		set.clear();
		for (UUID uuid : channel.getBanList()) {
			set.add(uuid.toString());
		}
		yaml.set(name + ".bans", set);
		set.clear();
		for (UUID uuid : channel.getApprovedUsers()) {
			set.add(uuid.toString());
		}
		yaml.set(name + ".approved", set);
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save data for channel " + channel.getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadAllChannelData() {
		File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "ChatChannels.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load channel data!", e);
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		for (String channelName : yaml.getKeys(false)) {
			Channel channel = ChannelManager.getChannelManager().loadChannel(channelName.replace("ThisIsNotAComment", "#"),
					AccessLevel.valueOf(yaml.getString(channelName + ".access")),
					UUID.fromString(yaml.getString(channelName + ".owner")),
					ChannelType.valueOf(yaml.getString(channelName + ".type")));
			for (String uuid : ((HashSet<String>) yaml.get(channelName + ".mods"))) {
				channel.addModerator(UUID.fromString(uuid));
			}
			for (String uuid : ((HashSet<String>) yaml.get(channelName + ".bans"))) {
				channel.addBan(UUID.fromString(uuid));
			}
			for (String uuid : ((HashSet<String>) yaml.get(channelName + ".approved"))) {
				channel.addApproved(UUID.fromString(uuid));
			}
		}
	}

	@Override
	public void deleteChannel(String channelName) {
		File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "ChatChannels.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load channel data!", e);
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		yaml.set(channelName.replace("#", "ThisIsNotAComment"), null);
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to delete channel " + channelName, e);
		}
	}

	@Override
	public void saveMachine(Machine m) {
		File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "Machines.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load machine data!", e);
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		String machine = m.getLocationString();
		yaml.set(machine + ".type", m.getType().name());
		yaml.set(machine + ".owner", m.getOwner());
		yaml.set(machine + ".direction", m.getFacingDirection().name());
		yaml.set(machine + ".data", m.getData());
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save machine at " + machine, e);
		}
	}

	@Override
	public void deleteMachine(Machine m) {
		File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "Machines.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load machine data!", e);
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		yaml.set(m.getLocationString(), null);
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to delete machine at " + m.getLocationString(), e);
		}
	}

	@Override
	public void loadAllMachines() {
		File file;
		try {
			file = new File(Sblock.getInstance().getDataFolder(), "Machines.yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load machine data!", e);
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		for (String machineLocation : yaml.getKeys(false)) {
			MachineManager.getManager().addMachine(BukkitSerializer.locationFromString(machineLocation),
					MachineType.getType(yaml.getString(machineLocation + ".type")),
					yaml.getString(machineLocation + ".owner"),
					Direction.valueOf(yaml.getString(machineLocation + ".direction")),
					yaml.getString(machineLocation + ".data"));
		}
	}

	@Override
	public String getUserFromIP(String hostAddress) {
		// TODO some sort of index
		return "Player";
	}

	@Override
	public Log getLogger() {
		return Log.getLog("SblockData");
	}
}
