package co.sblock.utilities.meteors;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import com.comphenix.protocol.ProtocolLibrary;

import co.sblock.Sblock;
import co.sblock.events.packets.WrapperPlayServerWorldParticles;
import co.sblock.module.CommandDenial;
import co.sblock.module.CommandDescription;
import co.sblock.module.CommandListener;
import co.sblock.module.CommandPermission;
import co.sblock.module.CommandUsage;
import co.sblock.module.SblockCommand;

/**
 * @author Dublek, Jikoo
 */
public class MeteorCommandListener implements CommandListener {

	/**
	 * Main MeteorMod Command.
	 * 
	 * @param sender the CommandSender
	 * @param arg the Command arguments
	 * @return true if Command was used correctly
	 */
	@CommandDenial
	@CommandDescription("Summon a meteor with parameters.")
	@CommandPermission("group.denizen")
	@CommandUsage("/meteor <u:user> <r:radius> <e:explode> <c:countdown> <m:material>")
	@SuppressWarnings("deprecation")
	@SblockCommand
	public boolean meteor(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		Location target = null;
		int radius = -1;
		String material = "";
		boolean blockDamage = false;
		int bore = -1;
		target = p.getTargetBlock(null, 128).getLocation();
		for (String s : args) {
			// lighter than multiple .equalsIgnoreCase
			s = s.toLowerCase();
			if (s.substring(0, 2).equals("u:")) {
				// set target (player or crosshairs)
				Player pTarget = Bukkit.getPlayer(s.substring(2));
				if (pTarget != null) {
					target = pTarget.getLocation();
				}
			} else if (s.substring(0, 2).equals("r:")) {
				// set radius
				radius = Integer.parseInt(s.substring(2));
			} else if (s.substring(0, 2).equals("e:")) {
				// set explosion block damage
				blockDamage = s.substring(2).equals("true");
			} else if (s.substring(0, 2).equals("m:")) {
				material = s.substring(2).toUpperCase();
			} else if (s.subSequence(0, 2).equals("b:")) {
				// set meteor to bore mode (default behavior: bore if not highest block)
				bore = s.substring(2).equals("true") ? 1 : 0;
			}
		}
		new Meteorite(target, material, radius, blockDamage, bore).dropMeteorite();
		return true;
	}

	@CommandDenial
	@CommandDescription("Uncomfortably fun!")
	@CommandPermission("group.denizen")
	@CommandUsage("/crotchrocket")
	@SblockCommand
	public boolean crotchrocket(CommandSender sender, String[] args) {
		final Player player = (Player) sender;
		player.getWorld().playEffect(player.getLocation(), Effect.EXPLOSION_HUGE, 0);

		final Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(),
				EntityType.FIREWORK);
		FireworkMeta fm = firework.getFireworkMeta();
		fm.setPower(4);
		firework.setFireworkMeta(fm);
		firework.setPassenger(player);

		final WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();
		packet.setParticleEffect(WrapperPlayServerWorldParticles.ParticleEffect.FIREWORKS_SPARK);
		packet.setNumberOfParticles(5);
		packet.setOffset(new Vector(0.5, 0.5, 0.5));

		final int particleTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				Sblock.getInstance(), new Runnable() {

					@Override
					public void run() {
						packet.setLocation(firework.getLocation());
						ProtocolLibrary.getProtocolManager().broadcastServerPacket(
								packet.getHandle(), firework.getLocation(), 64);
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
}
