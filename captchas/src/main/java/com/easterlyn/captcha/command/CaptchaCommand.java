package com.easterlyn.captcha.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCaptchas;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.PermissionUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@CommandAlias("captcha")
public class CaptchaCommand extends BaseCommand {

	@Dependency
	private EasterlynCaptchas captcha;

	public CaptchaCommand() {
		PermissionUtil.addParent("easterlyn.command.baptcha.free", UserRank.MODERATOR.getPermission());
	}

	@Subcommand("add")
	@Description("Add a custom alphanumeric captcha hash.")
	@Syntax("/captcha add <alphanumeric 8+ character hash>")
	@CommandPermission("easterlyn.command.captcha.add")
	public void add(@Flags(CoreContexts.SELF) Player player, @Single String hash) {
		if (!hash.matches("[0-9A-Za-z]{8,}")) {
			player.sendMessage("command.hash.requirements");
			return;
		}

		ItemStack item = player.getInventory().getItemInMainHand();
		if (item.getType() == Material.AIR) {
			player.sendMessage("command.hash.requirements");
			return;
		}
		if (captcha.addCustomHash(hash, item)) {
			player.sendMessage("command.hash.success_save".replace("{TARGET}", hash));
		} else {
			player.sendMessage("command.hash.used".replace("{TARGET}", hash));
		}
	}

	@Subcommand("get")
	@Description("Get a captcha by hash.")
	@Syntax("/captcha get <valid hash>")
	@CommandCompletion("@captcha")
	@CommandPermission("easterlyn.command.captcha.get")
	public void get(@Flags(CoreContexts.SELF) Player player, @Single String hash) {
		ItemStack item = captcha.getCaptchaForHash(hash);
		if (item == null) {
			player.sendMessage("command.hash.unused");
			return;
		}
		player.getWorld().dropItem(player.getLocation(), item).setPickupDelay(0);
		player.sendMessage("command.hash.success_load".replace("{TARGET}", hash));
	}

	@Subcommand("batch")
	@CommandAlias("baptcha")
	@Description("Captcha in bulk!")
	@Syntax("Run with an item to batch captcha in hand.")
	@CommandPermission("easterlyn.command.captcha.batch")
	@CommandCompletion("@permission:value=easterlyn.command.baptcha.free,complete=free")
	public void baptcha(@Flags(CoreContexts.SELF) Player player, @Optional @CommandPermission("easterlyn.command.baptcha.free") String free) {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item.getType() == Material.AIR || captcha.canNotCaptcha(item)) {
			player.sendMessage("captcha.uncaptchable");
			return;
		}

		if (item.getAmount() != item.getType().getMaxStackSize()) {
			player.sendMessage("command.baptcha.stack");
			return;
		}

		PlayerInventory inventory = player.getInventory();
		ItemStack blankCaptcha = EasterlynCaptchas.getBlankCaptchacard();

		int max;
		if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("easterlyn.command.baptcha.free")
				&& "free".equalsIgnoreCase(free)) {
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
					//noinspection ConstantConditions // isSimilar guarantees item is not null
					max += slot.getAmount();
				}
			}
		}

		if (max == 0) {
			player.sendMessage("command.baptcha.noCaptchas");
			return;
		}

		boolean blank = EasterlynCaptchas.isBlankCaptcha(item);

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
		if (item != null) {
			item.setAmount(count);
		}
		player.getInventory().addItem(item);
		player.sendMessage("command.baptcha.success".replace("{COUNT}", String.valueOf(count)));
	}

	@CommandAlias("convert")
	@Description("Convert captchas whose hashes have changed.")
	@Syntax("Run with an item to batch captcha in hand.")
	@CommandPermission("easterlyn.command.captcha.convert")
	public void convert(@Flags(CoreContexts.SELF) Player player) {
		int convert = captcha.convert(player);
		player.sendMessage("Converted " + convert + " captchas!");
	}

}
