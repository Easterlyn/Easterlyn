package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import co.sblock.data.SblockData;
import co.sblock.effects.EffectManager;
import co.sblock.users.User;
import co.sblock.users.UserManager;

/**
 * Listener for PlayerJoinEvents.
 * 
 * @author Jikoo
 */
public class PlayerJoinListener implements Listener {

    /**
     * The event handler for PlayerJoinEvents.
     * 
     * @param event the PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.AQUA + event.getPlayer().getDisplayName() + ChatColor.GREEN + " logs the fuck in");

        SblockData.getDB().loadUserData(event.getPlayer().getUniqueId());

        User u = UserManager.getUserManager().addUser(event.getPlayer().getUniqueId());
        u.setAllPassiveEffects(EffectManager.passiveScan(event.getPlayer()));
        EffectManager.applyPassiveEffects(u);
    }
}
