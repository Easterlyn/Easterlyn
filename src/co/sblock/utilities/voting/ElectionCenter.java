package co.sblock.utilities.voting;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.Player;

import co.sblock.utilities.voting.VoteCount.VoteCountOverview;

/**
 * 
 * @author ted
 *
 * where the votes are cast... hehehe
 *
 */
public class ElectionCenter {
    
    private final Map<SBWorld, VoteCount> WORLD_VOTE_MAP = new HashMap<>();
    private final static ElectionCenter INSTANCE = new ElectionCenter();
    
    private ElectionCenter() { }
    
    /**
     * @return the global instance of the election center
     */
    public static ElectionCenter getInstance() {
        return INSTANCE;
    }
    
    /**
     * @param p the player voting
     * @param world the world in which the player is voting
     * @return an overview of the votes
     */
    public VoteCountOverview voteYes(Player p, String world) {
        return voteYes(p, SBWorld.getWorldByName(world));
    }
    
    /**
     * @param p the player voting
     * @param world the world in which the player is voting
     * @return an overview of the votes
     */
    public VoteCountOverview voteYes(Player p, World world) {
        return voteYes(p, SBWorld.getWorldByReference(world));
    }

    /**
     * @param p the player voting
     * @param world the world in which the player is voting
     * @return an overview of the votes
     */
    public VoteCountOverview voteYes(Player p, SBWorld world) {
        VoteCount currentCount = WORLD_VOTE_MAP.get(world);
        if (currentCount == null) {
            currentCount = new VoteCount().voteYes(p);
        }
        WORLD_VOTE_MAP.put(world, currentCount);
        return currentCount.getOverview();
    }
    
    /**
     * @param p the player voting
     * @param world the world in which the player is voting
     * @return an overview of the votes
     */
    public VoteCountOverview voteNo(Player p, String world) {
        return voteNo(p, SBWorld.getWorldByName(world));
    }
    
    /**
     * @param p the player voting
     * @param world the world in which the player is voting
     * @return an overview of the votes
     */
    public VoteCountOverview voteNo(Player p, World world) {
        return voteNo(p, SBWorld.getWorldByReference(world));
    }

    /**
     * @param p the player voting
     * @param world the world in which the player is voting
     * @return an overview of the votes
     */
    public VoteCountOverview voteNo(Player p, SBWorld world) {
        VoteCount currentCount = WORLD_VOTE_MAP.get(world);
        if (currentCount == null) {
            currentCount = new VoteCount().voteNo(p);
        }
        WORLD_VOTE_MAP.put(world, currentCount);
        return currentCount.getOverview();
    }
    
}