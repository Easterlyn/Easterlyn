package com.easterlyn.chat.channel;

import com.easterlyn.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainChannel extends ServerChannel {

  public MainChannel() {
    super("main");
  }

  @Override
  public boolean isFocusedChannel(@Nullable String channelName) {
    return channelName == null || super.isFocusedChannel(channelName);
  }

  @Override
  public boolean isWhitelisted(@NotNull User user) {
    return true;
  }

}
