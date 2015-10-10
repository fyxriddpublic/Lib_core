package com.fyxridd.lib.core.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 玩家第一次进服事件
 */
public class FirstJoinEvent extends Event{
	private static final HandlerList handlers = new HandlerList();

    private Player p;

	public FirstJoinEvent(Player p) {
        this.p = p;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList() {
	    return handlers;
	}

    public Player getP() {
        return p;
    }
}
