package com.fyxridd.lib.core.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 服务器关闭前发出
 */
public class ServerCloseEvent extends Event{
    private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
