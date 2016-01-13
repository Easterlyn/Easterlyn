package co.sblock.effects.effect;

import java.util.Collection;
import java.util.List;

import co.sblock.users.UserAspect;

/**
 * Interface defining this Effect as belonging to a particular aspect.
 * 
 * @author Jikoo
 */
public interface BehaviorGodtier {

	public Collection<UserAspect> getAspects();

	public List<String> getDescription(UserAspect aspect);

}
