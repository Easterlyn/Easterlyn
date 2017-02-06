package com.easterlyn.commands.teleportation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Transportalizer;
import com.easterlyn.micromodules.Cooldowns;
import com.easterlyn.micromodules.Spectators;
import com.easterlyn.users.Region;
import com.easterlyn.users.User;
import com.easterlyn.users.UserRank;
import com.easterlyn.users.Users;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

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
	private final SimpleDateFormat time =  new SimpleDateFormat("m:ss");
	private final HashMap<UUID, TeleportRequest> pending = new HashMap<>();

	public TeleportRequestCommand(Easterlyn plugin) {
		super(plugin, "tpa");
		this.setAliases("tpask", "call", "tpahere", "tpaskhere", "callhere", "tpaccept", "tpyes", "tpdeny", "tpno", "tpreset");
		this.cooldowns = plugin.getModule(Cooldowns.class);
		this.spectators = plugin.getModule(Spectators.class);
		this.users = plugin.getModule(Users.class);
		this.transportalizer = (Transportalizer) plugin.getModule(Machines.class).getMachineByName("Transportalizer");
		this.addExtraPermission("reset", UserRank.HELPER);
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
		if (label.equals("tpreset")) {
			// TODO: separate command to prevent tab completion for lower perm levels
			if (player.hasPermission("easterlyn.command.tpa.reset")) {
				cooldowns.clearCooldown(player, "teleportRequest");
			}
		}
		return true;
	}

	private void ask(Player sender, String[] args, boolean here) {
		long remainder = cooldowns.getRemainder(sender, "teleportRequest");
		if (remainder > 0) {
			sender.sendMessage(getLang().getValue("command.tpa.error.cooldown")
					.replace("{TIME}", time.format(new Date(remainder))));
			if (sender.hasPermission("easterlyn.command.tpa.reset")) {
				sender.sendMessage(getLang().getValue("command.tpa.reset"));
			}
			return;
		}
		Player target = looseMatch(sender, args[0]);
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
		Region rTarget = targetUser.getCurrentRegion();
		Region rSource = sourceUser.getCurrentRegion();
		if (rTarget != rSource && !(rSource.isDream() && rTarget.isDream())) {
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
			if (!transportalizer.doPendingTransportalization(sender, true)) {
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
		if (spectators.isSpectator(toArriveAt.getUniqueId())
				&& !spectators.isSpectator(toTeleport.getUniqueId())) {
			getLang();
			String message = getLang().getValue("command.tpa.error.toSpectator");
			toTeleport.sendMessage(message);
			toArriveAt.sendMessage(message);
			return;
		}
		Region rTarget = users.getUser(toArriveAt.getUniqueId()).getCurrentRegion();
		Region rSource = users.getUser(toTeleport.getUniqueId()).getCurrentRegion();
		if (rTarget != rSource && !(rSource.isDream() && rTarget.isDream())) {
			getLang();
			String message = getLang().getValue("command.tpa.error.crossRegion");
			toTeleport.sendMessage(message);
			toArriveAt.sendMessage(message);
			return;
		}
		toTeleport.teleport(toArriveAt);
		toTeleport.sendMessage(getLang().getValue("command.tpa.success.arrive.teleported")
						.replace("{PLAYER}", toArriveAt.getDisplayName()));
		toArriveAt.sendMessage(getLang().getValue("command.tpa.success.arrive.target")
				.replace("{PLAYER}", toTeleport.getDisplayName()));

		// Teleporting as a spectator is a legitimate mechanic, no cooldown.
		Player issuer = request.isHere() ? toArriveAt : toTeleport;
		if (spectators.isSpectator(toTeleport.getUniqueId())) {
			cooldowns.clearCooldown(issuer, "teleportRequest");
		} else {
			cooldowns.addCooldown(issuer, "teleportRequest", getPlugin().getConfig().getLong("command.tpa.cooldown", 3600000L));
		}
	}

	private void decline(Player sender) {
		TeleportRequest request = pending.remove(sender.getUniqueId());
		if (request == null || request.getExpiry() < System.currentTimeMillis()) {
			if (!transportalizer.doPendingTransportalization(sender, false)) {
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
		public TeleportRequest(UUID source, UUID target, boolean here) {
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
		public long getExpiry() {
			return expiry;
		}
	}

	private Player looseMatch(Player sender, String name) {
		List<Player> matches = Bukkit.matchPlayer(name);
		matches.removeIf(target -> !sender.canSee(target));
		if (!matches.isEmpty()) {
			return matches.get(0);
		}
		name = name.toLowerCase();
		matches = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!sender.canSee(player) || player.getDisplayName() == null) {
				continue;
			}
			String display = ChatColor.stripColor(player.getDisplayName());
			if (display.equalsIgnoreCase(name)) {
				return player;
			}
			if (display.toLowerCase().startsWith(name)) {
				matches.add(player);
			}
		}
		if (!matches.isEmpty()) {
			return matches.get(0);
		}
		return null;
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
