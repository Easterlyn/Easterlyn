package co.sblock.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.sblock.utilities.captcha.Captcha;

/**
 * SblockCommand for creating a captcha of an item.
 * 
 * @author Jikoo
 */
public class ForceCaptchaCommand extends SblockCommand {

	public ForceCaptchaCommand() {
		super("forcecaptcha");
		this.setDescription("Captchalogues item in hand, even if it can't ordinarily be captcha'd.");
		this.setUsage("/forcecaptcha");
		this.setPermission("group.horrorterror");
		this.setPermissionMessage("By the order of the Jarl, stop right there!");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player p = (Player) sender;
		ItemStack item = p.getItemInHand();
		p.getInventory().remove(item);
		p.getInventory().addItem(Captcha.itemToCaptcha(item));
		return true;
	}
}
