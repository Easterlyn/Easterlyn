package co.sblock.machines.type.computer;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

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

	@Override
	public HashMap<Integer, ItemStack> addItem(ItemStack... items) throws IllegalArgumentException {
		return inventory.addItem(items);
	}

	@Override
	@Deprecated
	public HashMap<Integer, ? extends ItemStack> all(int materialId) {
		return inventory.all(materialId);
	}

	@Override
	public HashMap<Integer, ? extends ItemStack> all(Material material)
			throws IllegalArgumentException {
		return inventory.all(material);
	}

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
	@Deprecated
	public boolean contains(int materialId) {
		return inventory.contains(materialId);
	}

	@Override
	public boolean contains(Material material) throws IllegalArgumentException {
		return inventory.contains(material);
	}

	@Override
	public boolean contains(ItemStack item) {
		return inventory.contains(item);
	}

	@Override
	@Deprecated
	public boolean contains(int materialId, int amount) {
		return inventory.contains(materialId, amount);
	}

	@Override
	public boolean contains(Material material, int amount) throws IllegalArgumentException {
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
	@Deprecated
	public int first(int materialId) {
		return inventory.first(materialId);
	}

	@Override
	public int first(Material material) throws IllegalArgumentException {
		return inventory.first(material);
	}

	@Override
	public int first(ItemStack item) {
		return inventory.first(item);
	}

	@Override
	public int firstEmpty() {
		return inventory.firstEmpty();
	}

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
	public String getName() {
		return inventory.getName();
	}

	@Override
	public int getSize() {
		return inventory.getSize();
	}

	@Override
	public String getTitle() {
		return inventory.getTitle();
	}

	@Override
	public InventoryType getType() {
		return inventory.getType();
	}

	@Override
	public List<HumanEntity> getViewers() {
		return inventory.getViewers();
	}

	@Override
	public ListIterator<ItemStack> iterator() {
		return inventory.iterator();
	}

	@Override
	public ListIterator<ItemStack> iterator(int index) {
		return inventory.iterator(index);
	}

	@Override
	@Deprecated
	public void remove(int materialId) {
		inventory.remove(materialId);
	}

	@Override
	public void remove(Material material) throws IllegalArgumentException {
		inventory.remove(material);
	}

	@Override
	public void remove(ItemStack item) {
		inventory.remove(item);
	}

	@Override
	public HashMap<Integer, ItemStack> removeItem(ItemStack... items)
			throws IllegalArgumentException {
		return inventory.removeItem(items);
	}

	@Override
	public void setContents(ItemStack[] items) throws IllegalArgumentException {
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

	@Override
	public ItemStack[] getStorageContents() {
		return inventory.getStorageContents();
	}

	@Override
	public void setStorageContents(ItemStack[] arg0) throws IllegalArgumentException {
		inventory.setStorageContents(arg0);
	}

}
