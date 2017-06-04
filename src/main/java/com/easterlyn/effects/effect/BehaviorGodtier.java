package com.easterlyn.effects.effect;

import com.easterlyn.users.UserAspect;

import java.util.Collection;
import java.util.List;

/**
 * Interface defining this Effect as belonging to a particular aspect.
 *
 * @author Jikoo
 */
public interface BehaviorGodtier {

	Collection<UserAspect> getAspects();

	List<String> getDescription(UserAspect aspect);

}
