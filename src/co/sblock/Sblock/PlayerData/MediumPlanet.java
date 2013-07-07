/**
 * 
 */
package co.sblock.Sblock.PlayerData;

/**
 * Represents each planet in the Medium
 * @author FireNG
 *
 */
public enum MediumPlanet
{
	UNKNOWN("Unknown", "Land of Fail and Downvotes"),
	LOFAF("LOFAF", "Land of Frost and Frogs"),
	LOHAC("LOHAC", "Land of Heat and Clockwork"),
	LOLAR("LOLAR", "Land of Light and Rain"),
	LOWAS("LOWAS", "Land of Wind and Shade");
	
	private String shortName, longName;
	
	private MediumPlanet(String shortName, String longName)
	{
		this.shortName = shortName;
		this.longName = longName;
	}
	
	public String getShortName()
	{
		return shortName;
	}
	
	public String getLongName()
	{
		return longName;
	}
}
