package co.sblock.Sblock.PlayerData;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import co.sblock.Sblock.Sblock;

/**
 * @author FireNG
 *
 */
public class PlayerDataEvents implements Listener {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
	Sblock.getInstance().getLogger().info(event.getPlayer().getName() + " joined");
	PlayerManager.getPlayerManager().addPlayer(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
	PlayerManager.getPlayerManager().removePlayer(event.getPlayer());
    }

}
