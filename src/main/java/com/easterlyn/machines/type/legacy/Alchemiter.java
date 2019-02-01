package com.easterlyn.machines.type.legacy;

import java.util.ArrayList;
import java.util.UUID;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.captcha.CruxiteDowel;
import com.easterlyn.chat.Language;
import com.easterlyn.effects.Effects;
import com.easterlyn.machines.MachineInventoryTracker;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;
import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.machines.utilities.Shape.MaterialDataValue;
import com.easterlyn.utilities.Experience;
import com.easterlyn.utilities.InventoryUtils;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

/**
 * Simulate a Sburb Alchemiter in Minecraft.
 *
 * @author Jikoo
 */
public class Alchemiter extends Machine {

    private static Triple<ItemStack, ItemStack, ItemStack> exampleRecipes;

    private final Captcha captcha;
    private final Effects effects;
    private final MachineInventoryTracker tracker;
    private final ItemStack drop, barrier;

    public Alchemiter(Easterlyn plugin, Machines machines) {
        super(plugin, machines, new Shape(), "Alchemiter");
        this.captcha = plugin.getModule(Captcha.class);
        this.effects = plugin.getModule(Effects.class);
        this.tracker = machines.getInventoryTracker();
        Shape shape = getShape();
        MaterialDataValue m = shape.new MaterialDataValue(Material.QUARTZ_BLOCK, (byte) 1);
        shape.setVectorData(new Vector(0, 0, 0), m);
        shape.setVectorData(new Vector(0, 0, 1), m);
        shape.setVectorData(new Vector(1, 0, 1), m);
        shape.setVectorData(new Vector(1, 0, 0), m);
        m = shape.new MaterialDataValue(Material.QUARTZ_BLOCK, (byte) 2);
        shape.setVectorData(new Vector(0, 0, 2), m);
        m = shape.new MaterialDataValue(Material.NETHER_FENCE);
        shape.setVectorData(new Vector(0, 1, 2), m);
        shape.setVectorData(new Vector(0, 2, 2), m);
        shape.setVectorData(new Vector(0, 3, 2), m);
        shape.setVectorData(new Vector(0, 3, 1), m);
        m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.NORTH, "stair");
        shape.setVectorData(new Vector(-1, 0, -1), m);
        shape.setVectorData(new Vector(0, 0, -1), m);
        shape.setVectorData(new Vector(1, 0, -1), m);
        shape.setVectorData(new Vector(2, 0, -1), m);
        m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.SOUTH, "stair");
        shape.setVectorData(new Vector(-1, 0, 2), m);
        shape.setVectorData(new Vector(1, 0, 2), m);
        shape.setVectorData(new Vector(2, 0, 2), m);
        m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.EAST, "stair");
        shape.setVectorData(new Vector(-1, 0, 1), m);
        shape.setVectorData(new Vector(-1, 0, 0), m);
        m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.WEST, "stair");
        shape.setVectorData(new Vector(2, 0, 1), m);
        shape.setVectorData(new Vector(2, 0, 0), m);

        drop = new ItemStack(Material.QUARTZ_BLOCK, 1, (short) 2);
        ItemMeta meta = drop.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Alchemiter");
        drop.setItemMeta(meta);

        barrier = new ItemStack(Material.BARRIER);
        meta.setDisplayName(Language.getColor("emphasis.bad") + "No Result");
        barrier.setItemMeta(meta);
    }

    @Override
    public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
        if (super.handleInteract(event, storage)) {
            return true;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return true;
        }
        if (event.getPlayer().isSneaking()) {
            return false;
        }
        tracker.openVillagerInventory(event.getPlayer(), this, getKey(storage));
        InventoryUtils.updateVillagerTrades(event.getPlayer(), getExampleRecipes());
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean handleClick(InventoryClickEvent event, ConfigurationSection storage) {
        updateInventory(event.getWhoClicked().getUniqueId());
        if (event.getRawSlot() != event.getView().convertSlot(event.getRawSlot())) {
            // Clicked inv is not the top.
            return false;
        }
        if (event.getSlot() == 1) {
            // Exp slot is being clicked. No adding or removing items.
            return true;
        }
        if (event.getSlot() == 2 && event.getCurrentItem() != null
                && event.getCurrentItem().getType() != Material.AIR) {
            if (event.getCurrentItem().getType() == Material.BARRIER) {
                return true;
            }
            // Item is being crafted
            Inventory top = event.getView().getTopInventory();
            Player player = (Player) event.getWhoClicked();
            if (event.getClick().name().contains("SHIFT")) {
                if (InventoryUtils.hasSpaceFor(event.getCurrentItem(), player.getInventory())) {
                    player.getInventory().addItem(event.getCurrentItem().clone());
                } else {
                    return true;
                }
            } else if (event.getCursor() == null || event.getCursor().getType() == Material.AIR
                    || (event.getCursor().isSimilar(event.getCurrentItem())
                    && event.getCursor().getAmount() + event.getCurrentItem().getAmount()
                    < event.getCursor().getMaxStackSize())) {
                ItemStack result = event.getCurrentItem().clone();
                if (result.isSimilar(event.getCursor())) {
                    result.setAmount(result.getAmount() + event.getCursor().getAmount());
                }
                event.setCursor(result);
            } else {
                return true;
            }
            event.setCurrentItem(null);
            top.setItem(0, InventoryUtils.decrement(top.getItem(0), 1));
            // Color code + "Grist cost: " = 14 chars
            int expCost = Integer.valueOf(top.getItem(1).getItemMeta().getDisplayName().substring(14));
            Experience.changeExp(player, -expCost);
        }
        return false;
    }

    @Override
    public boolean handleClick(InventoryDragEvent event, ConfigurationSection storage) {
        updateInventory(event.getWhoClicked().getUniqueId());
        // Raw slot 1 = second slot of top inventory
        return event.getRawSlots().contains(1);
    }

    /**
     * Calculate result slot and update inventory on a delay (post-event completion)
     *
     * @param name the name of the player who is using the Punch Designix
     */
    public void updateInventory(final UUID id) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
            @Override
            public void run() {
                // Must re-obtain player or update doesn't seem to happen
                Player player = Bukkit.getPlayer(id);
                if (player == null || !tracker.hasMachineOpen(player)) {
                    // Player has logged out or closed inventory. Inventories are per-player, ignore.
                    return;
                }

                Inventory open = player.getOpenInventory().getTopInventory();
                ItemStack input = open.getItem(0);
                ItemStack expCost;
                ItemStack result;
                if (CruxiteDowel.isDowel(input)) {
                    input = input.clone();
                    input.setAmount(1);
                    result = captcha.captchaToItem(input);
                    expCost = new ItemStack(Material.EXP_BOTTLE);
                    int exp = CruxiteDowel.expCost(effects, result);
                    ItemMeta im = expCost.getItemMeta();
                    int playerExp = Experience.getExp(player);
                    int remainder = playerExp - exp;
                    ChatColor color = remainder >= 0 ? ChatColor.GREEN : ChatColor.DARK_RED;
                    im.setDisplayName(color + "Grist cost: " + exp);
                    ArrayList<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GOLD + "Current: " + playerExp);
                    if (remainder >= 0) {
                        lore.add(ChatColor.GOLD + "Remainder: " + remainder);
                    } else {
                        lore.add(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Not enough grist!");
                        result = null;
                    }
                    im.setLore(lore);
                    expCost.setItemMeta(im);
                } else {
                    result = null;
                    expCost = null;
                }
                // Set items
                open.setItem(1, expCost);
                open.setItem(2, result);
                InventoryUtils.updateVillagerTrades(player, getExampleRecipes(),
                        new ImmutableTriple<>(input, expCost, result == null ? barrier : result));
                player.updateInventory();
            }
        });
    }

    @Override
    public ItemStack getUniqueDrop() {
        return drop;
    }

    /**
     * Singleton for getting usage help ItemStacks.
     */
    public static Triple<ItemStack, ItemStack, ItemStack> getExampleRecipes() {
        if (exampleRecipes == null) {
            exampleRecipes = createExampleRecipes();
        }
        return exampleRecipes;
    }

    /**
     * Creates the ItemStacks used in displaying usage help.
     *
     * @return
     */
    private static Triple<ItemStack, ItemStack, ItemStack> createExampleRecipes() {
        ItemStack is1 = new ItemStack(Material.NETHER_BRICK_ITEM);
        ItemMeta im = is1.getItemMeta();
        im.setDisplayName(ChatColor.GOLD + "Cruxite Totem");
        ArrayList<String> lore = new ArrayList<>();
        is1.setItemMeta(im);

        ItemStack is2 = new ItemStack(Material.BARRIER);
        im = is2.getItemMeta();
        im.setDisplayName(ChatColor.GOLD + "Grist Cost");
        lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "This will display when a");
        lore.add(ChatColor.WHITE + "valid totem is inserted.");
        im.setLore(lore);
        is2.setItemMeta(im);

        ItemStack is3 = new ItemStack(Material.DIRT);
        im = is3.getItemMeta();
        im.setDisplayName(ChatColor.GOLD + "Perfectly Generic Result");
        lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "Your result here.");
        im.setLore(lore);
        is3.setItemMeta(im);

        return new ImmutableTriple<>(is1, is2, is3);
    }

}
