package co.sblock.utilities;

import java.util.Collection;

import org.bukkit.entity.Player;

public class CollectionUtils
{
	public static <T extends Player> int sizeofCollection(T[] col)
	{
		return col.length;
	}
	
	public static <T extends Player> int sizeofCollection(Collection<T> col)
	{
		return col.size();
	}
}
