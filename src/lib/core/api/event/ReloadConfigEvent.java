package lib.core.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * (重新)读取配置文件结束后调用的事件,意在各部分更新数据<br>
 * 注意事件的优先级必须大于LOWEST,否则读取到的语言文件可能还是旧的!
 */
public class ReloadConfigEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private String plugin;

	public ReloadConfigEvent(String plugin) {
		this.plugin = plugin;
	}
	
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	/**
	 * 获取发出此事件的插件<br>
	 * 指哪个插件需要重新读取配置文件
	 */
	public String getPlugin() {
		return plugin;
	}
}