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
import co.aikar.locales.MessageKey;
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
	@Description("{@@captcha.commands.captcha.add.description}")
	@Syntax("<hash>")
	@CommandPermission("easterlyn.command.captcha.add")
	public void add(@Flags(CoreContexts.SELF) Player player, @Single String hash) {
		if (!hash.matches("[0-9A-Za-z]{8,}")) {
			getCurrentCommandIssuer().sendInfo(MessageKey.of("captcha.commands.captcha.add.requirements"));
			return;
		}

		ItemStack item = player.getInventory().getItemInMainHand();
		if (item.getType() == Material.AIR) {
			getCurrentCommandIssuer().sendInfo(MessageKey.of("core.common.no_item"));
			return;
		}
		if (captcha.addCustomHash(hash, item)) {
			getCurrentCommandIssuer().sendInfo(MessageKey.of("captcha.commands.captcha.add.success"));
		} else {
			getCurrentCommandIssuer().sendInfo(MessageKey.of("captcha.commands.captcha.add.in_use"));
		}
	}

	@Subcommand("get")
	@Description("{@@captcha.commands.captcha.get.description}")
	@Syntax("<hash>")
	@CommandCompletion("@captcha")
	@CommandPermission("easterlyn.command.captcha.get")
	public void get(@Flags(CoreContexts.SELF) Player player, @Single String hash) {
		ItemStack item = captcha.getCaptchaForHash(hash);
		if (item == null) {
			getCurrentCommandIssuer().sendInfo(MessageKey.of("captcha.commands.captcha.get.invalid"));
			return;
		}
		player.getWorld().dropItem(player.getLocation(), item).setPickupDelay(0);
		getCurrentCommandIssuer().sendInfo(MessageKey.of("captcha.commands.captcha.get.success"));
	}

	@Subcommand("batch")
	@CommandAlias("baptcha")
	@Description("")
	@Syntax("")
	@CommandPermission("easterlyn.command.captcha.batch")
	@CommandCompletion("@permission:value=easterlyn.command.baptcha.free,complete=free")
	public void baptcha(@Flags(CoreContexts.SELF) Player player,
			@Optional @Single @CommandPermission("easterlyn.command.captcha.batch.free") String free) {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item.getType() == Material.AIR || captcha.canNotCaptcha(item)) {
			getCurrentCommandIssuer().sendInfo(MessageKey.of("captcha.commands.captcha.batch.invalid"));
			return;
		}

		if (item.getAmount() != item.getType().getMaxStackSize()) {
			getCurrentCommandIssuer().sendInfo(MessageKey.of("captcha.commands.captcha.batch.under_max"));
			return;
		}

		PlayerInventory inventory = player.getInventory();
		ItemStack blankCaptcha = EasterlynCaptchas.getBlankCaptchacard();

		int max;
		if (player.getGameMode() == GameMode.CREATIVE || player.hasPermission("easterlyn.command.captcha.batch.free")
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
			getCurrentCommandIssuer().sendInfo(MessageKey.of("captcha.commands.captcha.batch.no_captchas"));
			return;
		}

		// TODO fix blank captchas
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
			player.getInventory().addItem(item);
		}
		getCurrentCommandIssuer().sendInfo(MessageKey.of("captcha.commands.captcha.batch.success"),
				"{value}", String.valueOf(count));
	}

	@CommandAlias("convert")
	@Description("{@@captcha.commands.captcha.convert}")
	@Syntax("")
	@CommandPermission("easterlyn.command.captcha.convert")
	public void convert(@Flags(CoreContexts.SELF) Player player) {
		getCurrentCommandIssuer().sendInfo(MessageKey.of("captcha.commands.captcha.batch.success"),
				"{value}", String.valueOf(captcha.convert(player)));
	}

}
