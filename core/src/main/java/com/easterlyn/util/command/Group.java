package com.easterlyn.util.command;

import java.util.Collection;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface Group {

	@NotNull
	Collection<UUID> getMembers();

}
