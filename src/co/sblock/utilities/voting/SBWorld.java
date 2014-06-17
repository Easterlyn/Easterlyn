package co.sblock.utilities.voting;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;

/**
 * 
 * @author ted
 * 
 * this should be moved into a more general package
 *
 */
public class SBWorld {
    
    /**
     * private constructor; disallow inhertiance
     */
    private SBWorld(String name) {
        this.name = name;
    }
    
    private final String name;
    private final static Map<String, SBWorld> worlds = new HashMap<>();
    
    /**
     * 
     * @param name the name of the world
     * @return the world represented by that name. allows for multiple-static instances
     */
    public static SBWorld getWorldByName(String name) {
        if (name == null) {
            throw new NullPointerException("attempted to get world with null name");
        }
        SBWorld instance = worlds.get(name);
        if ( instance == null ) 
        {
            instance = new SBWorld(name);
            worlds.put(name, instance);
        }
        
        return instance;
    }
    
    /**
     * 
     * @param world the world that maps to this class
     * @return the SBWorld that represents the World
     */
    public static SBWorld getWorldByReference(World world) {
        return getWorldByName(world.getName());
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof SBWorld) {
            return ((SBWorld) other).name.equals(this.name);
        }
        return false;
    }
}
