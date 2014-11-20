package co.sblock.utilities.captcha;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.sblock.module.CommandDenial;
import co.sblock.module.CommandDescription;
import co.sblock.module.CommandListener;
import co.sblock.module.CommandPermission;
import co.sblock.module.CommandUsage;
import co.sblock.module.SblockCommand;

/**
 * @author Dublek, Jikoo
 */
public class CaptchaCommandListener implements CommandListener {

	@CommandDenial
	@CommandDescription("Captchalogues item in hand")
	@CommandPermission("group.horrorterror")
	@CommandUsage("/captcha")
	@SblockCommand
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

	@CommandDenial
	@CommandDescription("Punches card in hand. Good luck patching punched holes.")
	@CommandPermission("group.horrorterror")
	@CommandUsage("/punchcard")
	@SblockCommand
	public boolean punchcard(CommandSender sender, String[] args) {
		if (sender.isOp()) {
			Player p = (Player) sender;
			ItemStack item = p.getItemInHand();
			if (Captcha.isUsedCaptcha(item)) {
				p.getInventory().clear(p.getInventory().getHeldItemSlot());
				p.getInventory().addItem(Captcha.captchaToPunch(item));
				return true;
			}
			sender.sendMessage(ChatColor.RED + "Item is not a captchacard!");
			return true;
		}
		return false;
	}

	@CommandDescription("Converts captchacards from paper to plastic.")
	@CommandUsage("Run /convert with a Captchacard in hand.")
	@SblockCommand
	public boolean convert(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		int conversions = Captcha.convert(player);
		if (conversions > 0) {
			player.sendMessage(ChatColor.GREEN.toString() + conversions + " captchacards converted!");
		} else {
			player.sendMessage(ChatColor.RED + "No old captchacards found!");
		}
		return true;
	}
}
