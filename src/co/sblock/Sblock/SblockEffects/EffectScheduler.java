package co.sblock.Sblock.SblockEffects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Database.SblockData;
import co.sblock.Sblock.Machines.MachineManager;
import co.sblock.Sblock.UserData.ChatUser;
import co.sblock.Sblock.UserData.User;

public class EffectScheduler extends BukkitRunnable {

	@Override
	public void run() {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			User u = User.getUser(p.getUniqueId());
			if (u == null) {
				u = SblockData.getDB().loadUserData(p.getUniqueId());
			}
			boolean computerAccess = MachineManager.hasComputerAccess(u);
			if (computerAccess && !ChatUser.getListening(u).contains("#")) {
				ChatUser.addListening(u, ChannelManager.getChannelManager().getChannel("#"));
			}
			if (computerAccess && ChatUser.getListening(u).contains("#")) {
				ChatUser.removeListening(u, "#");
			}
		}
	}
}
