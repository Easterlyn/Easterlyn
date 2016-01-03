package co.sblock.effects.effect.godtier.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockExpEvent;

import co.sblock.Sblock;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.BehaviorReactive;
import co.sblock.effects.effect.Effect;
import co.sblock.users.UserAspect;

import net.md_5.bungee.api.ChatColor;

/**
 * Gives bonus experience for block actions.
 * 
 * @author Jikoo
 */
public class EffectBonusBlockExperience extends Effect implements BehaviorReactive, BehaviorGodtier {

	public EffectBonusBlockExperience(Sblock plugin) {
		super(plugin, 200, 5, 5, "Inquisitor");
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
			list.add(aspect.getColor() + "Inquiring Mind");
			break;
		default:
			break;
		}
		list.add(ChatColor.WHITE + "Discover more.");
		list.add(ChatColor.GRAY + "Gain bonus experience from blocks.");
		return list;
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(BlockExpEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		BlockExpEvent exp = (BlockExpEvent) event;

		exp.setExpToDrop((int) (exp.getExpToDrop() * (level * .25 + 1)));
	}

}
