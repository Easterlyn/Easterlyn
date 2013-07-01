package co.sblock.Sblock.Chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SblockChatListener implements Listener {

	@EventHandler
	public void onPlayerJoin (PlayerJoinEvent event)	{
		//Theoretically, the channel db knows all players listening, even those who are offline.
		//So as long as the channel has a list of players, the User doesn't need to know what channels it's listening to.
		
		//user.addplayer
		
		User.addPlayer(event.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerChat (AsyncPlayerChatEvent event)	{
		//if user exists
		//cancel event
		//user.chatevent
	}
	
	@EventHandler
	public void onPlayerQuit (PlayerQuitEvent event)	{
		//user.removeplayer
	}
	
}
