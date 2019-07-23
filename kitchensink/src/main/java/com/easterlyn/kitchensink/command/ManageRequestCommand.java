package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.User;
import com.easterlyn.util.Request;

public class ManageRequestCommand extends BaseCommand {

	@CommandAlias("accept|yes|y|tpyes|tpaccept")
	@Description("Accept a pending request.")
	@CommandCompletion("@none")
	@Syntax("/accept")
	@CommandPermission("easterlyn.command.request")
	public void accept(@Flags(CoreContexts.SELF) User user) {
		Request request = user.pollPendingRequest();
		if (request == null) {
			user.sendMessage("No pending requests!");
			return;
		}
		request.accept();
	}

	@CommandAlias("decline|no|n|tpno|tpdeny")
	@Description("Decline a pending request.")
	@CommandCompletion("@none")
	@Syntax("/decline")
	@CommandPermission("easterlyn.command.request")
	public void decline(@Flags(CoreContexts.SELF) User user) {
		Request request = user.pollPendingRequest();
		if (request == null) {
			user.sendMessage("No pending requests!");
			return;
		}
		request.decline();
	}

}
