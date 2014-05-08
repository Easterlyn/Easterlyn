package co.sblock.Sblock.Utilities.Economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import co.sblock.Sblock.SblockCommand;

/**
 * Utility for ATMs and computer-based deposit boxes.
 * 
 * @author Jikoo
 */
public class EconomyBank {

	private static EconomyBank instance;

	private Economy e;

	public static EconomyBank getBank() {
		if (instance == null) {
			instance = new EconomyBank();
		}
		return instance;
	}

	public EconomyBank() {
		e = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
	}

	public boolean withdraw(Player p, double amount) {
		EconomyResponse er = e.bankWithdraw(p.getUniqueId().toString(), amount);
		if (er.type == ResponseType.FAILURE) {
			if (er.errorMessage.equals("That bank does not exist!")) {
				e.createBank(p.getUniqueId().toString(), p.getUniqueId().toString());
			}
			p.sendMessage(ChatColor.RED + "Balance too low! Current balance: B" + er.balance);
			return false;
		}
		p.sendMessage(ChatColor.GREEN + "Withdrew B" + er.amount + ". Remaining balance: B" + er.balance);
		return true;
	}

	public boolean deposit(Player p, double amount) {
		EconomyResponse er = e.bankWithdraw(p.getUniqueId().toString(), amount);
		if (er.type == ResponseType.FAILURE) {
			if (er.errorMessage.equals("That bank does not exist!")) {
				er = e.createBank(p.getUniqueId().toString(), p.getUniqueId().toString());
				if (er.type == ResponseType.SUCCESS) {
					return deposit(p, amount);
				}
			}
			p.sendMessage(ChatColor.RED + "Could not deposit funds! Please seek help!");
			return false;
		}
		p.sendMessage(ChatColor.GREEN + "Deposited B" + er.amount + ". New balance: B" + er.balance);
		return true;
	}

	/**
	 * Extremely basic command for depositing to and withdrawing from banks.
	 */
	@SblockCommand(description = "The Boonconomy's one and only command.",
			usage = "boonconomy w|d <player> <double>", consoleFriendly = true, permission = "group.horrorterror")
	public boolean boonconomy(CommandSender s, String[] args) {
		if (args.length < 3) {
			return false;
		}
		Player p = Bukkit.getPlayer(args[1]);
		if (p == null) {
			s.sendMessage("Invalid player: " + args[1]);
			return true;
		}
		double amount;
		try {
			amount = Double.valueOf(args[2]);
		} catch (NumberFormatException e) {
			s.sendMessage("Invalid number: " + args[2]);
			return true;
		}
		if (args[0].equalsIgnoreCase("w")) {
			withdraw(p, amount);
		} else if (args[0].equalsIgnoreCase("d")) {
			deposit(p, amount);
		} else {
			return false;
		}
		return true;
	}
}
