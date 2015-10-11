package co.sblock.effects.effect.godtier.active;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;

import co.sblock.effects.effect.BehaviorActive;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.Effect;
import co.sblock.users.UserAspect;

import net.md_5.bungee.api.ChatColor;

/**
 * Gives bonus experience for entity kills.
 * 
 * @author Jikoo
 */
public class EffectBonusExperience extends Effect implements BehaviorActive, BehaviorGodtier {

	public EffectBonusExperience() {
		super(Integer.MAX_VALUE, 5, 5, "Veteran");
	}

	@Override
	public Collection<UserAspect> getAspects() {
		return Arrays.asList(UserAspect.MIND);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		switch (aspect) {
		case MIND:
			list.add(aspect.getColor() + "Deep Thinker");
			break;
		default:
			break;
		}
		list.add(ChatColor.WHITE + "Learn faster.");
		list.add(ChatColor.GRAY + "Gain bonus experience from mob kills.");
		return list;
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(EntityDeathEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		EntityDeathEvent death = (EntityDeathEvent) event;

		if (death.getEntity() instanceof Player) {
			// Players drop all exp, don't multiply
			return;
		}

		death.setDroppedExp((int) (death.getDroppedExp() * (level * .2 + 1)));
	}

}
