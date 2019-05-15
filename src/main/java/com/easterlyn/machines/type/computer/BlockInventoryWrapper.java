package com.easterlyn.machines.type.computer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * Wrapper for overriding the new getLocation method for an Inventory.
 *
 * @author Jikoo
 */
public class BlockInventoryWrapper implements Inventory {

	private final Inventory inventory;
	private final Location location;

	public BlockInventoryWrapper(Inventory inventory, Location location) {
		this.inventory = inventory;
		this.location = location;
	}

	@NotNull
	@Override
	public HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... items) throws IllegalArgumentException {
		return inventory.addItem(items);
	}

	@NotNull
	@Override
	public HashMap<Integer, ? extends ItemStack> all(@NotNull Material material)
			throws IllegalArgumentException {
		return inventory.all(material);
	}

	@NotNull
	@Override
	public HashMap<Integer, ? extends ItemStack> all(ItemStack item) {
		return inventory.all(item);
	}

	@Override
	public void clear() {
		inventory.clear();
	}

	@Override
	public void clear(int index) {
		inventory.clear(index);
	}

	@Override
	public boolean contains(@NotNull Material material) throws IllegalArgumentException {
		return inventory.contains(material);
	}

	@Override
	public boolean contains(ItemStack item) {
		return inventory.contains(item);
	}

	@Override
	public boolean contains(@NotNull Material material, int amount) throws IllegalArgumentException {
		return inventory.contains(material, amount);
	}

	@Override
	public boolean contains(ItemStack item, int amount) {
		return inventory.contains(item, amount);
	}

	@Override
	public boolean containsAtLeast(ItemStack item, int amount) {
		return inventory.containsAtLeast(item, amount);
	}

	@Override
	public int first(@NotNull Material material) throws IllegalArgumentException {
		return inventory.first(material);
	}

	@Override
	public int first(@NotNull ItemStack item) {
		return inventory.first(item);
	}

	@Override
	public int firstEmpty() {
		return inventory.firstEmpty();
	}

	@NotNull
	@Override
	public ItemStack[] getContents() {
		return inventory.getContents();
	}

	@Override
	public InventoryHolder getHolder() {
		return inventory.getHolder();
	}

	@Override
	public ItemStack getItem(int index) {
		return inventory.getItem(index);
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public int getMaxStackSize() {
		return inventory.getMaxStackSize();
	}

	@Override
	public int getSize() {
		return inventory.getSize();
	}

	@NotNull
	@Override
	public InventoryType getType() {
		return inventory.getType();
	}

	@NotNull
	@Override
	public List<HumanEntity> getViewers() {
		return inventory.getViewers();
	}

	@NotNull
	@Override
	public ListIterator<ItemStack> iterator() {
		return inventory.iterator();
	}

	@NotNull
	@Override
	public ListIterator<ItemStack> iterator(int index) {
		return inventory.iterator(index);
	}

	@Override
	public void remove(@NotNull Material material) throws IllegalArgumentException {
		inventory.remove(material);
	}

	@Override
	public void remove(@NotNull ItemStack item) {
		inventory.remove(item);
	}

	@NotNull
	@Override
	public HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... items)
			throws IllegalArgumentException {
		return inventory.removeItem(items);
	}

	@Override
	public void setContents(@NotNull ItemStack[] items) throws IllegalArgumentException {
		inventory.setContents(items);
	}

	@Override
	public void setItem(int index, ItemStack item) {
		inventory.setItem(index, item);
	}

	@Override
	public void setMaxStackSize(int size) {
		inventory.setMaxStackSize(size);
	}

	@NotNull
	@Override
	public ItemStack[] getStorageContents() {
		return inventory.getStorageContents();
	}

	@Override
	public void setStorageContents(@NotNull ItemStack[] arg0) throws IllegalArgumentException {
		inventory.setStorageContents(arg0);
	}

}
