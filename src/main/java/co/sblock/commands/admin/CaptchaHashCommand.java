package co.sblock.commands.admin;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock;
import co.sblock.captcha.Captcha;
import co.sblock.commands.SblockCommand;

import net.md_5.bungee.api.ChatColor;

/**
 * SblockCommand for creating a new unique hash for an item or getting a captcha for a hash.
 * 
 * @author Jikoo
 */
public class CaptchaHashCommand extends SblockCommand {

	private final Captcha captcha;

	public CaptchaHashCommand(Sblock plugin) {
		super(plugin, "hash");
		this.setPermissionLevel("denizen");
		this.setPermissionMessage(ChatColor.GOLD + "BROWNS!");
		this.captcha = plugin.getModule(Captcha.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args.length < 2) {
			return false;
		}
		if (!args[1].matches("[0-9A-Za-z]{8,}")) {
			sender.sendMessage(getLang().getValue("command.hash.requirements"));
			return true;
		}
		args[0] = args[0].toLowerCase();
		Player player = (Player) sender;
		if (args[0].equals("add")) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if (item == null || item.getType() == Material.AIR) {
				return false;
			}
			if (captcha.addCustomHash(args[1], item)) {
				sender.sendMessage(getLang().getValue("command.hash.success_save").replace("{TARGET}", args[1]));
				return true;
			} else {
				sender.sendMessage(getLang().getValue("command.hash.used").replace("{TARGET}", args[1]));
				return true;
			}
		}
		if (args[0].equals("get")) {
			ItemStack item = captcha.getCaptchaFor(args[1]);
			if (item == null) {
				sender.sendMessage(getLang().getValue("command.hash.unused"));
				return true;
			}
			player.getWorld().dropItem(player.getLocation(), item).setPickupDelay(0);
			player.sendMessage(getLang().getValue("command.hash.success_load").replace("{TARGET}", args[1]));
			return true;
		}
		return false;
	}

}
