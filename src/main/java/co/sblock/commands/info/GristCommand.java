package co.sblock.commands.info;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.captcha.CruxiteDowel;
import co.sblock.chat.Language;
import co.sblock.commands.SblockCommand;
import co.sblock.effects.Effects;
import co.sblock.utilities.Experience;
import co.sblock.utilities.InventoryUtils;

/**
 * SblockCommand for getting information about grist costs and totals.
 * 
 * @author Jikoo
 */
public class GristCommand extends SblockCommand {

	private final Effects effects;

	public GristCommand(Sblock plugin) {
		super(plugin, "grist");
		this.effects = plugin.getModule(Effects.class);
		this.setDescription("Grist-related operations.");
		this.setUsage(Language.getColor("command") + "/grist cost"
				+ Language.getColor("neutral") + ": Grist cost of item in hand.\n"
				+ Language.getColor("command") + "/grist current"
				+ Language.getColor("neutral") + ": Current grist.\n"
				+ Language.getColor("command") + "/grist (level)L"
				+ Language.getColor("neutral") + ": Grist from level. Ex: /grist 40L\n"
				+ Language.getColor("command") + "/grist (exp points)"
				+ Language.getColor("neutral") + ": Level from grist. Ex: /grist 100");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}
		try {
			if (args[0].length() > 1) {
				char lastChar = args[0].charAt(args[0].length() - 1);
				if (lastChar == 'L' || lastChar == 'l') {
					int level = Integer.parseInt(args[0].substring(0, args[0].length() - 1));
					sender.sendMessage(String.format("%1$sLevel %3$s is %2$s%4$s %1$sgrist.",
							Language.getColor("good"), Language.getColor("emphasis.good"), level,
							Experience.getExpFromLevel(level)));
					return true;
				}
			}
			int grist = Integer.parseInt(args[0]);
			sender.sendMessage(String.format("%1$s%3$s grist is level %2$s%4$.3f.",
					Language.getColor("good"), Language.getColor("emphasis.good"), grist, Experience.getLevelFromExp(grist)));
			return true;
		} catch (NumberFormatException e) {
			if (!args[0].equalsIgnoreCase("cost") && !args[0].equalsIgnoreCase("current")) {
				return false;
			}
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage("/grist <(exp)|(level)L>");
			return true;
		}
		Player player = (Player) sender;
		if (args[0].equalsIgnoreCase("current")) {
			sender.sendMessage(String.format("%1$sYou have %2$s%3$s %1$sgrist.", Language.getColor("good"),
					Language.getColor("emphasis.good"), Experience.getExp(player)));
			return true;
		}
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand == null || hand.getType() == Material.AIR) {
			sender.sendMessage(Language.getColor("bad") + "Nothing in life is free.");
			return true;
		}
		sender.sendMessage(String.format("%1$sYour %2$s%3$s%1$s would cost %2$s%4$s grist %1$sto recreate.",
				Language.getColor("good"), Language.getColor("emphasis.good"),
				InventoryUtils.getItemName(hand),
				CruxiteDowel.expCost(effects, hand)));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (!sender.hasPermission(this.getPermission()) || args.length != 1) {
			return ImmutableList.of();
		}
		args[0] = args[0].toLowerCase();
		List<String> completions = new ArrayList<>();
		if ("cost".startsWith(args[0])) {
			completions.add("cost");
		}
		if ("current".startsWith(args[0])) {
			completions.add("current");
		}
		return completions;
	}

}
