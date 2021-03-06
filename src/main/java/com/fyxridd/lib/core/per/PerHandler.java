package com.fyxridd.lib.core.per;

import org.bukkit.entity.Player;

/**
 * 权限处理器接口
 */
public interface PerHandler {
    /**
     * @see #has(String, String)
     */
    public boolean has(Player p, String per);

    /**
     * 检测玩家是否有权限
     * @param name 检测的玩家名(null时返回false)
     * @param per 相应的权限(null或""时返回true)
     * @return 是否有权限
     */
    public boolean has(String name, String per);

    /**
     * @see #add(String, String)
     */
    public boolean add(Player p, String per);

    /**
     * 给玩家添加权限<br>
     *     只检测玩家本身的权限<br>
     *     不会检测默认权限组与玩家权限组中的权限
     * @param name 玩家名(null时返回false)
     * @param per 权限(null或空时返回false)
     * @return 如果已经有此权限或添加异常返回false,否则返回true
     */
    public boolean add(String name, String per);

    /**
     * @see #del(String, String)
     */
    public boolean del(Player p, String per);

    /**
     * 删除玩家的权限<br>
     *     只检测玩家本身的权限<br>
     *     不会检测默认权限组与玩家权限组中的权限
     * @param name 玩家名(null时返回false)
     * @param per 权限(null时返回false)
     * @return 如果玩家无此权限或删除异常返回false,否则返回true
     */
    public boolean del(String name, String per);

    /**
     * @see #hasGroup(java.lang.String, java.lang.String, boolean)
     */
    public boolean hasGroup(Player p, String groupName, boolean loop);

    /**
     * 检测玩家是否拥有指定的权限组
     * @param name 玩家名(null时返回false)
     * @param groupName 权限组名(null时返回false)
     * @param loop 是否递归检测,true时会检测玩家拥有的权限组与权限组的继承权限组,false时只考虑玩家直接拥有的权限组
     * @return 是否拥有指定的权限组,如果权限组不存在或异常返回false
     */
    public boolean hasGroup(String name, String groupName, boolean loop);

    /**
     * @see #addGroup(String, String)
     */
    public boolean addGroup(Player p, String groupName);

    /**
     * 给玩家添加权限组
     * @param name 玩家名(null时返回false)
     * @param groupName 组名,不为默认权限组名(null时返回false)
     * @return 如果玩家已经有此权限组(直接的,不考虑权限组的继承)或权限组不存在或添加异常返回false,否则返回true
     */
    public boolean addGroup(String name, String groupName);

    /**
     * @see #delGroup(String, String)
     */
    public boolean delGroup(Player p, String groupName);

    /**
     * 删除玩家的权限组
     * @param name 玩家名(null时返回false)
     * @param groupName 组名,不为默认权限组名(null时返回false)
     * @return 如果玩家没有此权限组(直接的,不考虑权限组的继承)或权限组不存在或删除异常返回false,否则返回true
     */
    public boolean delGroup(String name, String groupName);

    /**
     * 检测权限组tar内是否包含权限组groupName<br>
     *     会递归检测所有的继承
     * @param tar (null时返回false)
     * @param groupName (null时返回false)
     * @return 是否包含,如果权限组不存在则返回false
     */
    public boolean checkHasGroup(String tar, String groupName);

    /**
     * @see com.fyxridd.lib.core.api.PerApi#createGroup(String)
     */
    public boolean createGroup(String group);

    /**
     * @see com.fyxridd.lib.core.api.PerApi#delGroup(String)
     */
    public boolean delGroup(String group);

    /**
     * @see com.fyxridd.lib.core.api.PerApi#groupAddPer(String, String)
     */
    public boolean groupAddPer(String group, String per);

    /**
     * @see com.fyxridd.lib.core.api.PerApi#groupRemovePer(String, String)
     */
    public boolean groupRemovePer(String group, String per);

    /**
     * @see com.fyxridd.lib.core.api.PerApi#groupAddInherit(String, String)
     */
    public boolean groupAddInherit(String group, String inherit);

    /**
     * @see com.fyxridd.lib.core.api.PerApi#groupRemoveInherit(String, String)
     */
    public boolean groupRemoveInherit(String group, String inherit);
}
