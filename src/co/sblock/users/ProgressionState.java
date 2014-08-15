package co.sblock.users;

/**
 * Enum representing User game progression.
 * 
 * @author Jikoo
 */
public enum ProgressionState {
	NONE(0), ENTRY_UNDERWAY(1), ENTRY_COMPLETING(2), ENTRY(3), GODTIER(10);

	private final int value;
	private ProgressionState(int value) {
		this.value = value;
	}

	/**
	 * Gets the magic value representing this ProgressionState. Used to check if a User is beyond a
	 * certain point in the game.
	 * <p>
	 * For example, checking if a User has achieved Entry or later in the game:
	 * <pre>
	 * user.getProgression().value() >= ProgressionState.ENTRY.value()
	 * </pre>
	 * <p>
	 * This value may change but will remain constant relative to existing
	 * ProgressionStates. It should not be used for saving or loading states, nor should it be used to
	 * compare to fixed values.
	 * 
	 * @return
	 */
	public int value() {
		return value;
	}
}
