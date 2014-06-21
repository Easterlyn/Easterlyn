package co.sblock.data;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.tmathmeyer.jadis.async.Promise;

import co.sblock.data.redis.RedisClient;
import co.sblock.users.User;

public class RedisTest {

	RedisClient data;
	User u = new User(UUID.randomUUID());
	
	@Before
	public void startRedis()
	{
		data = new RedisClient();
		System.out.println(u.getUUID().toString());
		try {
			data.getClass().getField("playerDataPromise").setAccessible(true);
			data.getClass().getField("playerDataPromise").set(data, new MockPromise());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	
	@Test
	public void insertionTest() {
		data.saveUserData(u);
	}
	
	
	
	private class MockPromise implements Promise<User>
	{

		@Override
		public void getList(List<User> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getMap(Map<String, User> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getObject(User user) {
			assertEquals(user, u);
		}
		
	}

}
