package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import co.aikar.locales.MessageKey;
import com.sk89q.worldedit.internal.expression.Expression;
import java.util.HashMap;
import java.util.UUID;

public class CalculateCommand extends BaseCommand {

	// TODO reintroduce chat-based option and fun responses

	private final HashMap<UUID, Double> lastValue = new HashMap<>();

	@CommandAlias("calculate|calc|halc")
	@Description("{@@sink.module.calculate}")
	@CommandPermission("easterlyn.command.calculate")
	@Syntax("[equation]")
	@CommandCompletion("")
	public void calculate(BukkitCommandIssuer issuer, String input) {
		input = input.toLowerCase().replace('x', '*');
		if (this.lastValue.containsKey(issuer.getUniqueId())) {
			input = input.replace("ans", String.valueOf(this.lastValue.get(issuer.getUniqueId())));
		}
		try {
			double ans = Expression.compile(input).evaluate();
			lastValue.put(issuer.getUniqueId(), ans);
			String answer;
			if (ans == (int) ans) {
				answer = String.valueOf((int) ans);
			} else {
				answer = String.format("%s", ans);
			}
			issuer.sendMessage(input + " = " + answer);
		} catch (Exception e) {
			issuer.sendInfo(MessageKey.of("sink.module.calculate.error"));
		}
	}

}
