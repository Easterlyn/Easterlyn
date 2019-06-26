package com.easterlyn.event;

import com.easterlyn.util.StringUtil;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event used for reporting internal issues.
 *
 * @author Jikoo
 */
public class ReportableEvent extends Event {

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final String message;
	private final String trace;

	public ReportableEvent(String message) {
		this(message, 0);
	}

	public ReportableEvent(String message, int traceDepth) {
		this(message, traceDepth > 0 ? new Throwable().fillInStackTrace() : null, traceDepth);
	}

	public ReportableEvent(String message, Throwable throwable, int traceDepth) {
		this.message = message;
		this.trace = traceDepth > 0 && throwable != null ? StringUtil.getTrace(throwable, traceDepth) : null;
	}

	public String getMessage() {
		return message;
	}

	public boolean hasTrace() {
		return trace != null;
	}

	public String getTrace() {
		return trace;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	@NotNull
	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}

}
