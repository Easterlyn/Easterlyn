package co.sblock.data.yaml;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import co.sblock.Sblock;
import co.sblock.chat.channel.Channel;
import co.sblock.data.SblockData;
import co.sblock.machines.MachineManager;
import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.MachineType;
import co.sblock.utilities.Log;

/**
 * 
 * 
 * @author Jikoo
 */
public class YamlData extends SblockData {

	private LinkedHashMap<String, String> ipcache;

	@Override
	public boolean enable() {
		ipcache = new LinkedHashMap<>();
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
		// Migrated
	}

	@Override
	public void loadUserData(UUID uuid) {
		// Migrated
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
		// Migrated
	}

	@Override
	public void loadAllChannelData() {
		// Migrated
	}

	@Override
	public void deleteChannel(String channelName) {
		// Migrated
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
