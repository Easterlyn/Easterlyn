package com.easterlyn.effects.effect.godtier.active;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorActive;
import com.easterlyn.effects.effect.BehaviorGodtier;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.users.UserAffinity;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Gives bonus experience for entity kills.
 *
 * @author Jikoo
 */
public class EffectBonusMobExperience extends Effect implements BehaviorActive, BehaviorGodtier {

	public EffectBonusMobExperience(Easterlyn plugin) {
		super(plugin, 250, 5, 5, "Veteran");
	}

	@Override
	public Collection<UserAffinity> getAffinity() {
		return Collections.singletonList(UserAffinity.DEATH);
	}

	@Override
	public List<String> getDescription(UserAffinity aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAffinity.DEATH) {
			list.add(aspect.getColor() + "Deep Thinker");
		}
		list.add(ChatColor.WHITE + "Learn faster.");
		list.add(ChatColor.GRAY + "Gain bonus experience from mob kills.");
		return list;
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Collections.singletonList(EntityDeathEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		EntityDeathEvent death = (EntityDeathEvent) event;

		if (death.getEntity().getType() == EntityType.PLAYER
				|| death.getEntity().getType() == EntityType.ENDER_DRAGON) {
			// Players drop all exp, don't multiply
			// Ender dragon drops 12000 exp, we do not need to make that number larger
			return;
		}

		death.setDroppedExp((int) (death.getDroppedExp() * (level * .25 + 1)));
	}

}
