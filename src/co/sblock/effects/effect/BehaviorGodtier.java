package co.sblock.effects.effect;

import java.util.Collection;

import co.sblock.users.UserAspect;

/**
 * Interface defining this Effect as belonging to a particular aspect.
 * 
 * @author Jikoo
 */
public interface BehaviorGodtier {

	public Collection<UserAspect> getAspects();

	public String getName(UserAspect aspect);

	public String getDescription(UserAspect aspect);

}
