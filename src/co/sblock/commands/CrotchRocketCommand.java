package co.sblock.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import co.sblock.Sblock;

import com.google.common.collect.ImmutableList;

/**
 * SblockCommand for riding a firework in style.
 * 
 * @author Jikoo
 */
public class CrotchRocketCommand extends SblockCommand {

	public CrotchRocketCommand() {
		super("crotchrocket");
		this.setDescription("Uncomfortably fun!");
		this.setUsage("/crotchrocket");
		this.setPermission("group.felt");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		final Player player = (Player) sender;
		player.getWorld().playEffect(player.getLocation(), Effect.EXPLOSION_HUGE, 0);

		final Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
		FireworkMeta fm = firework.getFireworkMeta();
		fm.setPower(4);
		firework.setFireworkMeta(fm);
		firework.setPassenger(player);

		// Particle data
//		final WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();
//		packet.setParticleEffect(WrapperPlayServerWorldParticles.ParticleEffect.FIREWORKS_SPARK);
//		packet.setNumberOfParticles(5);
//		packet.setOffset(new Vector(0.5, 0.5, 0.5));

		final int particleTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				//packet.setLocation(firework.getLocation());
				firework.setVelocity(new Vector(0, 2, 0));
				// TODO particle utility ProtocolLibrary.getProtocolManager().broadcastServerPacket(packet.getHandle(), firework.getLocation(), 64);
			}
		}, 0, 1L);

		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				Bukkit.getScheduler().cancelTask(particleTask);
				firework.remove();
			}
		}, 40L);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
