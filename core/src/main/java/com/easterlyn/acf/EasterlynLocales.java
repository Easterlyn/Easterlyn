package com.easterlyn.acf;

import co.aikar.commands.BukkitLocales;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import com.easterlyn.EasterlynCore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EasterlynLocales extends BukkitLocales {

  private static final Pattern I18N_STRING =
      Pattern.compile("\\{@@(?<key>.+?)}", Pattern.CASE_INSENSITIVE);

  private final EasterlynCore plugin;

  public EasterlynLocales(EasterlynCore plugin, EasterlynCommandManager manager) {
    super(manager);
    this.plugin = plugin;
  }

  @Override
  public @Nullable String getMessage(
      @NotNull CommandIssuer issuer, @NotNull MessageKeyProvider key) {
    return plugin.getLocaleManager().getValue(key.getMessageKey().getKey(), getLocale(issuer));
  }

  @Override
  public @Nullable String replaceI18NStrings(@Nullable String message) {
    if (message == null) {
      return null;
    }
    Matcher matcher = I18N_STRING.matcher(message);
    if (!matcher.find()) {
      return message;
    } else {
      CommandIssuer issuer = CommandManager.getCurrentCommandIssuer();
      matcher.reset();
      StringBuffer sb = new StringBuffer(message.length());

      while (matcher.find()) {
        MessageKey key = MessageKey.of(matcher.group("key"));
        String value = this.getMessage(issuer, key);
        if (value == null) {
          value = "";
        }
        matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
      }

      matcher.appendTail(sb);
      return sb.toString();
    }
  }

  private String getLocale(CommandIssuer issuer) {
    if (issuer == null) {
      return plugin.getLocaleManager().getDefaultLocale();
    }
    if (!(issuer.getIssuer() instanceof CommandSender)) {
      return plugin.getLocaleManager().getDefaultLocale();
    }
    return plugin.getLocaleManager().getLocale(issuer.getIssuer());
  }
}
