package com.easterlyn.captcha.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCaptchas;
import com.easterlyn.command.CommandRank;
import com.easterlyn.users.UserRank;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@CommandAlias("captcha")
public class CaptchaCommand extends BaseCommand {

	@Dependency
	private EasterlynCaptchas captcha;

	@Subcommand("add")
	@Description("Add a custom captcha hash.")
	@Syntax("<hash>")
	@CommandRank(UserRank.ADMIN)
	public void add(BukkitCommandIssuer issuer, @Single String hash) {
		if (!issuer.isPlayer()) {
			throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE);
		}
		if (!hash.matches("[0-9A-Za-z]{8,}")) {
			issuer.sendMessage("command.hash.requirements");
			return;
		}

		ItemStack item = issuer.getPlayer().getInventory().getItemInMainHand();
		if (item.getType() == Material.AIR) {
			issuer.sendMessage("command.hash.requirements");
			return;
		}
		if (captcha.addCustomHash(hash, item)) {
			issuer.sendMessage("command.hash.success_save".replace("{TARGET}", hash));
		} else {
			issuer.sendMessage("command.hash.used".replace("{TARGET}", hash));
		}
	}

	@Subcommand("get")
	@Description("Get a captcha by hash.")
	// TODO command completion
	@Syntax("<hash>")
	@CommandRank(UserRank.ADMIN)
	public void get(BukkitCommandIssuer issuer, @Single String hash) {
		if (!issuer.isPlayer()) {
			throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE);
		}
		ItemStack item = captcha.getCaptchaForHash(hash);
		if (item == null) {
			issuer.sendMessage("command.hash.unused");
			return;
		}
		issuer.getPlayer().getWorld().dropItem(issuer.getPlayer().getLocation(), item).setPickupDelay(0);
		issuer.sendMessage("command.hash.success_load".replace("{TARGET}", hash));
	}

	@CommandAlias("baptcha|batchcap|batchcaptcha")
	@Description("Captcha in bulk!")
	@Syntax("Run with an item to batch captcha in hand.")
	public void baptcha(BukkitCommandIssuer issuer, @Optional String free) {
		if (!issuer.isPlayer()) {
			throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE);
		}
		Player player = issuer.getPlayer();
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
		item.setAmount(count);
		player.getInventory().addItem(item);
		player.sendMessage("command.baptcha.success".replace("{COUNT}", String.valueOf(count)));
	}

	@CommandAlias("convert")
	@Description("Convert captchas whose hashes have changed.")
	@Syntax("Run with an item to batch captcha in hand.")
	public void convert(BukkitCommandIssuer issuer) {
		if (!issuer.isPlayer()) {
			throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE);
		}
		Player player = issuer.getPlayer();
		int convert = captcha.convert(player);
		player.sendMessage("Converted " + convert + " captchas!");
	}

}
