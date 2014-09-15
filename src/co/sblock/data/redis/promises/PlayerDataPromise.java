package co.sblock.data.redis.promises;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.users.User;
import co.sblock.users.UserManager;

import com.tmathmeyer.jadis.async.Promise;

public class PlayerDataPromise implements Promise<User> {

	private final static PlayerDataPromise instance = new PlayerDataPromise();

	private PlayerDataPromise() { }

	public static PlayerDataPromise getPDP() {
		return instance;
	}

	@Override
	public void getList(List<User> listOfUsers) { }

	@Override
	public void getMap(Map<String, User> MapOfUsers) { }

	@Override
	public void getSet(Set<User> set) { }

	/**
	 * gets called by load user data!!
	 */
	@Override
	public void getObject(User user, String key) {
		if (user != null) {
			user.initAfterDeserialization();
			UserManager.addUser(user);
		} else {
			UUID id = UUID.fromString(key);
			Player p = Bukkit.getPlayer(id);
			if (p != null) {
				UserManager.doFirstLogin(p);
			}
		}
	}

}
