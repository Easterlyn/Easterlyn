package co.sblock.commands.utility;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.captcha.Captcha;
import co.sblock.chat.Language;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for creating lots of captchas of items at a time.
 * 
 * @author Jikoo
 */
public class BatchCaptchaCommand extends SblockCommand {

	private final Captcha captcha;

	public BatchCaptchaCommand(Sblock plugin) {
		super(plugin, "baptcha");
		this.setAliases("batchcap", "capbatch", "batchcaptcha", "captchabatch");
		this.setDescription("Captchalogues all items in your inventory matching your item in main hand!");
		this.setUsage("Hold an item in main hand, run /baptcha. Batch captcha!");

		Permission permission;
		try {
			permission = new Permission("sblock.command.baptcha.free", PermissionDefault.OP);
			Bukkit.getPluginManager().addPermission(permission);
		} catch (IllegalArgumentException e) {
			permission = Bukkit.getPluginManager().getPermission("sblock.command.baptcha.free");
			permission.setDefault(PermissionDefault.OP);
		}
		permission.addParent("sblock.command.*", true).recalculatePermissibles();
		permission.addParent("sblock.denizen", true).recalculatePermissibles();
		this.captcha = plugin.getModule(Captcha.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item == null || item.getType() == Material.AIR) {
			return false;
		}

		if (!captcha.canCaptcha(item)) {
			player.sendMessage(Language.getColor("bad") + "That item cannot be put in a captcha!");
			return true;
		}

		PlayerInventory inventory = player.getInventory();
		ItemStack blankCaptcha = Captcha.blankCaptchaCard();

		int max;
		if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("sblock.command.baptcha.free")
				&& args.length > 0 && args[0].equals("free")) {
			max = Integer.MAX_VALUE;
		} else {
			max = 0;
			for (int i = 0; i < inventory.getSize(); i++) {
				if (i == inventory.getHeldItemSlot()) {
					// Skip hand, it's the target stack.
					continue;
				}
				ItemStack slot = inventory.getItem(i);
				if (blankCaptcha.isSimilar(slot)) {
					max += slot.getAmount();
				}
			}
		}

		if (max == 0) {
			player.sendMessage(Language.getColor("bad") + "You don't have any blank captchas to use!");
			return true;
		}

		boolean blank = Captcha.isBlankCaptcha(item);

		int count = 0;
		for (int i = 0; count < max && i < inventory.getSize(); i++) {
			if (item.equals(inventory.getItem(i))) {
				inventory.setItem(i, null);
				if (blank && max != Integer.MAX_VALUE && inventory.removeItem(blankCaptcha).size() > 0) {
					// Blank captchas are required - if they're being stored as well, we need to store as we go or risk running out.
					inventory.setItem(i, item.clone());
					break;
				}
				count++;
			}
		}

		if (!blank && max != Integer.MAX_VALUE) {
			blankCaptcha.setAmount(count);
			// Not bothering catching failed removals here, there should be none.
			inventory.removeItem(blankCaptcha);
		}
		item = captcha.itemToCaptcha(item);
		item.setAmount(count);
		player.getInventory().addItem(item);
		player.sendMessage(Language.getColor("good") + "Used " + count + " captchas to store items.");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (args.length < 2 && sender instanceof Player && sender.hasPermission("sblock.command.baptcha.free")) {
			return ImmutableList.of("free");
		}
		return ImmutableList.of();
	}
}
