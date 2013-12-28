/*
 * PacketWrapper - Contains wrappers for each packet in Minecraft.
 * Copyright (C) 2012 Kristian S. Stangeland
 * Modified 12/24/13 by A. Gunn aka Jikoo - updated to remove deprecation and correct for 1.7 changes.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02package co.sblock.Sblock.Events;111-1307 USA
 */

package co.sblock.Sblock.Events.Packets;

import org.bukkit.World;
import org.bukkit.entity.Entity;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.IntEnum;

public class Packet12Animation extends AbstractPacket {
	public static final int ID = 18;

	/**
	 * Represents the different animations sent by notchian clients.
	 * 
	 * @author Kristian
	 */
	public static class Animations extends IntEnum {
		// Jikoo start
		public static final int SWING_ARM = 0;
		public static final int DAMAGE_ANIMATION = 1;
		public static final int LEAVE_BED = 2;
		public static final int EAT_FOOD = 3;
		public static final int CRITICAL_EFFECT = 4;
		public static final int MAGIC_CRITICAL_EFFECT = 5;
		// Jikoo end
		public static final int UNKNOWN = 102;
		public static final int CROUCH = 104;
		public static final int UNCROUCH = 105;

		/**
		 * The singleton instance. Can also be retrieved from the parent class.
		 */
		private static Animations INSTANCE = new Animations();

		/**
		 * Retrieve an instance of the Animation enum.
		 * 
		
		 * @return Animation enum. */
		public static Animations getInstance() {
			return INSTANCE;
		}
	}

	public Packet12Animation() {
		super(new PacketContainer(PacketType.Play.Server.ANIMATION), PacketType.Play.Server.ANIMATION); // Jikoo
		handle.getModifier().writeDefaults();
	}

	public Packet12Animation(PacketContainer packet) {
		super(packet, PacketType.Play.Server.ANIMATION); // Jikoo
	}

	/**
	 * Retrieve the player ID.
	 * 
	 * @return The current EID
	 */
	public int getEntityID() {
		return handle.getIntegers().read(0);
	}

	/**
	 * Set the player ID.
	 * 
	 * @param value
	 *            new value.
	 */
	public void setEntityID(int value) {
		handle.getIntegers().write(0, value);
	}

	/**
	 * Retrieve the player's entity object.
	 * 
	 * @param world
	 *            the world the player has joined.
	 * @return The player's entity.
	 */
	public Entity getEntity(World world) {
		return handle.getEntityModifier(world).read(0);
	}

	/**
	 * Retrieve the player's entity object.
	 * 
	 * @param event
	 *            the packet event.
	 * @return The player's entity.
	 */
	public Entity getEntity(PacketEvent event) {
		return getEntity(event.getPlayer().getWorld());
	}

	/**
	 * Retrieve animation ID.
	 * 
	 * @return The current Animation
	 */
	public byte getAnimation() {
		return handle.getIntegers().read(1).byteValue();
	}

	/**
	 * Set animation ID.
	 * 
	 * @param value
	 *            new value.
	 */
	public void setAnimation(byte value) {
		handle.getIntegers().write(1, (int) value);
	}
}
