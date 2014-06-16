package co.sblock.users;

/**
 * Represents a dream planet.
 * 
 * @author FireNG, Jikoo
 */
public enum DreamPlanet {

    NONE("Earth"), PROSPIT("InnerCircle"), DERSE("OuterCircle");

    /** The name of the World. */
    private String worldName;

    /**
     * Constructor for DreamPlanet.
     * 
     * @param worldName the name of the World used for this DreamPlanet.
     */
    DreamPlanet(String worldName) {
        this.worldName = worldName;
    }

    /**
     * Gets the DreamPlanet's display name.
     * 
     * @return The display name of this DreamPlanet.
     */
    public String getDisplayName() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase();
    }

    /**
     * Gets the name of the World.
     * 
     * @return the World name
     */
    public String getWorldName() {
        return this.worldName;
    }

    /**
     * Gets a DreamPlanet by name.
     * 
     * @param name the name of a DreamPlanet
     * 
     * @return the DreamPlanet specified, DreamPlanet.NONE if invalid.
     */
    public static DreamPlanet getPlanet(String name) {
        try {
            return DreamPlanet.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DreamPlanet.NONE;
        }
    }
}
