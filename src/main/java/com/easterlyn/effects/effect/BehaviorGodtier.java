package com.easterlyn.effects.effect;

import java.util.Collection;
import java.util.List;

import com.easterlyn.users.UserAspect;

/**
 * Interface defining this Effect as belonging to a particular aspect.
 * 
 * @author Jikoo
 */
public interface BehaviorGodtier {

	public Collection<UserAspect> getAspects();

	public List<String> getDescription(UserAspect aspect);

}
