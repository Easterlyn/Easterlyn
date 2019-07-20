package com.easterlyn.command;

import com.easterlyn.EasterlynCore;
import com.easterlyn.util.StringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class CoreCompletions {

	public static void register(EasterlynCore plugin) {
		plugin.getCommandManager().getCommandCompletions().registerAsyncCompletion("integer", context -> {
			String input = context.getInput();
			if (!input.matches("-?\\d+?")) {
				return Collections.emptyList();
			}
			ArrayList<String> completions = new ArrayList<>();
			if (!input.isEmpty()) {
				completions.add(input);
			}
			for (int i = input.isEmpty() ? 1 : 0; i < 10; ++i) {
				completions.add(input + i);
			}
			return completions;
		});

		plugin.getCommandManager().getCommandCompletions().registerAsyncCompletion("decimal", context -> {
			String input = context.getInput();
			if (!input.matches("-?\\d+?\\.?\\d+?")) {
				return Collections.emptyList();
			}
			ArrayList<String> completions = new ArrayList<>();
			if (!input.isEmpty()) {
				completions.add(input);
			}
			for (int i = input.isEmpty() ? 1 : 0; i < 10; ++i) {
				completions.add(input + i);
			}
			if (input.indexOf('.') == -1) {
				completions.add(input + '.');
			}
			return completions;
		});

		plugin.getCommandManager().getCommandCompletions().registerAsyncCompletion("permission", context -> {
			if (context.hasConfig("value") && !context.getIssuer().hasPermission(context.getConfig("value"))) {
				return Collections.emptyList();
			}
			if (!context.hasConfig("complete")) {
				return Collections.emptyList();
			}
			return Arrays.stream(context.getConfig("complete").split("/")).distinct()
					.filter(completion -> StringUtil.startsWithIgnoreCase(completion, context.getInput()))
					.collect(Collectors.toList());
		});
		// TODO player, playerOnline, playerOffline, playerOnlineIfPerm

		plugin.getCommandManager().getCommandCompletions().registerStaticCompletion("password", Collections.singletonList("Hunter2"));
	}

	private CoreCompletions() {}

}
