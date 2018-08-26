package com.easterlyn.commands.teleportation;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Transportalizer;
import com.easterlyn.micromodules.Cooldowns;
import com.easterlyn.micromodules.Spectators;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.player.PlayerUtils;
import com.easterlyn.utilities.RegionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Essentials' TPA just won't cut it.
 *
 * @author Jikoo
 */
public class TeleportRequestCommand extends EasterlynCommand {

	private final Cooldowns cooldowns;
	private final Spectators spectators;
	private final Users users;
	private final Transportalizer transportalizer;
	private final SimpleDateFormat time = new SimpleDateFormat("m:ss");
	private final HashMap<UUID, TeleportRequest> pending = new HashMap<>();

	public TeleportRequestCommand(Easterlyn plugin) {
		super(plugin, "tpa");
		this.setAliases("tpask", "call", "tpahere", "tpaskhere", "callhere", "tpaccept", "tpyes", "tpdeny", "tpno");
		this.cooldowns = plugin.getModule(Cooldowns.class);
		this.spectators = plugin.getModule(Spectators.class);
		this.users = plugin.getModule(Users.class);
		this.transportalizer = (Transportalizer) plugin.getModule(Machines.class).getMachineByName("Transportalizer");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		label = label.toLowerCase();
		Player player = (Player) sender;
		if (label.equals("tpa") || label.equals("tpask") || label.equals("call")) {
			if (args.length == 0) {
				sender.sendMessage(getLang().getValue("command.tpa.help.request"));
				return true;
			}
			ask(player, args, false);
			return true;
		}
		if (label.equals("tpahere") || label.equals("tpaskhere") || label.equals("callhere")) {
			if (args.length == 0) {
				sender.sendMessage(getLang().getValue("command.tpa.help.here"));
				return true;
			}
			ask(player, args, true);
			return true;
		}
		if (label.equals("tpaccept") || label.equals("tpyes")) {
			accept(player);
			return true;
		}
		if (label.equals("tpdeny") || label.equals("tpno")) {
			decline(player);
			return true;
		}
		return false;
	}

	private void ask(Player sender, String[] args, boolean here) {
		long remainder = cooldowns.getRemainder(sender, "teleportRequest");
		if (remainder > 0) {
			sender.sendMessage(getLang().getValue("command.tpa.error.cooldown")
					.replace("{TIME}", time.format(new Date(remainder))));
			if (sender.hasPermission("easterlyn.command.tpreset")) {
				sender.sendMessage(getLang().getValue("command.tpa.reset"));
			}
			return;
		}
		Player target = PlayerUtils.matchOnlinePlayer(sender, args[0]);
		if (target == null) {
			sender.sendMessage(getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[0]));
			return;
		}
		if (target.getUniqueId().equals(sender.getUniqueId())) {
			sender.sendMessage(getLang().getValue("command.tpa.self"));
			return;
		}
		if (!target.hasPermission(getPermission())) {
			sender.sendMessage(getLang().getValue("command.tpa.error.recipientPermission")
					.replace("{PLAYER}", target.getDisplayName()));
			return;
		}
		User targetUser = users.getUser(target.getUniqueId());
		User sourceUser = users.getUser(sender.getUniqueId());
		boolean targetSpectating = spectators.isSpectator(targetUser.getUUID());
		boolean sourceSpectating = spectators.isSpectator(sourceUser.getUUID());
		if (!here && targetSpectating && !sourceSpectating || here && !targetSpectating && sourceSpectating) {
			sender.sendMessage(getLang().getValue("command.tpa.error.toSpectator"));
			return;
		}
		if (!RegionUtils.regionsMatch(targetUser.getPlayer().getWorld().getName(),
				sourceUser.getPlayer().getWorld().getName())) {
			sender.sendMessage(getLang().getValue("command.tpa.error.crossRegion"));
			return;
		}
		if (pending.containsKey(target.getUniqueId()) && pending.get(target.getUniqueId()).getExpiry() > System.currentTimeMillis()) {
			sender.sendMessage(getLang().getValue("command.tpa.error.recipientPending")
					.replace("{PLAYER}", target.getDisplayName()));
			return;
		}
		pending.put(target.getUniqueId(), new TeleportRequest(sender.getUniqueId(), target.getUniqueId(), here));
		cooldowns.addCooldown(sender, "teleportRequest", getPlugin().getConfig().getLong("command.tpa.ignored", 480000L));
		sender.sendMessage(getLang().getValue("command.tpa.success.sent"));
		target.sendMessage(getLang().getValue("command.tpa.success.receive")
				.replace("{PLAYER}", sender.getDisplayName())
				.replace("{OPTION}",
						here ? getLang().getValue("command.tpa.success.receiveToSender")
								: getLang().getValue("command.tpa.success.receiveToTarget")));
	}

	private void accept(Player sender) {
		TeleportRequest request = pending.remove(sender.getUniqueId());
		if (request == null || request.getExpiry() < System.currentTimeMillis()) {
			if (transportalizer.pendingTransportalizationFailed(sender, true)) {
				sender.sendMessage(getLang().getValue("command.tpa.error.noPending"));
			}
			return;
		}
		Player toTeleport = Bukkit.getPlayer(request.isHere() ? request.getTarget() : request.getSource());
		Player toArriveAt = Bukkit.getPlayer(request.isHere() ? request.getSource() : request.getTarget());
		if (toTeleport == null || toArriveAt == null) {
			sender.sendMessage(getLang().getValue("command.tpa.error.senderMissing"));
			return;
		}
		Player issuer = request.isHere() ? toArriveAt : toTeleport;
		if (spectators.isSpectator(toArriveAt.getUniqueId())
				&& !spectators.isSpectator(toTeleport.getUniqueId())) {
			String message = getLang().getValue("command.tpa.error.toSpectator");
			toTeleport.sendMessage(message);
			toArriveAt.sendMessage(message);
			cooldowns.clearCooldown(issuer, "teleportRequest");
			return;
		}
		if (!RegionUtils.regionsMatch(toTeleport.getWorld().getName(),
				toArriveAt.getWorld().getName())) {
			String message = getLang().getValue("command.tpa.error.crossRegion");
			toTeleport.sendMessage(message);
			toArriveAt.sendMessage(message);
			cooldowns.clearCooldown(issuer, "teleportRequest");
			return;
		}
		toTeleport.teleport(toArriveAt.getLocation().add(0, 0.1, 0), TeleportCause.COMMAND);
		toTeleport.sendMessage(getLang().getValue("command.tpa.success.arrive.teleported")
						.replace("{PLAYER}", toArriveAt.getDisplayName()));
		toArriveAt.sendMessage(getLang().getValue("command.tpa.success.arrive.target")
				.replace("{PLAYER}", toTeleport.getDisplayName()));

		// Teleporting as a spectator is a legitimate mechanic, no cooldown.
		if (spectators.isSpectator(toTeleport.getUniqueId())) {
			cooldowns.clearCooldown(issuer, "teleportRequest");
		} else {
			cooldowns.addCooldown(issuer, "teleportRequest", getPlugin().getConfig().getLong("command.tpa.cooldown", 3600000L));
		}
	}

	private void decline(Player sender) {
		TeleportRequest request = pending.remove(sender.getUniqueId());
		if (request == null || request.getExpiry() < System.currentTimeMillis()) {
			if (transportalizer.pendingTransportalizationFailed(sender, false)) {
				sender.sendMessage(getLang().getValue("command.tpa.error.noPending"));
			}
			return;
		}
		sender.sendMessage(getLang().getValue("command.tpa.decline.self"));
		Player issuer = Bukkit.getPlayer(request.getSource());
		if (issuer != null) {
			issuer.sendMessage(getLang().getValue("command.tpa.decline.other")
					.replace("{PLAYER}", sender.getDisplayName()));
			cooldowns.clearCooldown(issuer, "teleportRequest");
		}
	}

	private class TeleportRequest {
		private final UUID source;
		private final UUID target;
		private final boolean here;
		private final long expiry;
		TeleportRequest(UUID source, UUID target, boolean here) {
			this.source = source;
			this.target = target;
			this.here = here;
			this.expiry = System.currentTimeMillis() + 60000L;
		}
		public UUID getSource() {
			return source;
		}
		public UUID getTarget() {
			return target;
		}
		public boolean isHere() {
			return here;
		}
		long getExpiry() {
			return expiry;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission()) || args.length != 1) {
			return com.google.common.collect.ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}
}
