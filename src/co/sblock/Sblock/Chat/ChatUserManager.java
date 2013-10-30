package co.sblock.Sblock.Chat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jikoo
 */
public class ChatUserManager {

	private  Map<String, ChatUser> users = new HashMap<String, ChatUser>();
	private static ChatUserManager instance;

	private ChatUserManager(){}

	/**
	 * Singleton
	 */
	public static ChatUserManager getUserManager() {
		if (instance == null) {
			instance = new ChatUserManager();
		}
		return instance;
	}

	public ChatUser getUser(String name) {
		return users.get(name);
	}

	public ChatUser removeUser(String name) {
		ChatUser u = users.remove(name);
		if (u != null) {
			u.stopPendingTasks();
		}
		return u;
	}

	public ChatUser addUser(String name) {
		ChatUser u = new ChatUser(name);
		users.put(name, u);
		return u;
	}

	public Collection<ChatUser> getUserlist() {
		return users.values();
	}
}
