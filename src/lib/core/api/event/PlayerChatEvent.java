package lib.core.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerChatEvent extends Event{
    private static final HandlerList handlers = new HandlerList();

    private Player p;
    private String msg;
    private boolean cancelled;

    public PlayerChatEvent(Player p, String msg) {
        this.p = p;
        this.msg = msg;
    }

    public Player getP() {
        return p;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
