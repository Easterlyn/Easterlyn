package co.sblock.data;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.Gson;

import co.sblock.users.User;
import co.sblock.users.UserManager;

public class SerializationTest {

	@Test
	public void test()
	{
		new MockBroadcast();
		new MockSpawnLocationInformation();
		
		
		
		User user = UserManager.doFirstLogin(new MockPlayer());
		
		String s = new Gson().toJson(user);
		System.out.println(s);
		
		User u = new Gson().fromJson(s, User.class);
		
		assertEquals(u, user);
		
	}
}
