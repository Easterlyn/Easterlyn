package co.sblock.chat.ai;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

import co.sblock.users.OfflineUser;

/**
 * Extension of OfflineUser to allow fake player objects to be created.
 * 
 * @author Jikoo
 */
public class AIUser extends OfflineUser {

	/**
	 * @param displayName
	 */
	protected AIUser(String displayName) {
		super(UUID.randomUUID(), "localhost", new YamlConfiguration(), null, null, "#", new HashSet<String>());
		getYamlConfiguration().set("nickname", displayName);
	}
}
