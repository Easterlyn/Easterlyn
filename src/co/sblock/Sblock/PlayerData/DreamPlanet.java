/**
 * 
 */
package co.sblock.Sblock.PlayerData;

/**
 * Represents a dream planet
 * 
 * @author FireNG
 * 
 */
public enum DreamPlanet {
	UNKNOWN("Unknown"), PROSPIT("Prospit"), DERSE("Derse");

	private String displayName;

	private DreamPlanet(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return The display name of this planet.
	 */
	public String getDisplayName() {
		return displayName;
	}
}
