package com.fyxridd.lib.core.api;

import com.fyxridd.lib.core.api.inter.*;
import com.fyxridd.lib.core.show.ShowListImpl;
import com.fyxridd.lib.core.show.ShowManager;
import com.fyxridd.lib.core.show.ShowMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ShowApi {
    /**
     * 注册键值获取器
     * @param plugin 插件
     * @param key 键名
     * @param mapHandler 键值获取器
     */
    public static void register(String plugin, String key, MapHandler mapHandler) {
        ShowMap.register(plugin, key, mapHandler);
    }

    /**
     * @see com.fyxridd.lib.core.show.ShowManager#register(String, String)
     * @param plugin
     * @param name
     */
    public static void register(String plugin, String name) {
        ShowManager.register(plugin, name);
    }

    /**
     * @see ShowManager#getPageControl()
     */
    public static FancyMessage getPageControl() {
        return ShowManager.getPageControl();
    }

    /**
     * @see ShowManager#getListControl()
     */
    public static FancyMessage getListControl() {
        return ShowManager.getListControl();
    }

    /**
     * @see ShowManager#show(com.fyxridd.lib.core.api.inter.ShowInterface, Object, org.bukkit.entity.Player, String, String, com.fyxridd.lib.core.api.inter.ShowList, java.util.HashMap, java.util.List, java.util.List)
     */
    public static void show(ShowInterface callback, Object obj, Player p, String plugin, String pageName,
                            ShowList list, HashMap<String, Object> data, List<FancyMessage> front, List<FancyMessage> behind) {
        ShowManager.show(callback, obj, p, plugin, pageName, list, data, front, behind);
    }

    /**
     * @see ShowManager#show(ShowInterface, Object, org.bukkit.entity.Player, String, String, ShowList, java.util.HashMap, int, int, java.util.List, java.util.List)
     */
    public static void show(ShowInterface callback, Object obj, Player p, String plugin, String pageName,
                            ShowList<Object> list, HashMap<String, Object> data, int pageNow, int listNow,
                            List<FancyMessage> front, List<FancyMessage> behind) {
        ShowManager.show(callback, obj, p, plugin, pageName, list, data, pageNow, listNow, front, behind);
    }

    /**
     * @see ShowManager#show(ShowInterface, Object, org.bukkit.entity.Player, String, String, ShowList, java.util.HashMap, java.util.List, java.util.List, java.util.HashMap)
     */
    public static void show(ShowInterface callback, Object obj, Player p, String plugin, String pageName,
                            ShowList<Object> list, HashMap<String, Object> data,
                            List<FancyMessage> front, List<FancyMessage> behind, HashMap<String, ItemStack> itemHash) {
        ShowManager.show(callback, obj, p, plugin, pageName, list, data, front, behind, itemHash);
    }

    /**
     * @see ShowManager#show(ShowInterface, Object, org.bukkit.entity.Player, String, String, ShowList, java.util.HashMap, int, int, java.util.List, java.util.List, java.util.HashMap)
     */
    public static void show(ShowInterface callback, Object obj, Player p, String plugin, String pageName,
                            ShowList<Object> list, HashMap<String, Object> data, int pageNow, int listNow,
                            List<FancyMessage> front, List<FancyMessage> behind, HashMap<String, ItemStack> itemHash) {
        ShowManager.show(callback, obj, p, plugin, pageName, list, data, pageNow, listNow, front, behind, itemHash);
    }

    /**
     * @see ShowManager#tip(org.bukkit.entity.Player, java.util.List, boolean)
     */
    public static void tip(Player p, String msg, boolean force) {
        if (msg == null) return;
        ShowManager.tip(p, MessageApi.convert(msg), force);
    }

    /**
     * @see ShowManager#tip(org.bukkit.entity.Player, java.util.List, boolean)
     */
    public static void tip(Player p, FancyMessage msg, boolean force) {
        List<FancyMessage> tipList = new ArrayList<FancyMessage>();
        tipList.add(msg);
        ShowManager.tip(p, tipList, force);
    }

    /**
     * @see ShowManager#tip(org.bukkit.entity.Player, java.util.List, boolean)
     */
    public static void tip(Player p, List<FancyMessage> msgList, boolean force) {
        ShowManager.tip(p, msgList, force);
    }

    /**
     * @see ShowManager#getMsg(java.util.LinkedHashMap, int)
     */
    public static FancyMessage getMsg(LinkedHashMap<Integer, Page.LineContext> lines, int line) {
        return ShowManager.getMsg(lines, line);
    }

    /**
     * @see ShowManager#reShow(com.fyxridd.lib.core.api.inter.PlayerContext)
     */
    public static void reShow(PlayerContext pc) {
        ShowManager.reShow(pc);
    }


    /**
     * @see ShowManager#setTip(org.bukkit.entity.Player, FancyMessage)
     */
    public static void setTip(Player p, FancyMessage tip) {
        ShowManager.setTip(p, tip);
    }

    /**
     * @see ShowManager#setTip(org.bukkit.entity.Player, java.util.List)
     */
    public static void setTip(Player p, List<FancyMessage> tip) {
        ShowManager.setTip(p, tip);
    }

    /**
     * @see ShowManager#getPageHash()
     */
    public static HashMap<String, HashMap<String, Page>> getPageHash() {
        return ShowManager.getPageHash();
    }

    /**
     * @see ShowManager#hasRegistered(String, String)
     */
    public static boolean hasRegistered(String plugin, String page) {
        return ShowManager.hasRegistered(plugin, page);
    }

    /**
     * @see ShowManager#getPage(String, String)
     */
    public static Page getPage(String plugin, String page) {
        return ShowManager.getPage(plugin, page);
    }

    /**
     * @see ShowManager#back(org.bukkit.entity.Player)
     */
    public static void back(Player p) {
        ShowManager.back(p);
    }

    /**
     * @see ShowManager#exit(org.bukkit.entity.Player, boolean)
     */
    public static void exit(Player p, boolean tip) {
        ShowManager.exit(p, tip);
    }

    /**
     * @see ShowManager#exit(org.bukkit.entity.Player, boolean, boolean)
     */
    public static void exit(Player p, boolean tip, boolean successTip) {
        ShowManager.exit(p, tip, successTip);
    }

    /**
     * @see ShowManager#isInPage(org.bukkit.entity.Player)
     */
    public static boolean isInPage(Player p) {
        return ShowManager.isInPage(p);
    }

    /**
     * @see ShowManager#clearTip(org.bukkit.entity.Player, boolean, boolean)
     */
    public static void clearTip(Player p, boolean show, boolean cancelInput) {
        ShowManager.clearTip(p, show, cancelInput);
    }

    /**
     * @see com.fyxridd.lib.core.show.ShowListImpl#ShowListImpl(int, Object)
     */
    public static ShowList getShowList(int type, Object list) {
        return new ShowListImpl(type, list);
    }

    /**
     * @see com.fyxridd.lib.core.show.ShowListImpl#ShowListImpl(int, Object, Class)
     */
    public static ShowList getShowList(int type, Object list, Class classType) {
        return new ShowListImpl(type, list, classType);
    }

    /**
     * @see com.fyxridd.lib.core.show.ShowManager#load(String, String)
     */
    public static Page load(String plugin, String page) {
       return ShowManager.load(plugin, page);
    }

    /**
     * @see com.fyxridd.lib.core.show.ShowManager#load(String, String, org.bukkit.configuration.file.YamlConfiguration)
     */
    public static Page load(String plugin, String page, YamlConfiguration config) {
        return ShowManager.load(plugin, page, config);
    }

    /**
     * @see com.fyxridd.lib.core.show.ShowManager#save(com.fyxridd.lib.core.api.inter.Page)
     */
    public static boolean save(Page page) {
        return ShowManager.save(page);
    }
}

