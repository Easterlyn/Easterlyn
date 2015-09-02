package co.sblock.commands.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.micromodules.Spectators;
import co.sblock.users.OfflineUser;
import co.sblock.users.Region;
import co.sblock.users.Users;
import co.sblock.utilities.Cooldowns;

/**
 * Essentials' TPA just won't cut it.
 * 
 * @author Jikoo
 */
public class TeleportRequestCommand extends SblockCommand {

	private final SimpleDateFormat time =  new SimpleDateFormat("m:ss");
	private final HashMap<UUID, TeleportRequest> pending = new HashMap<>();

	public TeleportRequestCommand() {
		super("tpa");
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
		// entry
//		this.setPermissionLevel("hero");
//		this.setPermissionMessage("You must complete classpect selection before you can teleport!");
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
		long remainder = Cooldowns.getInstance().getRemainder(sender, "teleportRequest");
		if (remainder > 0) {
			sender.sendMessage(Color.BAD + "You cannot send a teleport request for another "
					+ Color.BAD_EMPHASIS + time.format(new Date(remainder)) + Color.BAD + ".");
			return;
		}
		List<Player> matches = Bukkit.matchPlayer(args[0]);
		matches.removeIf(target -> !sender.canSee(target));
		if (matches.isEmpty()) {
			sender.sendMessage(Color.BAD + "No player by that name could be found!");
			return;
		}
		Player target = matches.get(0);
		if (target.getUniqueId().equals(sender.getUniqueId())) {
			sender.sendMessage(Color.GOOD + "If I told you I just teleported you to yourself would you believe me?");
			return;
		}
		if (!target.hasPermission(getPermission())) {
			sender.sendMessage(Color.BAD_PLAYER + target.getName() + Color.BAD + " cannot accept teleport requests!");
			return;
		}
		OfflineUser targetUser = Users.getGuaranteedUser(target.getUniqueId());
		OfflineUser sourceUser = Users.getGuaranteedUser(sender.getUniqueId());
		Spectators spectators = Spectators.getInstance();
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
		if (!sender.hasPermission("group.helper")) {
			Cooldowns.getInstance().addCooldown(sender, "teleportRequest", 3600000L);
		}
		sender.sendMessage(Color.GOOD + "Request sent!");
		target.sendMessage(Color.GOOD_PLAYER + sender.getDisplayName() + Color.GOOD + " is requesting to teleport " + (here ? "you to them." : "to you."));
		target.sendMessage(Color.GOOD + "To accept, use " + Color.COMMAND + "/tpyes"
				+ Color.GOOD + ". To decline, use " + Color.COMMAND + "/tpno" + Color.GOOD + ".");
		target.sendMessage(Color.GOOD + "This request will expire in 45 seconds.");
	}

	private void accept(Player sender) {
		TeleportRequest request = pending.remove(sender.getUniqueId());
		if (request == null || request.getExpiry() < System.currentTimeMillis()) {
			sender.sendMessage(Color.BAD + "You do not have any pending teleport requests.");
			return;
		}
		Player toTeleport = Bukkit.getPlayer(request.isHere() ? request.getTarget() : request.getSource());
		Player toArriveAt = Bukkit.getPlayer(request.isHere() ? request.getSource() : request.getTarget());
		if (toTeleport == null || toArriveAt == null) {
			sender.sendMessage(Color.BAD + "The issuer of the request seems to have logged off.");
			return;
		}
		if (Spectators.getInstance().isSpectator(toArriveAt.getUniqueId())
				&& !Spectators.getInstance().isSpectator(toTeleport.getUniqueId())) {
			String message = Color.BAD + "Corporeal players cannot teleport to incorporeal players!";
			toTeleport.sendMessage(message);
			toArriveAt.sendMessage(message);
			return;
		}
		Region rTarget = Users.getGuaranteedUser(toArriveAt.getUniqueId()).getCurrentRegion();
		Region rSource = Users.getGuaranteedUser(toTeleport.getUniqueId()).getCurrentRegion();
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
		if (Spectators.getInstance().isSpectator(toTeleport.getUniqueId())) {
			Cooldowns.getInstance().clearCooldown(request.isHere() ? toArriveAt : toTeleport, "teleportRequest");
		}
	}

	private void decline(Player sender) {
		TeleportRequest request = pending.remove(sender.getUniqueId());
		if (request == null || request.getExpiry() < System.currentTimeMillis()) {
			sender.sendMessage(Color.BAD + "You do not have any pending teleport requests.");
			return;
		}
		sender.sendMessage(Color.GOOD + "Request declined!");
		Player issuer = Bukkit.getPlayer(request.getSource());
		if (issuer != null) {
			issuer.sendMessage(Color.BAD_PLAYER + sender.getDisplayName() + Color.BAD + " declined your request!");
			Cooldowns.getInstance().clearCooldown(issuer, "teleportRequest");
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
			this.expiry = System.currentTimeMillis() + 45000L;
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

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission()) || args.length != 1) {
			return com.google.common.collect.ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}
}
