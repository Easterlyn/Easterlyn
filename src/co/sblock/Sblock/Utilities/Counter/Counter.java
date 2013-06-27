package co.sblock.Sblock.Utilities.Counter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Counter extends JavaPlugin {

	@SuppressWarnings("unused")
	private BukkitTask task;
	private int playerLevel;

	@Override
	public void onEnable() {

	}

	@Override
	public void onDisable() {
		// task.cancel();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player pTarget;
		int duration;
		if (cmd.getName().equalsIgnoreCase("counter")) {
			if ((sender instanceof Player && sender.hasPermission("counter.set"))
					|| !(sender instanceof Player)) {
				if (args.length == 2) {
					pTarget = getServer().getPlayer(args[0]);
					duration = Integer.parseInt(args[1]);
					this.startCounter(pTarget, duration);
					return true;
				}
			}
		}
		return false;
	}

	public void startCounter(Player p, int tStart) {
		playerLevel = p.getLevel();
		p.setLevel(tStart);
		task = new CounterClock(this, p, playerLevel, false).runTaskLater(this, 20);
	}

}
