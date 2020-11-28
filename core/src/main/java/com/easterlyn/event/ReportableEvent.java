package com.easterlyn.event;

import com.easterlyn.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Event used for reporting internal issues.
 *
 * @author Jikoo
 */
public class ReportableEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  private final String message;
  private final String trace;

  private ReportableEvent(String message, @Nullable String trace) {
    this.message = message;
    this.trace = trace;
  }

  public @NotNull static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  public static void call(String message) {
    call(message, 0);
  }

  public static void call(String message, int traceDepth) {
    call(message, traceDepth > 0 ? new Throwable().fillInStackTrace() : null, traceDepth);
  }

  public static void call(String message, @Nullable Throwable throwable, int traceDepth) {
    Bukkit.getPluginManager()
        .callEvent(
            new ReportableEvent(
                message,
                traceDepth > 0 && throwable != null
                    ? StringUtil.getTrace(throwable, traceDepth)
                    : null));
  }

  public String getMessage() {
    return message;
  }

  public boolean hasTrace() {
    return trace != null;
  }

  public @Nullable String getTrace() {
    return trace;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLER_LIST;
  }
}
