package lib.core.api;

import lib.core.per.PerManager;
import org.bukkit.entity.Player;
import lib.core.per.PerHandler;

public class PerApi {
    /**
     * 检测玩家是否拥有权限,无权限会自动进行提示
     * @param p 玩家,不为null
     * @param per 权限,可为null
     * @return 是否拥有权限
     */
    public static boolean checkPer(Player p, String per) {
        if (!PerManager.perHandler.has(p, per)) {
            ShowApi.tip(p, FormatApi.get(CorePlugin.pn, 10, per), true);
            return false;
        }else return true;
    }

    /**
     * @see PerHandler#has(org.bukkit.entity.Player, String)
     */
    public static boolean has(Player p, String per) {
        return PerManager.perHandler.has(p, per);
    }

    /**
     * @see PerHandler#has(String, String)
     */
    public static boolean has(String name, String per) {
        return PerManager.perHandler.has(name, per);
    }

    /**
     * @see PerHandler#add(Player, String)
     */
    public static boolean add(Player p, String per) {
        return PerManager.perHandler.add(p, per);
    }

    /**
     * @see PerHandler#add(String, String)
     */
    public static boolean add(String name, String per) {
        return PerManager.perHandler.add(name, per);
    }

    /**
     * @see PerHandler#del(org.bukkit.entity.Player, String)
     */
    public static boolean del(Player p, String per) {
        return PerManager.perHandler.del(p, per);
    }

    /**
     * @see PerHandler#del(String, String)
     */
    public static boolean del(String name, String per) {
        return PerManager.perHandler.del(name, per);
    }

    /**
     * @see PerHandler#hasGroup(org.bukkit.entity.Player, String, boolean)
     */
    public static boolean hasGroup(Player p,String groupName, boolean loop) {
        return PerManager.perHandler.hasGroup(p, groupName, loop);
    }

    /**
     * @see PerHandler#hasGroup(String, String, boolean)
     */
    public static boolean hasGroup(String name, String groupName, boolean loop) {
        return PerManager.perHandler.hasGroup(name, groupName, loop);
    }

    /**
     * @see PerHandler#addGroup(org.bukkit.entity.Player, String)
     */
    public static boolean addGroup(Player p,String groupName) {
        return PerManager.perHandler.addGroup(p, groupName);
    }

    /**
     * @see PerHandler#addGroup(String, String)
     */
    public static boolean addGroup(String name, String groupName) {
        return PerManager.perHandler.addGroup(name, groupName);
    }

    /**
     * @see PerHandler#delGroup(org.bukkit.entity.Player, String)
     */
    public static boolean delGroup(Player p,String groupName) {
        return PerManager.perHandler.delGroup(p, groupName);
    }

    /**
     * @see PerHandler#delGroup(String, String)
     */
    public static boolean delGroup(String name, String groupName) {
        return PerManager.perHandler.delGroup(name, groupName);
    }

    /**
     * @see PerHandler#checkHasGroup(String, String)
     */
    public static boolean checkHasGroup(String tar, String groupName) {
        return PerManager.perHandler.checkHasGroup(tar, groupName);
    }
}
