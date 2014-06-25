package co.sblock.data.redis;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.Broadcast;

import com.tmathmeyer.jadis.async.Promise;

public class PlayerDataPromise implements Promise<User> {

	private final static PlayerDataPromise instance = new PlayerDataPromise();

	private PlayerDataPromise() { }

	public static PlayerDataPromise getPDP() {
		return instance;
	}

	@Override
	public void getList(List<User> listOfUsers) {
		throw new NotImplementedException();
	}

	@Override
	public void getMap(Map<String, User> MapOfUsers) {
		throw new NotImplementedException();
	}

	@Override
	public void getObject(User user, String key) {
		if (user != null) {
			UserManager.addUser(user);
		} else {
			UUID id = UUID.fromString(key);
			Player p = Bukkit.getPlayer(id);
			if (p != null) {
				//player's first login
				Broadcast.lilHal("It would seem that " + p.getName()
						+ " is joining us for the first time! Please welcome them.");
				p.teleport(new Location(Bukkit.getWorld("Earth"), -3.5, 20, 6.5, 179.99F, 1F));
				user = UserManager.getUser(p.getUniqueId());
				user.loginAddListening(new String[]{"#" , "#" + user.getPlayerRegion().name()});
				user.updateCurrentRegion(user.getPlayerRegion());
				user.setLoaded();
				UserManager.team(p);
			}
		}
	}

}
