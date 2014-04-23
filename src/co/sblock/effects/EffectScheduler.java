package co.sblock.effects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.chat.SblockChat;
import co.sblock.data.SblockData;
import co.sblock.machines.MachineManager;
import co.sblock.users.ChatData;
import co.sblock.users.User;

public class EffectScheduler extends BukkitRunnable {

	@Override
	public void run() {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			User u = User.getUser(p.getUniqueId());
			if (u == null) {
				u = SblockData.getDB().loadUserData(p.getUniqueId());
			}
			if (SblockChat.getComputerRequired()) {
				boolean computerAccess = MachineManager.hasComputerAccess(u);
				if (computerAccess && !ChatData.getListening(u).contains("#")) {
					ChatData.addListening(u, SblockChat.getChat().getChannelManager().getChannel("#"));
				}
				if (computerAccess && ChatData.getListening(u).contains("#")) {
					ChatData.removeListening(u, "#");
				}
			}
		}
	}
}
