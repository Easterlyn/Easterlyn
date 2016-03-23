package co.sblock.commands.info;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.commands.SblockAsynchronousCommand;
import co.sblock.discord.DiscordPlayer;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * 
 * 
 * @author Jikoo
 */
public class CoordsCommand extends SblockAsynchronousCommand {

	public CoordsCommand(Sblock plugin) {
		super(plugin, "coords");
		this.setAliases("getpos", "loc", "pos", "whereami");
		this.setDescription("Get your current coordinates.");
		this.setUsage("/coords [player]");
		Permission permission;
		try {
			permission = new Permission("sblock.command.coords.other", PermissionDefault.OP);
			Bukkit.getPluginManager().addPermission(permission);
		} catch (IllegalArgumentException e) {
			permission = Bukkit.getPluginManager().getPermission("sblock.command.coords.other");
			permission.setDefault(PermissionDefault.OP);
		}
		permission.addParent("sblock.command.*", true).recalculatePermissibles();
		permission.addParent("sblock.felt", true).recalculatePermissibles();
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)
				&& (args.length == 0 || !sender.hasPermission("sblock.command.coords.other"))) {
			return false;
		}
		final boolean other = args.length >= 1 && sender.hasPermission("sblock.command.coords.other");
		final Player target;
		if (other) {
			final UUID uuid = getUniqueId(args[0]);
			if (uuid == null || (target = Bukkit.getPlayer(uuid)) == null) {
				sender.sendMessage(getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[0]));
				return true;
			}
		} else {
			target = (Player) sender;
		}
		Location loc = target.getLocation();
		String baseMessage = String.format("%1$sOn %2$s%3$s%1$s at %2$s%4$.1f%1$s, "
					+ "%2$s%5$.1f%1$s, %2$s%6$.1f%1$s, %2$s%7$.1f%1$s pitch, and %2$s%8$.1f%1$s yaw.",
			Language.getColor("good"), Language.getColor("emphasis.good"), loc.getWorld().getName(), loc.getX(),
			loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw());
		if (!(sender instanceof Player) || sender instanceof DiscordPlayer) {
			sender.sendMessage(baseMessage);
			return true;
		}
		TextComponent raw = new TextComponent(TextComponent.fromLegacyText(baseMessage));
		raw.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
				String.format("%1$s %2$.0f %3$.0f %4$.0f %5$.0f %6$.0f", loc.getWorld().getName(),
						loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw())));
		raw.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText(Language.getColor("good") + "Click to insert into chat!")));
		((Player) sender).spigot().sendMessage(raw);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission("sblock.command.coords.other") || args.length != 1) {
			return com.google.common.collect.ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}

}
