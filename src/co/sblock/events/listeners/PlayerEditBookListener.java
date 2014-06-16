package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

import co.sblock.utilities.Broadcast;

/**
 * Listener for PlayerEditBookEvents.
 * 
 * @author Jikoo
 */
public class PlayerEditBookListener implements Listener {

    /**
     * EventHandler for PlayerEditBookEvents.
     * 
     * @param event the PlayerEditBookEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onBookEdit(PlayerEditBookEvent e) {
        if (e.isSigning() && e.getNewBookMeta().hasTitle() && e.getNewBookMeta().getTitle().equals("Captchadex")) {
            BookMeta bm = e.getNewBookMeta().clone();
            bm.setTitle(ChatColor.DARK_RED + "CaptchaNOPE.");
            e.setNewBookMeta(bm);
            Broadcast.lilHal("It appears that " + e.getPlayer().getName()
                    + " just tried to title a book Captchadex. Please take a moment to laugh at them.");
        }
    }
}
