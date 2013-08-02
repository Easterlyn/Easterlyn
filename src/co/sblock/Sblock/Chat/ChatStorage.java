/**
 * 
 */
package co.sblock.Sblock.Chat;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * Storage for all chat-related data that is not to be stored in the db.
 * (Main channel data as fallback, ban reasons)
 * 
 * @author Jikoo
 * 
 */
public class ChatStorage {
	private YamlConfiguration storage;
	File storageFile;
	public ChatStorage() {
		storageFile = new File(Sblock.getInstance().getDataFolder(), "data.yml");
		// loadConfiguration loads a new configuration if the file is not found.
		// I see no reason for needless error handling here, messages will appear
		// on later failures.
		storage = YamlConfiguration.loadConfiguration(storageFile);
	}

	public void setBan(String user, String reason) {
		storage.set("bans." + user, reason);
		try {
			storage.save(storageFile);
		} catch (IOException e) {
			new Sblogger("SblockChat").warning("Could not update bans; " + user
					+ " will have no (or, if changed, prior) ban reason on restart.");
		}
	}

	public String getBan(String user) {
		Set<String> allBans = storage.getConfigurationSection("bans").getKeys(false);
		if (allBans.contains(user)) {
			return storage.getString("bans." + user);
		} else return null;
	}

	public void removeBan(String user) {
		storage.set("bans." + user, null);
		try {
			storage.save(storageFile);
		} catch (IOException e) {
			new Sblogger("SblockChat").warning("Could not update bans; " +
					user + "'s ban reason is still filed.");
		}
	}

	public void setGlobalNick(String user, String nick) {
		storage.set("nicks." + user, nick);
		try {
			storage.save(storageFile);
		} catch (IOException e) {
			new Sblogger("SblockChat").warning("Could not update nicks; " +
					user + " will have no (or, if changed, prior) nick on restart.");
		}
	}

	public String getGlobalNick(String user) {
		Set<String> allNicks = storage.getConfigurationSection("nicks").getKeys(false);
		if (allNicks.contains(user)) {
			return storage.getString("nicks." + user);
		} else return user;
	}

	public void removeGlobalNick(String user) {
		storage.set("nicks." + user, null);
		try {
			storage.save(storageFile);
		} catch (IOException e) {
			new Sblogger("SblockChat").warning("Could not update nicks; " +
					user + "'s nick is still filed.");
		}
	}

	public void setGlobalMute(String user) {
		storage.set("mutes." + user, true);
		try {
			storage.save(storageFile);
		} catch (IOException e) {
			new Sblogger("SblockChat").warning("Could not update mutes; " +
					user + " will be unmuted on restart.");
		}
	}

	public boolean getGlobalMute(String user) {
		Set<String> allMutes = storage.getConfigurationSection("mutes").getKeys(false);
		if (allMutes.contains(user)) {
			return storage.getBoolean("mutes." + user);
		} else return false;
	}

	public void removeGlobalMute(String user) {
		storage.set("mutes." + user, null);
		try {
			storage.save(storageFile);
		} catch (IOException e) {
			new Sblogger("SblockChat").warning("Could not update mutes; " +
					user + "'s mute will recur on restart.");
		}
	}
}
