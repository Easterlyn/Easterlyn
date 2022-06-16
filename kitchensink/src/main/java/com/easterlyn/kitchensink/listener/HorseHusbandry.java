package com.easterlyn.kitchensink.listener;

import java.util.Random;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.event.entity.EntityMountEvent;

/**
 * Make Minecraft horse husbandry less dumb. You're welcome, Kyle.
 */
public class HorseHusbandry implements Listener {

  private final Random random = new Random();
  /* Health ranges */
  private final Range healthDefault = new Range(15.0, 30.0, Attribute.GENERIC_MAX_HEALTH);
  private final Range healthUndead = new Range(15.0, Attribute.GENERIC_MAX_HEALTH);
  /* Speed ranges */
  private final Range speedHorse = new Range(0.1125, 0.3375, Attribute.GENERIC_MOVEMENT_SPEED);
  private final Range speedPackAnimal = new Range(0.175, Attribute.GENERIC_MOVEMENT_SPEED);
  private final Range speedUndead = new Range(0.2, Attribute.GENERIC_MOVEMENT_SPEED);
  /* Jump range */
  private final Range jump = new Range(0.4, 1.0, Attribute.HORSE_JUMP_STRENGTH);

  @EventHandler
  private void onHorseMount(@NotNull EntityMountEvent event) {
    if (!(event.getEntity() instanceof Player player
        && event.getMount() instanceof AbstractHorse horse)) {
      return;
    }

    Range health = getHealthRange(horse);

    if (health == null) {
      // Don't display for llamas.
      return;
    }

    player.sendTitle(
        "",
        String.format(
            "Health: %.2f%%  -  Speed: %.2f%%  -  Jump: %.2f%%",
            getSkillPercent(health, horse),
            getSkillPercent(getSpeedRange(horse), horse),
            getSkillPercent(getJumpRange(horse), horse)
        ),
        10,
        60,
        10);

  }

  private double getSkillPercent(@Nullable Range range, @NotNull AbstractHorse horse) {
    if (range == null || range.max() == range.min()) {
      return 100.0;
    }

    AttributeInstance attribute = horse.getAttribute(range.attribute());

    if (attribute == null) {
      return 100.0;
    }

    return (attribute.getBaseValue() - range.min()) / (range.max() - range.min()) * 100.0;
  }

  @EventHandler
  private void onHorseBreed(@NotNull EntityBreedEvent event) {
    if (!(event.getEntity() instanceof AbstractHorse horse
        && event.getMother() instanceof AbstractHorse parent1
        && event.getFather() instanceof AbstractHorse parent2)) {
      return;
    }

    generateNewValue(getHealthRange(horse), horse, parent1, parent2);
    generateNewValue(getSpeedRange(horse), horse, parent1, parent2);
    generateNewValue(getJumpRange(horse), horse, parent1, parent2);
  }

  private void generateNewValue(
      @Nullable Range range,
      @NotNull AbstractHorse child,
      @NotNull AbstractHorse parent1,
      @NotNull AbstractHorse parent2) {
    if (range == null) {
      return;
    }

    AttributeInstance childAttribute = child.getAttribute(range.attribute());
    AttributeInstance parent1Attribute = parent1.getAttribute(range.attribute());
    AttributeInstance parent2Attribute = parent2.getAttribute(range.attribute());

    if (childAttribute == null || parent1Attribute == null || parent2Attribute == null) {
      return;
    }

    // Get average of parents' stats.
    double parentalAverage = (parent1Attribute.getBaseValue() + parent2Attribute.getBaseValue()) / 2;

    // Add a random bonus ranging from -5% to +10% to the parents' average stats.
    double childValue = parentalAverage * random.nextDouble(0.95, 1.10);

    // Clamp and set.
    childAttribute.setBaseValue(range.clamp(childValue));
  }

  private @Nullable Range getHealthRange(AbstractHorse horse) {
    if (horse instanceof Horse || horse instanceof Donkey || horse instanceof Mule) {
      return healthDefault;
    }
    if (horse instanceof SkeletonHorse || horse instanceof ZombieHorse) {
      return healthUndead;
    }
    return null;
  }

  private @Nullable Range getSpeedRange(AbstractHorse horse) {
    if (horse instanceof Horse) {
      return speedHorse;
    }
    if (horse instanceof Mule || horse instanceof Donkey) {
      return speedPackAnimal;
    }
    if (horse instanceof SkeletonHorse || horse instanceof ZombieHorse) {
      return speedUndead;
    }
    return null;
  }

  private @Nullable Range getJumpRange(AbstractHorse horse) {
    if (horse instanceof Llama) {
      return null;
    }
    return jump;
  }

  private record Range(double min, double max, @NotNull Attribute attribute) {
    private Range(double value, @NotNull Attribute attribute) {
      this(value, value, attribute);
    }

    private double clamp(double input) {
      return Math.min(max, Math.max(min, input));
    }
  }

}
