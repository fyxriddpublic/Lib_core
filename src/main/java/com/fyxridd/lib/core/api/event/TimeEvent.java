package com.fyxridd.lib.core.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 时间事实,现实的每秒自动发出一次
 */
public class TimeEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	private static long Time;
	
	public TimeEvent() {
		Time++;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList() {
	    return handlers;
	}

	/**
	 * 时间,从0开始,每发生此事件值会加1
	 * @return
	 */
	public static long getTime() {
		return Time;
	}
}
