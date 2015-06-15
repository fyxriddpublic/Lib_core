package com.fyxridd.lib.core.api.inter;

import org.bukkit.entity.Player;

/**
 * 所有功能必须实现的接口,方便统一管理
 */
public interface FunctionInterface {
	/**
	 * 获取功能名(大小写敏感)
	 * @return 不为null
	 */
	public String getName();
	
	/**
	 * 功能对玩家是否开启
	 * @param name 玩家名,不为null
	 * @param data 附加数据,可为null
	 * @return 是否开启
	 */
	public boolean isOn(String name, String data);
	
	/**
	 * 操作时调用
	 * @param p 操作的玩家,不为null
	 * @param args 操作的数据,不为null可为空(长度为0)
	 */
	public void onOperate(Player p, String... args);
}
