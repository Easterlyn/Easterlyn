package co.sblock.data;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

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
	
	
	@Test
	public void recurisveClassTest()
	{
		classPrinter(User.class);
	}
	
	public void classPrinter(Class<?> t)
	{
		Set<Field> fields = new HashSet<>();
		
		for(Field f : t.getDeclaredFields())
		{
			if (!Modifier.isTransient(f.getModifiers()))
			{
				if (!Modifier.isStatic(f.getModifiers()))
				{
					fields.add(f);
				}
			}
		}
		
		System.out.println("="+t.getSimpleName()+"===============");
		for(Field f : fields)
		{
			System.out.println(" -> "+f.getType().getSimpleName());
		}
		System.out.println("======================================");
		
		for(Field f : fields)
		{
			classPrinter(f.getType());
		}
	}
}
