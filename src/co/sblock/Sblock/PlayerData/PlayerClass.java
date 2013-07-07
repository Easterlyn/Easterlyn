/**
 * 
 */
package co.sblock.Sblock.PlayerData;

/**
 * Represents each character class.
 * @author FireNG
 *
 */
public enum PlayerClass
{
	UNKNOWN("Noob"),
	BARD("Bard"),
	HEIR("Heir"),
	KNIGHT("Knight"),
	MAGE("Mage"),
	MAID("Maid"),
	PAGE("Page"),
	PRINCE("Prince"),
	ROGUE("Rogue"),
	SEER("Seer"),
	SYLPH("Sylph"),
	THEIF("Theif"),
	WITCH("Witch");
	
	private String displayName;
	
	private PlayerClass(String displayName)
	{
		this.displayName = displayName;
	}
	
	/**
	 * @return the displayed name of this player class.
	 */
	public String getDisplayName()
	{
		return displayName;
	}
	
	
}
