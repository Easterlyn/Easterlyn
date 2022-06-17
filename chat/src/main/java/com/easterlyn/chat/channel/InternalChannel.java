package com.easterlyn.chat.channel;

import com.easterlyn.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * A class for server-owned special channels.
 *
 * @author Jikoo
 */
public class InternalChannel extends ServerChannel {

  public InternalChannel(@NotNull String name) {
    super(name);
  }

  @Override
  public boolean isWhitelisted(@NotNull User user) {
    return isModerator(user);
  }

  @Override
  public boolean isPrivate() {
    return true;
  }

}
