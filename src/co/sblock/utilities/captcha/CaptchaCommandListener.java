package co.sblock.utilities.captcha;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.sblock.CommandListener;
import co.sblock.SblockCommand;

/**
 * @author Dublek, Jikoo
 */
public class CaptchaCommandListener implements CommandListener {

	/**
	 * Command used to convert an ItemStack into a Captchacard.
	 * 
	 * @param sender the CommandSender
	 * @param args the command arguments
	 * 
	 * @return true if successful
	 */
	@SblockCommand(description = "Captchalogues item in hand", usage = "/captcha")
	public boolean captcha(CommandSender sender, String[] args) {
		if (sender.isOp()) {
			Player p = (Player) sender;
			ItemStack item = p.getItemInHand();
			p.getInventory().remove(item);
			p.getInventory().addItem(Captcha.itemToCaptcha(item));
			return true;
		}
		return false;
	}

	/**
	 * Command used to convert a Captchacard into an ItemStack.
	 * 
	 * @param sender the CommandSender
	 * @param args the command arguments
	 * 
	 * @return true if successful
	 */
	@SblockCommand(description = "Uncaptchalogues item in hand", usage = "/uncaptcha")
	public boolean uncaptcha(CommandSender sender, String[] args) {
		if (sender.isOp()) {
			Player p = (Player) sender;
			ItemStack item = p.getItemInHand();
			if (item.getItemMeta().hasDisplayName()
					&& item.getItemMeta().getDisplayName().equals("Captchacard")) {
				p.getInventory().clear(p.getInventory().getHeldItemSlot());
				p.getInventory().addItem(Captcha.captchaToItem(item));
			}
			return true;
		}
		return false;
	}

	/**
	 * Command used to convert a Captchacard into a Punchcard.
	 * 
	 * @param sender the CommandSender
	 * @param args the command arguments
	 * 
	 * @return true if successful
	 */
	@SblockCommand(description = "Punches card in hand", usage = "/punchcard")
	public boolean punchcard(CommandSender sender, String[] args) {
		if (sender.isOp()) {
			Player p = (Player) sender;
			ItemStack item = p.getItemInHand();
			if (Captcha.isUsedCaptcha(item)) {
				p.getInventory().clear(p.getInventory().getHeldItemSlot());
				p.getInventory().addItem(Captchadex.punchCard(item));
				return true;
			}
			sender.sendMessage(ChatColor.RED + "Item is not a captchacard!");
			return true;
		}
		return false;
	}

	/**
	 * Command used to convert a Punchcard into a Captchacard.
	 * 
	 * @param sender the CommandSender
	 * @param args the command arguments
	 * 
	 * @return true if successful
	 */
	@SblockCommand(description = "Gives player a captchadex", usage = "/captchadex")
	public boolean captchadex(CommandSender sender, String[] args) {
		if (sender.isOp())
			((Player) sender).getInventory().addItem(Captchadex.createCaptchadexBook((Player) sender));
		return true;
	}

	@SblockCommand(description = "Converts captchacards from itemID format",
			usage = "Run /convert with a Captchacard in hand.")
	public boolean convert(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		if (!Captcha.isUsedCaptcha(player.getItemInHand())) {
			return false;
		}
		int amount = player.getItemInHand().getAmount();
		ItemStack newCard = Captcha.itemToCaptcha(Captcha.captchaToItem(player.getItemInHand()));
		newCard.setAmount(amount);
		player.setItemInHand(newCard);
		player.sendMessage(ChatColor.GREEN + "Captchacard converted!");
		return true;
	}
}
