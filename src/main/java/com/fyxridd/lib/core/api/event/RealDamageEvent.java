package com.fyxridd.lib.core.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class RealDamageEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	private EntityDamageByEntityEvent entityDamageByEntityEvent;
    //是否禁止后面检测暴击
    private boolean banCheckCritic;

	public RealDamageEvent(EntityDamageByEntityEvent entityDamageByEntityEvent) {
		this.entityDamageByEntityEvent = entityDamageByEntityEvent;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}

	public EntityDamageByEntityEvent getEntityDamageByEntityEvent() {
		return entityDamageByEntityEvent;
	}

    public boolean isBanCheckCritic() {
        return banCheckCritic;
    }

    public void setBanCheckCritic(boolean banCheckCritic) {
        this.banCheckCritic = banCheckCritic;
    }
}
