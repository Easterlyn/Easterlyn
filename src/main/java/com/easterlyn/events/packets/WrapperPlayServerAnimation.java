/*
 *  PacketWrapper - Contains wrappers for each packet in Minecraft.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.easterlyn.events.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayServerAnimation extends AbstractPacket {

	public static final PacketType TYPE = PacketType.Play.Server.ANIMATION;

	public WrapperPlayServerAnimation() {
		super(new PacketContainer(TYPE), TYPE);
		handle.getModifier().writeDefaults();
	}

	public WrapperPlayServerAnimation(PacketContainer packet) {
		super(packet, TYPE);
	}

	/**
	 * Retrieve Entity ID.
	 * <p>
	 * Notes: player ID
	 * 
	 * @return The current Entity ID
	 */
	public int getEntityId() {
		return handle.getIntegers().read(0);
	}

	/**
	 * Set Entity ID.
	 * 
	 * @param value - new value.
	 */
	public void setEntityId(int value) {
		handle.getIntegers().write(0, value);
	}

	/**
	 * Retrieve Animation.
	 * <p>
	 * Notes: animation ID
	 * 
	 * @return The current Animation
	 */
	public int getAnimation() {
		return handle.getIntegers().read(1);
	}

	/**
	 * Set Animation.
	 * 
	 * @param value - new value.
	 */
	public void setAnimation(int value) {
		handle.getIntegers().write(1, value);
	}

}
