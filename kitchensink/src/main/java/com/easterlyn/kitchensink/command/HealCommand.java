package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.PermissionUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class HealCommand extends BaseCommand {

	public HealCommand() {
		PermissionUtil.addParent("easterlyn.command.heal.other", UserRank.MODERATOR.getPermission());
		PermissionUtil.addParent("easterlyn.command.feed.other", UserRank.MODERATOR.getPermission());
	}

	@CommandAlias("heal")
	@Description("Heal a player to full health.")
	@CommandPermission("easterlyn.command.heal")
	@CommandCompletion("@player")
	@Syntax("/heal [player]")
	public void heal(@Flags(CoreContexts.ONLINE_WITH_PERM) Player player) {
		AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		CommandIssuer issuer = getCurrentCommandIssuer();
		if (attribute == null) {
			issuer.sendMessage("Cannot obtain max health for " + player.getName());
			return;
		}
		player.setHealth(attribute.getValue());
		player.sendMessage("You have been healed!");
		if (!issuer.getUniqueId().equals(player.getUniqueId())) {
			issuer.sendMessage("Healed " + player.getName());
		}
	}

	@CommandAlias("feed")
	@Description("Fill a player's food.")
	@CommandPermission("easterlyn.command.feed")
	@CommandCompletion("@player")
	@Syntax("/feed [player]")
	public void feed(@Flags(CoreContexts.ONLINE_WITH_PERM) Player player) {
		CommandIssuer issuer = getCurrentCommandIssuer();
		player.setFoodLevel(20);
		player.setSaturation(20);
		player.sendMessage("You have been fed!");
		if (!issuer.getUniqueId().equals(player.getUniqueId())) {
			issuer.sendMessage("Fed " + player.getName());
		}
	}

}
