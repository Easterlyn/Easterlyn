package co.sblock.chat.ai;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import co.sblock.users.OfflineUser;
import co.sblock.users.ProgressionState;
import co.sblock.users.Region;
import co.sblock.users.UserAspect;
import co.sblock.users.UserClass;

/**
 * Extension of OfflineUser to allow fake player objects to be created.
 * 
 * @author Jikoo
 */
public class AIUser extends OfflineUser {

	/**
	 * @param displayName
	 * @param userClass
	 * @param userAspect
	 * @param medium
	 * @param dream
	 */
	protected AIUser(String displayName, UserClass userClass, UserAspect userAspect, Region medium, Region dream) {
		super(UUID.randomUUID(), "localhost", displayName, null, null, "N/A", null, userClass,
				userAspect, medium, dream, ProgressionState.NONE, null, null,
				new HashSet<Integer>(), false, false, "#", new HashSet<String>(),
				new AtomicBoolean(false), new AtomicBoolean(false), new AtomicBoolean(false));
	}
}
