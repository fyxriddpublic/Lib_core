package com.fyxridd.lib.core.eco;

import org.bukkit.entity.Player;

/**
 * 经济处理接口
 */
public interface EcoHandler {
    /**
     * @see #get(String)
     */
    public double get(Player p);

    /**
     * 获取玩家的金币
     *
     * @param name 精确的玩家名,不为null
     * @return 没有金币返回0, 账户不存在返回-1
     */
    public double get(String name);

    /**
     * @see #set(String, double)
     */
    public boolean set(Player p, int amount);

    /**
     * @see #set(String, double)
     */
    public boolean set(String name, int amount);

    /**
     * @see #set(String, double)
     */
    public boolean set(Player p, double amount);

    /**
     * 注意: 返回值并不能准确地代表结果,玩家钱超过上下限时会自动调整
     * @param name 玩家名,不为null
     * @param amount 数量,小于0时会自动设置为0,大于max时会自动设置为max
     * @return 是否设置成功, 账户不存在或异常返回false
     */
    public boolean set(String name, double amount);

    /**
     * @see #add(String, double)
     */
    public boolean add(Player p, int amount);

    /**
     * @see #add(String, double)
     */
    public boolean add(String name, int amount);

    /**
     * @see #add(String, double)
     */
    public boolean add(Player p, double amount);

    /**
     * 注意: 返回值并不能准确地代表结果,玩家钱超过上下限时会自动调整
     * @param name 精确的玩家名,不为null
     * @param amount 增加的数量,小于0时自动设置为0
     * @return 是否增加成功, 账户不存在或异常返回false
     */
    public boolean add(String name, double amount);

    /**
     * @see #del(String, double)
     */
    public boolean del(Player p, int amount);

    /**
     * @see #del(String, double)
     */
    public boolean del(String name, int amount);

    /**
     * @see #del(String, double)
     */
    public boolean del(Player p, double amount);

    /**
     * 注意: 返回值并不能准确地代表结果,玩家钱超过上下限时会自动调整
     * @param name   精确的玩家名,不为null
     * @param amount 减少的数量,小于0时自动设置为0
     * @return 是否减少成功,账户不存在或异常返回false
     */
    public boolean del(String name, double amount);
}
