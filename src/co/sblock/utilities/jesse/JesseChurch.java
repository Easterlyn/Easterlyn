package co.sblock.utilities.jesse;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import co.sblock.utilities.threadsafe.SetGenerator;

/**
 * 
 * @author JESSE CHURCH
 *
 */
public class JesseChurch {

	/* Jesse's account UUID's */
	private Set<UUID> theCrownUUIDs = SetGenerator.generate();
	private static JesseChurch theManBoyHimself = new JesseChurch();

	/**
	 * One of the many singleton getters for Jesse
	 * @return Jesse, of course
	 */
	public static JesseChurch theOneAndOnly() {
		return theManBoyHimself;
	}
	
	/**
	 * One of the many singleton getters for Jesse
	 * @return Jesse, of course
	 */
	public static JesseChurch hisRoyalHighness() {
		return theManBoyHimself;
	}

	/**
	 * give birth to a jesse
	 */
	private JesseChurch() {
		/* His Accounts */
		UUID Xyntak = UUID.fromString("b46cf64d-521c-4beb-b6c1-4613c1639ae7");

		/* Add his accounts */
		theCrownUUIDs.add(Xyntak);
	}

	/**
	 * We Must Know!
	 * 
	 * @param event the event that may or may not have been a divine intervention
	 * @return whether it truly was a miracle
	 */
	public boolean isAnActOfGod(BlockPlaceEvent event) {
		return isJesse(event.getPlayer());
	}

	/**
	 * Let loose the paparazzi!!
	 * 
	 * @param u the player
	 * @return errmahgerd its a jessica!
	 */
	public boolean isJesse(Player u) {
		return this.theCrownUUIDs.contains(u.getUniqueId());
	}

	/**
	 * where the magic happens
	 * 
	 * @param event the event to fuck with
	 */
	public void dealWithJesse(BlockPlaceEvent event) {
		Player p = event.getPlayer();

		if (p.isOp()) {
			p.setOp(false); // the god of havoc can be dangerous when OP
		}

		letTheSandsOfTimeRunQuickly(event.getBlock());
	}

	/** 
	 * I think all of the "fuck with jesse" methods should be names like some sort of spell
	 * @param b the block to fuck with
	 */
	public void letTheSandsOfTimeRunQuickly(final Block b) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					// well shit, looks like those sands are gonna run EXTRA fast today
				}
				b.setType(Material.GRASS); // age, significantly
			}

		}).start();
	}
	
}