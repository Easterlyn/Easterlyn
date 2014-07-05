package co.sblock.data;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Before;
import org.junit.Test;

import com.tmathmeyer.jadis.async.Promise;

import co.sblock.data.redis.RedisClient;
import co.sblock.users.User;
import co.sblock.users.User.UserSpawner;

public class RedisTest {

	RedisClient data;
	User u1 = new UserSpawner().build(UUID.fromString("258a1cc2-937e-4b7f-aaa9-7b21c0ad0753"));
	User u2 = new UserSpawner().build(UUID.fromString("419f5b4d-a664-418c-89cc-d8d20942c178"));
	User u3 = new UserSpawner().build(UUID.fromString("61bc3587-668a-4541-b02b-2f5fcd19e686"));
	User u4 = new UserSpawner().build(UUID.fromString("4db202c4-76fa-48d2-8183-9ce9c165892f"));
	User u5 = new UserSpawner().build(UUID.fromString("fafbb4b3-03d0-4031-93cb-73535802b39d"));
	User u6 = new UserSpawner().build(UUID.fromString("1c464369-4bf8-4100-82b7-a3d4e1a66c02"));
	User u7 = new UserSpawner().build(UUID.fromString("9e63b68f-f52b-45ae-a393-9f3d7081cb4a"));

	AtomicInteger query = new AtomicInteger(7);

	@Before
	public void startRedis()
	{
		data = new RedisClient();
		data.enable();
		try {
			Field f = RedisClient.class.getDeclaredField("playerDataPromise");
			f.setAccessible(true);
			f.set(data, new MockPromise());
		} catch (Exception e) {
			fail(e.toString());
		}
	}


	@Test
	public void insertionTest() {
		data.saveUserData(u1);
		data.saveUserData(u2);
		data.saveUserData(u3);
		data.saveUserData(u4);
		data.saveUserData(u5);
		data.saveUserData(u6);
		data.saveUserData(u7);
	}

	@Test
	public void requestTest() {
		data.loadUserData(u1.getUUID());
		data.loadUserData(u2.getUUID());
		data.loadUserData(u3.getUUID());
		data.loadUserData(u4.getUUID());
		data.loadUserData(u5.getUUID());
		data.loadUserData(u6.getUUID());
		data.loadUserData(u7.getUUID());
		while(query.get() != 0);
	}

	private class MockPromise implements Promise<User>
	{

		@Override
		public void getList(List<User> arg0) {
			throw new NotImplementedException();
		}

		@Override
		public void getMap(Map<String, User> arg0) {
			throw new NotImplementedException();
		}

		@Override
		public void getObject(User user, String s) {
			assertNotNull(user);
			query.decrementAndGet();
		}

		@Override
		public void getSet(Set<User> arg0) {
			// TODO Auto-generated method stub
		}
	}

}
