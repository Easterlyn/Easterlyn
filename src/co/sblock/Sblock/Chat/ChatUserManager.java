package co.sblock.Sblock.Chat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Jikoo
 */
public class ChatUserManager {

	private  Map<UUID, ChatUser> users;
	private static ChatUserManager instance;

	/**
	 * Singleton
	 */
	public static ChatUserManager getUserManager() {
		if (instance == null) {
			instance = new ChatUserManager();
		}
		return instance;
	}

	private ChatUserManager() {
		users = new HashMap<>();
	}

	public ChatUser getUser(UUID userID) {
		return users.get(userID);
	}

	public ChatUser removeUser(UUID userID) {
		ChatUser u = users.remove(userID);
		if (u != null) {
			u.stopPendingTasks();
		}
		return u;
	}

	public ChatUser addUser(UUID userID) {
		if (users.containsKey(userID)) {
			return users.get(userID);
		}
		ChatUser u = new ChatUser(userID);
		users.put(userID, u);
		return u;
	}

	public Collection<ChatUser> getUserlist() {
		return users.values();
	}
}
