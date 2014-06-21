package co.sblock.data.redis;

import java.util.List;
import java.util.Map;

import co.sblock.users.User;

import com.tmathmeyer.jadis.async.Promise;

public class PlayerDataPromise implements Promise<User> {

	private final static PlayerDataPromise instance = new PlayerDataPromise();

	private PlayerDataPromise() { }

	public static PlayerDataPromise getPDP() {
		return instance;
	}

	@Override
	public void getList(List<User> listOfUsers) {
		// TODO Auto-generated method stub
	}

	@Override
	public void getMap(Map<String, User> MapOfUsers) {
		// TODO Auto-generated method stub
	}

	@Override
	public void getObject(User user) {
		// TODO Auto-generated method stub
	}

}
