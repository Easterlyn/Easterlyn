package com.easterlyn.effects.effect.godtier.reactive;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorGodtier;
import com.easterlyn.effects.effect.BehaviorReactive;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.users.UserAffinity;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockExpEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Gives bonus experience for block actions.
 *
 * @author Jikoo
 */
public class EffectBonusBlockExperience extends Effect implements BehaviorReactive, BehaviorGodtier {

	public EffectBonusBlockExperience(Easterlyn plugin) {
		super(plugin, 200, 5, 5, "Inquisitor");
	}

	@Override
	public Collection<UserAffinity> getAffinity() {
		return Collections.singletonList(UserAffinity.EARTH);
	}

	@Override
	public List<String> getDescription(UserAffinity aspect) {
		ArrayList<String> list = new ArrayList<>();
		if (aspect == UserAffinity.EARTH) {
			list.add(aspect.getColor() + "Inquiring Mind");
		}
		list.add(ChatColor.WHITE + "Discover more.");
		list.add(ChatColor.GRAY + "Gain bonus experience from blocks.");
		return list;
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Collections.singletonList(BlockExpEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		BlockExpEvent exp = (BlockExpEvent) event;

		exp.setExpToDrop((int) (exp.getExpToDrop() * (level * .25 + 1)));
	}

}
