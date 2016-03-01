package co.sblock.commands.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock;
import co.sblock.captcha.Captcha;
import co.sblock.chat.Color;
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
		this.setDescription("Create a new hash for your item in hand, or get a captcha for a specific hash.");
		this.setUsage("/hash get <hash> | With item in main hand, /hash add <hash>");
		this.setPermissionLevel("denizen");
		this.setPermissionMessage(ChatColor.GOLD + "BROWNS!");
		captcha = plugin.getModule(Captcha.class);
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
			sender.sendMessage(Color.BAD + "Hashes must be 8 or more characters containing only 0-9, A-Z, a-z.");
			return true;
		}
		args[0] = args[0].toLowerCase();
		Player player = (Player) sender;
		if (args[0].equals("add")) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if (item == null) {
				return false;
			}
			if (captcha.saveItemStack(args[1], item)) {
				sender.sendMessage(Color.GOOD + "Saved to " + args[1]);
				return true;
			} else {
				sender.sendMessage(Color.BAD + "Hash " + args[1]
						+ " is already in use. Manually delete the file if you're sure there will be no conflict.");
				return true;
			}
		}
		if (args[0].equals("get")) {
			ItemStack item = captcha.getCaptchaFor(args[1]);
			if (item == null) {
				sender.sendMessage(Color.BAD + "No item is stored by that hash.");
				return true;
			}
			player.getWorld().dropItem(player.getLocation(), item).setPickupDelay(0);
			player.sendMessage(Color.GOOD + "Loaded captcha of item for " + args[1]);
			return true;
		}
		return false;
	}

}
