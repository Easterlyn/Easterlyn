package co.sblock.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.users.Region;
import co.sblock.users.Users;

/**
 * Essentials' TPA just won't cut it.
 * 
 * @author Jikoo
 */
public class TeleportRequestCommand extends SblockCommand {

	private final SimpleDateFormat time =  new SimpleDateFormat("m:ss");
	private final HashMap<UUID, Long> tpacooldown = new HashMap<>();
	private final HashMap<UUID, TeleportRequest> pending = new HashMap<>();

	public TeleportRequestCommand() {
		super("tpa");
		this.setDescription("Handle a teleport request");
		this.setUsage("/tpa name, /tpahere name, /tpaccept, /tpdecline");
		this.setAliases("tpask", "call", "tpahere", "tpaskhere", "callhere", "tpaccept", "tpyes", "tpdeny", "tpno");
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
				sender.sendMessage(ChatColor.AQUA + "/tpa <player>" + ChatColor.RED + " - Request to teleport to a player");
				return true;
			}
			ask(player, args, false);
			return true;
		}
		if (label.equals("tpahere") || label.equals("tpaskhere") || label.equals("callhere")) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.AQUA + "/tpahere <player>" + ChatColor.RED + " - Request to teleport another player to you");
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
		if (tpacooldown.containsKey(sender.getUniqueId())
				&& tpacooldown.get(sender.getUniqueId()) > System.currentTimeMillis()) {
			sender.sendMessage(ChatColor.RED + "You cannot send a teleport request for another " + ChatColor.GOLD
					+ time.format(new Date(tpacooldown.get(sender.getUniqueId()) - System.currentTimeMillis()))
					+ ChatColor.RED + ".");
			return;
		}
		List<Player> matches = Bukkit.matchPlayer(args[0]);
		matches.removeIf(target -> !sender.canSee(target));
		if (matches.isEmpty()) {
			sender.sendMessage(ChatColor.RED + "No player by that name could be found!");
			return;
		}
		Player target = matches.get(0);
		if (target.getUniqueId().equals(sender.getUniqueId())) {
			sender.sendMessage(ChatColor.YELLOW + "If I told you I just teleported you to yourself would you believe me?");
			return;
		}
		Region rTarget = Users.getGuaranteedUser(target.getUniqueId()).getCurrentRegion();
		Region rSource = Users.getGuaranteedUser(sender.getUniqueId()).getCurrentRegion();
		if (rTarget != rSource && !(rSource.isDream() && rTarget.isDream())) {
			// TODO re-check on accept?
			sender.sendMessage(ChatColor.RED + "Teleports cannot be initiated across different planets!");
			return;
		}
		if (pending.containsKey(target.getUniqueId()) && pending.get(target.getUniqueId()).getExpiry() > System.currentTimeMillis()) {
			sender.sendMessage(ChatColor.GOLD + target.getDisplayName() +  ChatColor.RED + " has a pending request already.");
			return;
		}
		pending.put(target.getUniqueId(), new TeleportRequest(sender.getUniqueId(), target.getUniqueId(), here));
		if (!sender.hasPermission("group.helper")) {
			tpacooldown.put(sender.getUniqueId(), System.currentTimeMillis() + 480000L);
		}
		sender.sendMessage(ChatColor.YELLOW + "Request sent!");
		target.sendMessage(ChatColor.DARK_AQUA + sender.getDisplayName() + ChatColor.YELLOW + " is requesting to teleport " + (here ? "you to them." : "to you."));
		target.sendMessage(ChatColor.YELLOW + "To accept, use " + ChatColor.AQUA + "/tpaccept"
				+ ChatColor.YELLOW + ". To decline, use " + ChatColor.AQUA + "/tpdeny" + ChatColor.YELLOW + ".");
		target.sendMessage(ChatColor.YELLOW + "This request will expire in 30 seconds.");
	}

	private void accept(Player sender) {
		TeleportRequest request = pending.remove(sender.getUniqueId());
		if (request == null || request.getExpiry() < System.currentTimeMillis()) {
			sender.sendMessage(ChatColor.RED + "You do not have any pending teleport requests.");
			return;
		}
		Player toTeleport = Bukkit.getPlayer(request.isHere() ? request.getTarget() : request.getSource());
		Player toArriveAt = Bukkit.getPlayer(request.isHere() ? request.getSource() : request.getTarget());
		if (toTeleport == null || toArriveAt == null) {
			sender.sendMessage(ChatColor.RED + "The issuer of the request seems to have logged off.");
			return;
		}
		toTeleport.teleport(toArriveAt);
		toTeleport.sendMessage(ChatColor.YELLOW + "Teleporting to " + ChatColor.DARK_AQUA
				+ toArriveAt.getDisplayName() + ChatColor.YELLOW + ".");
		toArriveAt.sendMessage(ChatColor.YELLOW + "Teleported " + ChatColor.DARK_AQUA
				+ toTeleport.getDisplayName() + ChatColor.YELLOW + " to you.");
	}

	private void decline(Player sender) {
		TeleportRequest request = pending.remove(sender.getUniqueId());
		if (request == null || request.getExpiry() < System.currentTimeMillis()) {
			sender.sendMessage(ChatColor.RED + "You do not have any pending teleport requests.");
			return;
		}
		sender.sendMessage(ChatColor.YELLOW + "Request declined!");
		Player issuer = Bukkit.getPlayer(request.getSource());
		if (issuer != null) {
			issuer.sendMessage(ChatColor.GOLD + sender.getDisplayName() + ChatColor.RED + " declined your request!");
		}
		tpacooldown.remove(request.getSource());
	}

	private class TeleportRequest {
		private UUID source;
		private UUID target;
		boolean here;
		private long expiry;
		public TeleportRequest(UUID source, UUID target, boolean here) {
			this.source = source;
			this.target = target;
			this.here = here;
			this.expiry = System.currentTimeMillis() + 30000L;
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
}
