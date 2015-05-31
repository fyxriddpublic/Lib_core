package lib.core.api.event;

import lib.core.api.inter.FunctionInterface;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 玩家操作事件<br>
 * 玩家打算操作时,在与具体功能交互前发出,可取消操作
 */
public class PlayerOperateEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	private Player p;
	private FunctionInterface func;
    private String subFunc;
	
	private boolean cancel;
	
	public PlayerOperateEvent(Player p, FunctionInterface func, String subFunc) {
		this.p = p;
		this.func = func;
		this.subFunc = subFunc;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}

	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}

    public String getSubFunc() {
        return subFunc;
    }

    public FunctionInterface getFunc() {
        return func;
    }

    public Player getP() {
        return p;
    }
}
