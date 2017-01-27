package co.sblock.commands.admin;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import co.sblock.Sblock;
import co.sblock.commands.SblockAsynchronousCommand;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Transition command: Remove all free machines from world
 * 
 * @author Jikoo
 */
public class MachineWipeCommand extends SblockAsynchronousCommand {

	private final Machines machines;

	public MachineWipeCommand(Sblock plugin) {
		super(plugin, "I'mAbsolutelyCertainWeAreReadyToStopSblockAndMoveToEasterlynRightNow");
		this.setPermissionLevel("horrorterror");
		this.machines = ((Sblock) getPlugin()).getModule(Machines.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		sender.sendMessage("I'm sure, too. Thanks for the vote of confidence, boo.");

		Future<Collection<List<Location>>> future = Bukkit.getScheduler().callSyncMethod(getPlugin(), new Callable<Collection<List<Location>>>() {
			@Override
			public Collection<List<Location>> call() throws Exception {
				Machine[] types = machines.getMachinesByName().values().stream().filter(machine -> machine.isFree()).toArray(Machine[]::new);
				return machines.getMachinesOfType(types).values();
			}
		});

		Collection<List<Location>> locationsByChunk;
		try {
			locationsByChunk = future.get(10, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
			sender.sendMessage("Error getting machines by type within 10 seconds, check console.");
			return true;
		}

		for (List<Location> locations : locationsByChunk) {
			new BukkitRunnable() {
				@Override
				public void run() {
					for (Location key : locations) {
						Pair<Machine, ConfigurationSection> machine = machines.getMachineByLocation(key);
						if (machine != null) {
							machine.getLeft().remove(machine.getRight());
						}
					}
				}
			};
			try {
				Thread.sleep(50L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		sender.sendMessage("All done!");
		return true;
	}

}
