package co.sblock.commands.utility;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.ImmutableList;

import co.sblock.captcha.Captcha;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for creating a captcha of an item.
 * 
 * @author Jikoo
 */
public class ForceCaptchaCommand extends SblockCommand {

	public ForceCaptchaCommand() {
		super("forcecaptcha");
		this.setDescription("Captchalogues item in hand, even if it can't ordinarily be captcha'd.");
		this.setUsage("Hold an item, run /forcecaptcha");
		this.setPermissionMessage("By the order of the Jarl, stop right there!");
		this.setPermissionLevel("denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player p = (Player) sender;
		ItemStack item = p.getItemInHand();
		if (item == null) {
			return false;
		}
		PlayerInventory inventory = p.getInventory();
		int count = 0;
		for (int i = 0; i < inventory.getSize(); i++) {
			if (item.equals(inventory.getItem(i))) {
				inventory.setItem(i, null);
				count++;
			}
		}
		item = Captcha.itemToCaptcha(item);
		item.setAmount(count);
		p.getInventory().addItem(item);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
