package com.easterlyn.commands.fun;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.events.packets.ParticleEffectWrapper;
import com.easterlyn.micromodules.ParticleUtils;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * EasterlynCommand for riding a firework in style.
 * 
 * @author Jikoo
 */
public class CrotchRocketCommand extends EasterlynCommand {

	public CrotchRocketCommand(Easterlyn plugin) {
		super(plugin, "crotchrocket");
		this.setPermissionLevel(UserRank.ADMIN);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		return launch((Easterlyn) this.getPlugin(), (LivingEntity) sender);
	}

	public static boolean launch(Easterlyn plugin, LivingEntity entity) {
		entity.setFallDistance(0);
		entity.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, entity.getLocation(), 1);

		final Firework firework = (Firework) entity.getWorld().spawnEntity(entity.getLocation(), EntityType.FIREWORK);
		FireworkMeta fm = firework.getFireworkMeta();
		fm.setPower(4);
		firework.setFireworkMeta(fm);
		firework.addPassenger(entity);

		ParticleUtils particles = plugin.getModule(ParticleUtils.class);
		particles.addEntity(firework, new ParticleEffectWrapper(Particle.FIREWORKS_SPARK, 5));

		new BukkitRunnable() {

			private int count = 0;

			@Override
			public void run() {
				if (count > 39 || firework.getLocation().getY() > 255) {
					particles.removeAllEffects(firework);
					firework.remove();
					cancel();
					return;
				}
				++count;
				firework.setVelocity(new Vector(0, 1, 0));
			}
		}.runTaskTimer(plugin, 0L, 1L);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
