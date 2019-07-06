package com.easterlyn.effect;

import java.util.function.Function;
import org.bukkit.inventory.EquipmentSlot;

public class EquipmentSlots {

	public static final Function<EquipmentSlot, Boolean> ALL = equipmentSlot -> true;
	public static final Function<EquipmentSlot, Boolean> TOOL = equipmentSlot -> equipmentSlot == EquipmentSlot.HAND;
	public static final Function<EquipmentSlot, Boolean> HELD = equipmentSlot ->
			equipmentSlot == EquipmentSlot.HAND || equipmentSlot == EquipmentSlot.OFF_HAND;
	public static final Function<EquipmentSlot, Boolean> ARMOR = equipmentSlot ->
			equipmentSlot == EquipmentSlot.HEAD || equipmentSlot == EquipmentSlot.CHEST
					|| equipmentSlot == EquipmentSlot.LEGS || equipmentSlot == EquipmentSlot.FEET;
	public static final Function<EquipmentSlot, Boolean> HEAD = equipmentSlot -> equipmentSlot == EquipmentSlot.HEAD;
	public static final Function<EquipmentSlot, Boolean> FEET = equipmentSlot -> equipmentSlot == EquipmentSlot.FEET;

	private EquipmentSlots() {}

}
