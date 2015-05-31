package lib.core.api.inter;

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
	 * @param subFunc 子功能,可为null
	 * @return
	 */
	public boolean isOn(String name, String subFunc);
	
	/**
	 * 设置功能对玩家的开启状态
	 * @param name 玩家名,不为null
	 * @param subFunc 子功能,可为null
	 * @param on 是否开启
	 */
	public void setOn(String name, String subFunc, boolean on);
	
	/**
	 * 操作时调用
	 * @param p 操作的玩家,不为null
	 * @param data 操作的数据,可为null
	 */
	public void onOperate(Player p, String data);
}
