package com.fyxridd.lib.core.api;

import com.fyxridd.lib.core.CoreMain;
import org.bukkit.entity.Player;
import com.fyxridd.lib.core.per.PerHandler;

public class PerApi {
    /**
     * 检测玩家是否拥有权限,无权限会自动进行提示
     * @param p 玩家,不为null
     * @param per 权限,可为null(null或""时返回true)
     * @return 是否拥有权限
     */
    public static boolean checkPer(Player p, String per) {
        if (!CoreMain.perManager.perHandler.has(p, per)) {
            ShowApi.tip(p, FormatApi.get(CorePlugin.pn, 10, per), true);
            return false;
        }else return true;
    }

    /**
     * @see PerHandler#has(org.bukkit.entity.Player, String)
     */
    public static boolean has(Player p, String per) {
        return CoreMain.perManager.perHandler.has(p, per);
    }

    /**
     * @see PerHandler#has(String, String)
     */
    public static boolean has(String name, String per) {
        return CoreMain.perManager.perHandler.has(name, per);
    }

    /**
     * @see PerHandler#add(Player, String)
     */
    public static boolean add(Player p, String per) {
        return CoreMain.perManager.perHandler.add(p, per);
    }

    /**
     * @see PerHandler#add(String, String)
     */
    public static boolean add(String name, String per) {
        return CoreMain.perManager.perHandler.add(name, per);
    }

    /**
     * @see PerHandler#del(org.bukkit.entity.Player, String)
     */
    public static boolean del(Player p, String per) {
        return CoreMain.perManager.perHandler.del(p, per);
    }

    /**
     * @see PerHandler#del(String, String)
     */
    public static boolean del(String name, String per) {
        return CoreMain.perManager.perHandler.del(name, per);
    }

    /**
     * @see PerHandler#hasGroup(org.bukkit.entity.Player, String, boolean)
     */
    public static boolean hasGroup(Player p,String groupName, boolean loop) {
        return CoreMain.perManager.perHandler.hasGroup(p, groupName, loop);
    }

    /**
     * @see PerHandler#hasGroup(String, String, boolean)
     */
    public static boolean hasGroup(String name, String groupName, boolean loop) {
        return CoreMain.perManager.perHandler.hasGroup(name, groupName, loop);
    }

    /**
     * @see PerHandler#addGroup(org.bukkit.entity.Player, String)
     */
    public static boolean addGroup(Player p,String groupName) {
        return CoreMain.perManager.perHandler.addGroup(p, groupName);
    }

    /**
     * @see PerHandler#addGroup(String, String)
     */
    public static boolean addGroup(String name, String groupName) {
        return CoreMain.perManager.perHandler.addGroup(name, groupName);
    }

    /**
     * @see PerHandler#delGroup(org.bukkit.entity.Player, String)
     */
    public static boolean delGroup(Player p,String groupName) {
        return CoreMain.perManager.perHandler.delGroup(p, groupName);
    }

    /**
     * @see PerHandler#delGroup(String, String)
     */
    public static boolean delGroup(String name, String groupName) {
        return CoreMain.perManager.perHandler.delGroup(name, groupName);
    }

    /**
     * @see PerHandler#checkHasGroup(String, String)
     */
    public static boolean checkHasGroup(String tar, String groupName) {
        return CoreMain.perManager.perHandler.checkHasGroup(tar, groupName);
    }

    /**
     * 新建组
     * @param group 组名(null时返回false)
     * @return 是否成功
     */
    public static boolean createGroup(String group) {
        return CoreMain.perManager.perHandler.createGroup(group);
    }

    /**
     * 删除组
     * @param group 组名(null时返回false)
     * @return 是否成功
     */
    public static boolean delGroup(String group) {
        return CoreMain.perManager.perHandler.delGroup(group);
    }

    /**
     * 权限组添加权限
     * @param group 权限组(null时返回false)
     * @param per 权限(null时返回false)
     * @return 是否添加成功
     */
    public static boolean groupAddPer(String group, String per) {
        return CoreMain.perManager.perHandler.groupAddPer(group, per);
    }

    /**
     * 权限组删除权限
     * @param group 权限组(null时返回false)
     * @param per 权限(null时返回false)
     * @return 是否删除成功
     */
    public static boolean groupRemovePer(String group, String per) {
        return CoreMain.perManager.perHandler.groupRemovePer(group, per);
    }

    /**
     * 权限组添加继承
     * @param group 权限组(null时返回false)
     * @param inherit 继承(null时返回false)
     * @return 是否添加成功
     */
    public static boolean groupAddInherit(String group, String inherit) {
        return CoreMain.perManager.perHandler.groupAddInherit(group, inherit);
    }

    /**
     * 权限组删除继承
     * @param group 权限组(null时返回false)
     * @param inherit 继承(null时返回false)
     * @return 是否删除成功
     */
    public static boolean groupRemoveInherit(String group, String inherit) {
        return CoreMain.perManager.perHandler.groupRemoveInherit(group, inherit);
    }
}
