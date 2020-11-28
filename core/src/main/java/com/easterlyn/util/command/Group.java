package com.easterlyn.util.command;

import java.util.Collection;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

public interface Group {

  @NotNull
  Collection<UUID> getMembers();

  TextComponent getMention();
}
