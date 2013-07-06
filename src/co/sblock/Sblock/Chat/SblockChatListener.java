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
		//Theoretically, each channel db knows all players listening, even those who are offline.
		//So as long as the channel has a list of players, the User doesn't need to know what channels it's listening to.
		
		//if (pg.SELECT*FROMPlayerDataWHEREplayerName=event.getPlayer().getName() == null)
		User.addPlayer(event.getPlayer());
		//else
		User.login(event.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerChat (AsyncPlayerChatEvent event)	{
		if(User.getUser(event.getPlayer().getName()) != null)	{
			event.setCancelled(true);
			User.getUser(event.getPlayer().getName()).chat(event);
		}
	}
	
	@EventHandler
	public void onPlayerQuit (PlayerQuitEvent event)	{
		User.logout(event.getPlayer());
	}
	
}
