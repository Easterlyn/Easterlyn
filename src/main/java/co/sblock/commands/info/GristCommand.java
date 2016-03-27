package co.sblock.commands.info;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.captcha.CruxiteDowel;
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
	private final DecimalFormat format;

	public GristCommand(Sblock plugin) {
		super(plugin, "grist");
		this.effects = plugin.getModule(Effects.class);
		this.format = new DecimalFormat("#,###,###,###.###");
		this.format.setRoundingMode(RoundingMode.CEILING);
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
					sender.sendMessage(this.getLang().getValue("command.grist.level")
							.replace("{LEVEL}", this.format.format(level))
							.replace("{EXP}", this.format.format(Experience.getExpFromLevel(level))));
					return true;
				}
			}
			int grist = Integer.parseInt(args[0]);
			sender.sendMessage(this.getLang().getValue("command.grist.exp")
					.replace("{LEVEL}", this.format.format(Experience.getLevelFromExp(grist)))
					.replace("{EXP}", this.format.format(grist)));
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
			sender.sendMessage(this.getLang().getValue("command.grist.current")
					.replace("{EXP}", this.format.format(Experience.getExp(player))));
			return true;
		}
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand == null || hand.getType() == Material.AIR) {
			sender.sendMessage(this.getLang().getValue("command.grist.nothing"));
			return true;
		}
		sender.sendMessage(this.getLang().getValue("command.grist.cost")
				.replace("{ITEM}", InventoryUtils.getItemName(hand))
				.replace("{EXP}", this.format.format(CruxiteDowel.expCost(effects, hand))));
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
