package com.easterlyn.kitchensink.listener;

import java.util.regex.Pattern;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class NoIllegalName implements Listener {

  private final Pattern pattern = Pattern.compile("[^a-zA-Z_0-9]");

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerLogin(PlayerLoginEvent event) {
    if (this.pattern.matcher(event.getPlayer().getName()).find()) {
      /*
       * One day we had a guy log in who had a space after his name. Played hell with plugins
       * and couldn't be targeted by commands. Good times. Luckily, he wasn't malicious and
       * probably didn't even realize how badly he could have screwed us over.
       *
       * If Mojang screws up again, I am not dealing with it.
       */
      event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
      event.setKickMessage("Your name appears to be invalid. Please restart Minecraft.");
      return;
    }
    // Support old full bans TODO move or remove and fix old bans
    switch (event.getResult()) {
      case KICK_BANNED:
      case KICK_OTHER:
        String reason = event.getKickMessage();
        event.setKickMessage(reason.replaceAll("<(ip|uuid|name)=.*?>", ""));
        break;
      case ALLOWED:
      case KICK_FULL:
      case KICK_WHITELIST:
      default:
        break;
    }
  }
}
