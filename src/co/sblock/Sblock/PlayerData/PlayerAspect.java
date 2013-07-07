/**
 * 
 */
package co.sblock.Sblock.PlayerData;

/**
 * Represents each character aspect
 * @author FireNG
 *
 */
public enum PlayerAspect
{
	UNKNOWN("Bluh"),
	BLOOD("Blood"),
	BREATH("Breath"),
	DOOM("Doom"),
	HEART("Heart"),
	HOPE("Hope"),
	LIFE("Life"),
	LIGHT("Light"),
	MIND("Mind"),
	RAGE("Rage"),
	SPACE("Space"),
	TIME("Time"),
	VOID("Void");
	
	private String displayName;
	
	private PlayerAspect(String displayName)
	{
		this.displayName = displayName;
	}
	
	/**
	 * @return The display name of this aspect.
	 */
	public String getDisplayName()
	{
		return displayName;
	}
}
