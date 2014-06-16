package co.sblock.users;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.sblock.data.SblockData;
import co.sblock.module.Module;

/**
 * This module holds player information and provides methods for other modules
 * to access that data, as well as interfacing with the database.
 * 
 * @author FireNG, Dublek
 */
public class SblockUsers extends Module {

    /**
     * @see co.sblock.Module#onEnable()
     */
    @Override
    protected void onEnable() {
        // Initialize the player manager
        UserManager.getUserManager();
        this.registerCommands(new UserDataCommands());

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            SblockData.getDB().loadUserData(p.getUniqueId());
        }
    }

    /**
     * @see co.sblock.Module#onDisable()
     */
    @Override
    protected void onDisable() {
        for (User u : UserManager.getUserManager().getUserlist().toArray(new User[0])) {
            SblockData.getDB().saveUserData(u.getUUID());
        }
    }

}
