package com.fyxridd.lib.core.api.event;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 玩家进入/走上新方块类型事件<br>
 * 注意: 与具体方块无关,只与方块类型有关,并且无法分辨方块小ID
 */
public class EnterBlockTypeEvent extends Event{
	private static final HandlerList handlers = new HandlerList();

    private Player p;
    //可能为null(如玩家进服时)
    private Material oldBlockType;
    //可能为null(如玩家退服时)
    private Material newBlockType;
    //true表示进入方块类型;false表示走上方块类型
    private boolean in;

	public EnterBlockTypeEvent(Player p, Material oldBlockType, Material newBlockType, boolean in) {
        this.p = p;
        this.oldBlockType = oldBlockType;
        this.newBlockType = newBlockType;
        this.in = in;
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

    public Material getOldBlockType() {
        return oldBlockType;
    }

    public Material getNewBlockType() {
        return newBlockType;
    }

    public boolean isIn() {
        return in;
    }
}
