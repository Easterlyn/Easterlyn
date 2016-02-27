package co.sblock.commands.teleportation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Transportalizer;
import co.sblock.micromodules.Cooldowns;
import co.sblock.micromodules.Spectators;
import co.sblock.users.Region;
import co.sblock.users.User;
import co.sblock.users.Users;

/**
 * Essentials' TPA just won't cut it.
 * 
 * @author Jikoo
 */
public class TeleportRequestCommand extends SblockCommand {

	private final Cooldowns cooldowns;
	private final Spectators spectators;
	private final Users users;
	private final Transportalizer transportalizer;
	private final SimpleDateFormat time =  new SimpleDateFormat("m:ss");
	private final HashMap<UUID, TeleportRequest> pending = new HashMap<>();

	public TeleportRequestCommand(Sblock plugin) {
		super(plugin, "tpa");
		this.cooldowns = plugin.getModule(Cooldowns.class);
		this.spectators = plugin.getModule(Spectators.class);
		this.users = plugin.getModule(Users.class);
		this.transportalizer = (Transportalizer) plugin.getModule(Machines.class).getMachineByName("Transportalizer");
		this.setDescription("Handle a teleport request");
		this.setUsage("/tpa name, /tpahere name, /tpaccept, /tpdecline");
		this.setAliases("tpask", "call", "tpahere", "tpaskhere", "callhere", "tpaccept", "tpyes", "tpdeny", "tpno");
		Permission permission;
		try {
			permission = new Permission("sblock.command.tpa.nocooldown", PermissionDefault.OP);
			Bukkit.getPluginManager().addPermission(permission);
		} catch (IllegalArgumentException e) {
			permission = Bukkit.getPluginManager().getPermission("sblock.command.tpa.nocooldown");
			permission.setDefault(PermissionDefault.OP);
		}
		permission.addParent("sblock.command.*", true).recalculatePermissibles();
		permission.addParent("sblock.helper", true).recalculatePermissibles();
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		label = label.toLowerCase();
		Player player = (Player) sender;
		if (label.equals("tpa") || label.equals("tpask") || label.equals("call")) {
			if (args.length == 0) {
				sender.sendMessage(Color.COMMAND + "/tpa <player>" + Color.GOOD + ": Request to teleport to a player");
				return true;
			}
			ask(player, args, false);
			return true;
		}
		if (label.equals("tpahere") || label.equals("tpaskhere") || label.equals("callhere")) {
			if (args.length == 0) {
				sender.sendMessage(Color.COMMAND + "/tpahere <player>" + Color.GOOD + ": Request to teleport another player to you");
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
		return true;
	}

	private void ask(Player sender, String[] args, boolean here) {
		long remainder = cooldowns.getRemainder(sender, "teleportRequest");
		if (remainder > 0) {
			sender.sendMessage(Color.BAD + "You cannot send a teleport request for another "
					+ Color.BAD_EMPHASIS + time.format(new Date(remainder)) + Color.BAD + ".");
			return;
		}
		Player target = looseMatch(sender, args[0]);
		if (target == null) {
			sender.sendMessage(Color.BAD + "No player by that name could be found!");
			return;
		}
		if (target.getUniqueId().equals(sender.getUniqueId())) {
			sender.sendMessage(Color.GOOD + "If I told you I just teleported you to yourself would you believe me?");
			return;
		}
		if (!target.hasPermission(getPermission())) {
			sender.sendMessage(Color.BAD_PLAYER + target.getName() + Color.BAD + " cannot accept teleport requests!");
			return;
		}
		User targetUser = users.getUser(target.getUniqueId());
		User sourceUser = users.getUser(sender.getUniqueId());
		boolean targetSpectating = spectators.isSpectator(targetUser.getUUID());
		boolean sourceSpectating = spectators.isSpectator(sourceUser.getUUID());
		if (!here && targetSpectating && !sourceSpectating || here && !targetSpectating && sourceSpectating) {
			sender.sendMessage(Color.BAD + "Corporeal players cannot teleport to incorporeal players!");
			return;
		}
		Region rTarget = targetUser.getCurrentRegion();
		Region rSource = sourceUser.getCurrentRegion();
		if (rTarget != rSource && !(rSource.isDream() && rTarget.isDream())) {
			sender.sendMessage(Color.BAD + "Teleports cannot be initiated from different planets!");
			return;
		}
		if (pending.containsKey(target.getUniqueId()) && pending.get(target.getUniqueId()).getExpiry() > System.currentTimeMillis()) {
			sender.sendMessage(Color.BAD_PLAYER + target.getDisplayName() +  Color.BAD + " has a pending request already.");
			return;
		}
		pending.put(target.getUniqueId(), new TeleportRequest(sender.getUniqueId(), target.getUniqueId(), here));
		if (!sender.hasPermission("sblock.command.tpa.nocooldown")) {
			cooldowns.addCooldown(sender, "teleportRequest", 480000L);
		}
		sender.sendMessage(Color.GOOD + "Request sent!");
		target.sendMessage(Color.GOOD_PLAYER + sender.getDisplayName() + Color.GOOD + " is requesting to teleport " + (here ? "you to them." : "to you."));
		target.sendMessage(Color.GOOD + "To accept, use " + Color.COMMAND + "/tpyes"
				+ Color.GOOD + ". To decline, use " + Color.COMMAND + "/tpno" + Color.GOOD + ".");
		target.sendMessage(Color.GOOD + "This request will expire in 60 seconds.");
	}

	private void accept(Player sender) {
		TeleportRequest request = pending.remove(sender.getUniqueId());
		if (request == null || request.getExpiry() < System.currentTimeMillis()) {
			if (!transportalizer.doPendingTransportalization(sender, true)) {
				sender.sendMessage(Color.BAD + "You do not have any pending teleport requests.");
			}
			return;
		}
		Player toTeleport = Bukkit.getPlayer(request.isHere() ? request.getTarget() : request.getSource());
		Player toArriveAt = Bukkit.getPlayer(request.isHere() ? request.getSource() : request.getTarget());
		if (toTeleport == null || toArriveAt == null) {
			sender.sendMessage(Color.BAD + "The issuer of the request seems to have logged off.");
			return;
		}
		if (spectators.isSpectator(toArriveAt.getUniqueId())
				&& !spectators.isSpectator(toTeleport.getUniqueId())) {
			String message = Color.BAD + "Corporeal players cannot teleport to incorporeal players!";
			toTeleport.sendMessage(message);
			toArriveAt.sendMessage(message);
			return;
		}
		Region rTarget = users.getUser(toArriveAt.getUniqueId()).getCurrentRegion();
		Region rSource = users.getUser(toTeleport.getUniqueId()).getCurrentRegion();
		if (rTarget != rSource && !(rSource.isDream() && rTarget.isDream())) {
			String message = Color.BAD + "Teleports cannot be initiated from different planets!";
			toTeleport.sendMessage(message);
			toArriveAt.sendMessage(message);
			return;
		}
		toTeleport.teleport(toArriveAt);
		toTeleport.sendMessage(Color.GOOD + "Teleporting to " + Color.GOOD_PLAYER
				+ toArriveAt.getDisplayName() + Color.GOOD + ".");
		toArriveAt.sendMessage(Color.GOOD + "Teleported " + Color.GOOD_PLAYER
				+ toTeleport.getDisplayName() + Color.GOOD + " to you.");

		// Teleporting as a spectator is a legitimate mechanic, no cooldown.
		Player issuer = request.isHere() ? toArriveAt : toTeleport;
		if (issuer.hasPermission("sblock.command.tpa.nocooldown")
				|| spectators.isSpectator(toTeleport.getUniqueId())) {
			cooldowns.clearCooldown(issuer, "teleportRequest");
		} else {
			cooldowns.addCooldown(issuer, "teleportRequest", 3600000L);
		}
	}

	private void decline(Player sender) {
		TeleportRequest request = pending.remove(sender.getUniqueId());
		if (request == null || request.getExpiry() < System.currentTimeMillis()) {
			if (!transportalizer.doPendingTransportalization(sender, false)) {
				sender.sendMessage(Color.BAD + "You do not have any pending teleport requests.");
			}
			return;
		}
		sender.sendMessage(Color.GOOD + "Request declined!");
		Player issuer = Bukkit.getPlayer(request.getSource());
		if (issuer != null) {
			issuer.sendMessage(Color.BAD_PLAYER + sender.getDisplayName() + Color.BAD + " declined your request!");
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
			if (player.getDisplayName().equalsIgnoreCase(name)) {
				return player;
			}
			if (player.getDisplayName().toLowerCase().startsWith(name)) {
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
