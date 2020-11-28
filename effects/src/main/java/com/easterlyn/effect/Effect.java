package com.easterlyn.effect;

import com.easterlyn.EasterlynEffects;
import java.util.function.Function;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class defining properties for an Effect.
 *
 * @author Jikoo
 */
public abstract class Effect {

  private final EasterlynEffects plugin;
  private final String name;
  private final double cost;
  private final int maximumLevel;
  private final int maximumCombinedLevel;
  private final Function<EquipmentSlot, Boolean> target;

  protected Effect(
      @NotNull EasterlynEffects plugin,
      @NotNull String name,
      @NotNull Function<EquipmentSlot, Boolean> target,
      double cost,
      int maximumLevel,
      int maximumCombinedLevel) {
    this.plugin = plugin;
    this.name = name;
    this.target = target;
    this.cost = cost;
    this.maximumLevel = maximumLevel;
    this.maximumCombinedLevel = maximumCombinedLevel;
  }

  /**
   * Gets the name of the Effect.
   *
   * @return the name
   */
  public @NotNull String getName() {
    return this.name;
  }

  /**
   * Gets a function to process whether or not an EquipmentSlot is applicable for the Effect.
   *
   * @return the function
   */
  public @NotNull Function<EquipmentSlot, Boolean> getTarget() {
    return target;
  }

  /**
   * Gets the cost of the Effect.
   *
   * @return the cost
   */
  public double getCost() {
    return this.cost;
  }

  /**
   * Gets the maximum level of an Effect per gear part.
   *
   * @return the maximum level
   */
  public int getMaxLevel() {
    return this.maximumLevel;
  }

  /**
   * Gets the maximum level of an Effect for all gear parts combined.
   *
   * @return the maximum level
   */
  public int getMaxTotalLevel() {
    return this.maximumCombinedLevel;
  }

  /**
   * Gets the EasterlynEffects instance loading this Effect.
   *
   * @return the EasterlynEffects instance
   */
  public @NotNull EasterlynEffects getPlugin() {
    return this.plugin;
  }

  /**
   * Applies the Effect to a LivingEntity, potentially during an event.
   *
   * @param entity the effect's target/activator
   * @param level the level of the effect to apply
   * @param event the event causing the Effect application
   */
  public abstract void applyEffect(@NotNull LivingEntity entity, int level, @Nullable Event event);
}
