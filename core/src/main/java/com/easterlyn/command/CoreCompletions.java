package com.easterlyn.command;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import com.easterlyn.EasterlynCore;
import com.easterlyn.util.StringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CoreCompletions {

  private CoreCompletions() {}

  public static void register(EasterlynCore plugin) {
    CommandCompletions<BukkitCommandCompletionContext> completions =
        plugin.getCommandManager().getCommandCompletions();
    completions.registerAsyncCompletion(
        "integer",
        context -> {
          String input = context.getInput();
          if (!input.matches("-?\\d+?")) {
            return Collections.emptyList();
          }
          ArrayList<String> values = new ArrayList<>();
          if (!input.isEmpty()) {
            values.add(input);
          }
          for (int i = input.isEmpty() ? 1 : 0; i < 10; ++i) {
            values.add(input + i);
          }
          return values;
        });
    completions.setDefaultCompletion("integer", int.class, Integer.class, long.class, Long.class);

    completions.registerAsyncCompletion(
        "decimal",
        context -> {
          String input = context.getInput();
          if (!input.matches("-?\\d+?\\.?\\d+?")) {
            return Collections.emptyList();
          }
          ArrayList<String> values = new ArrayList<>();
          if (!input.isEmpty()) {
            values.add(input);
          }
          for (int i = input.isEmpty() ? 1 : 0; i < 10; ++i) {
            values.add(input + i);
          }
          if (input.indexOf('.') == -1) {
            values.add(input + '.');
          }
          return values;
        });
    completions.setDefaultCompletion(
        "decimal", double.class, Double.class, float.class, Float.class);

    completions.registerAsyncCompletion(
        "permission",
        context -> {
          if (context.hasConfig("value")
              && !context.getIssuer().hasPermission(context.getConfig("value"))) {
            return Collections.emptyList();
          }
          if (!context.hasConfig("complete")) {
            return Collections.emptyList();
          }
          return Arrays.stream(context.getConfig("complete").split("/"))
              .distinct()
              .filter(completion -> StringUtil.startsWithIgnoreCase(completion, context.getInput()))
              .collect(Collectors.toList());
        });

    completions.registerCompletion(
        "player",
        context -> {
          Player issuer = context.getPlayer();
          ArrayList<String> values = new ArrayList<>();
          for (Player player : Bukkit.getOnlinePlayers()) {
            if (issuer == null || issuer.canSee(player)) {
              values.add(player.getName());
            }
          }
          return values;
        });
    completions.setDefaultCompletion("player", Player.class);
    // TODO
    //  commands, date, boolean
    //  location, worldLocation, material, world, chatcolor, chatformat

    completions.registerStaticCompletion(
        "password", Arrays.asList("Hunter2", "animebuttsdrivemenuts1"));
  }

}
