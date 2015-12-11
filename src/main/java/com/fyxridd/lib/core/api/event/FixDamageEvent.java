package com.fyxridd.lib.core.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * 修正伤害事件
 */
public class FixDamageEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
    private ItemStack is;
    private int damage;
    private boolean set;

    public FixDamageEvent(ItemStack is, int damage, boolean set) {
        this.is = is;
        this.damage = damage;
        this.set = set;
    }

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList() {
	    return handlers;
	}

    public ItemStack getIs() {
        return is;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isSet() {
        return set;
    }

    public void setSet(boolean set) {
        this.set = set;
    }
}
