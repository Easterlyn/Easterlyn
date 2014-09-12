package co.sblock.data;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

import com.google.gson.Gson;

import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.users.User.UserSpawner;

public class SerializationTest {

	@Test
	public void test()
	{
		UserSpawner sp = new UserSpawner();
		
		User user = new UserSpawner().build(UUID.randomUUID());
		user.setLoaded();
		
		String s = new Gson().toJson(user);
		System.out.println(s);
		
		User u = new Gson().fromJson(s, User.class);
		
		assertEquals(u, user);
		
	}

}
