package co.sblock.utilities.voting;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

public class VoteCount
{
    private final Set<Player> YES = new HashSet<>();
    private final Set<Player> NO = new HashSet<>();
    
    //TODO: populate this with all players
    private final Set<Player> ABSTAIN = new HashSet<>();
    
    /**
     * @param p the player voting
     * @return the same instance (for chaining purposes);
     */
    public VoteCount voteYes(Player p) {
        YES.add(p);
        ABSTAIN.remove(p);
        NO.remove(p);
        return this;
    }
    
    /**
     * @param p the player voting
     * @return the same instance (for chaining purposes);
     */
    public VoteCount voteNo(Player p) {
        NO.add(p);
        ABSTAIN.remove(p);
        YES.remove(p);
        return this;
    }
    
    /**
     * @param p the player voting
     * @return the same instance (for chaining purposes);
     */
    public VoteCount abstain(Player p) {
        ABSTAIN.add(p);
        YES.remove(p);
        NO.remove(p);
        return this;
    }
    
    /**
     * clear all votes
     * @return this same instance
     */
    public VoteCount reset() {
        YES.clear();
        NO.clear();
        
        // TODO: repopulate this with all players
        ABSTAIN.clear();
        return this;
    }
    
    /**
     * @return an object representing an overview (without exposing the inner workings)
     */
    public VoteCountOverview getOverview() {
        return new VoteCountOverview();
    }
    
    /**
     * 
     * @author ted
     *
     * abstract away the storage of the VoteCount object
     */
    class VoteCountOverview {
        public final int yes = YES.size();
        public final int no = NO.size();
        public final int abstain = ABSTAIN.size();
        public final int total = yes + no + abstain;
        
        private VoteCountOverview() { }
    }
    
}
