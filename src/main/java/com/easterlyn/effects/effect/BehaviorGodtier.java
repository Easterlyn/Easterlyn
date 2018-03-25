package com.easterlyn.effects.effect;

import com.easterlyn.users.UserAffinity;

import java.util.Collection;
import java.util.List;

/**
 * Interface defining this Effect as belonging to a particular affinity.
 *
 * @author Jikoo
 */
public interface BehaviorGodtier {

	Collection<UserAffinity> getAffinity();

	List<String> getDescription(UserAffinity aspect);

}
