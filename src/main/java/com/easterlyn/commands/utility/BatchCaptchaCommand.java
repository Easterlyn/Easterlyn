package com.easterlyn.commands.utility;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;
import com.google.common.collect.ImmutableList;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

/**
 * EasterlynCommand for creating lots of captchas of items at a time.
 *
 * @author Jikoo
 */
public class BatchCaptchaCommand extends EasterlynCommand {

	private final Captcha captcha;

	public BatchCaptchaCommand(Easterlyn plugin) {
		super(plugin, "baptcha");
		this.setAliases("batchcap", "capbatch", "batchcaptcha", "captchabatch");
		this.captcha = plugin.getModule(Captcha.class);
		this.addExtraPermission("free", UserRank.MOD);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item == null || item.getType() == Material.AIR) {
			return false;
		}

		if (captcha.canNotCaptcha(item)) {
			player.sendMessage(getLang().getValue("captcha.uncaptchable"));
			return true;
		}

		if (item.getAmount() != item.getType().getMaxStackSize()) {
			player.sendMessage(getLang().getValue("command.baptcha.stack"));
			return true;
		}

		PlayerInventory inventory = player.getInventory();
		ItemStack blankCaptcha = Captcha.getBlankCaptchacard();

		int max;
		if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("easterlyn.command.baptcha.free")
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
			player.sendMessage(getLang().getValue("command.baptcha.noCaptchas"));
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
		item = captcha.getCaptchaForItem(item);
		item.setAmount(count);
		player.getInventory().addItem(item);
		player.sendMessage(getLang().getValue("command.baptcha.success").replace("{COUNT}", String.valueOf(count)));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (args.length < 2 && sender instanceof Player && sender.hasPermission("easterlyn.command.baptcha.free")) {
			return ImmutableList.of("free");
		}
		return ImmutableList.of();
	}
}
