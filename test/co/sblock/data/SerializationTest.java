package co.sblock.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import co.sblock.users.User;

public class SerializationTest {

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
