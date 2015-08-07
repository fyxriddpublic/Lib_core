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
     * 注册插件的所有页面<br>
     * 会从plugins/plugin/show文件夹里读取所有的页面信息<br>
     *     会重新注册指定插件的所有页面<br>
     *      (即使读取的页面为null也会加入注册)
     * @param plugin 插件名,不为null
     */
    public static void register(String plugin) {
        ShowManager.register(plugin);
    }

    /**
     * 注册插件的单个页面<br>
     * 会从plugins/plugin/show/name.yml里读取页面信息<br>
     *      可重新注册,会覆盖旧的信息<br>
     *      (即使读取的页面为null也会加入注册)
     * @param plugin 插件名,不为null
     * @param name 页面名,不为null
     */
    public static void register(String plugin, String name) {
        ShowManager.register(plugin, name);
    }

    public static FancyMessage getPageControl() {
        return ShowManager.getPageControl();
    }

    public static FancyMessage getListControl() {
        return ShowManager.getListControl();
    }

    /**
     * @see #show(com.fyxridd.lib.core.api.inter.ShowInterface, Object, org.bukkit.entity.Player, String, String, com.fyxridd.lib.core.api.inter.ShowList, java.util.HashMap, int, int, java.util.List, java.util.List, java.util.HashMap)
     */
    public static void show(ShowInterface callback, Object obj, Player p, String plugin, String pageName,
                            ShowList list, HashMap<String, Object> data, List<FancyMessage> front, List<FancyMessage> behind) {
        ShowManager.show(callback, obj, p, plugin, pageName, list, data, front, behind);
    }

    /**
     * @see #show(com.fyxridd.lib.core.api.inter.ShowInterface, Object, org.bukkit.entity.Player, String, String, com.fyxridd.lib.core.api.inter.ShowList, java.util.HashMap, int, int, java.util.List, java.util.List, java.util.HashMap)
     */
    public static void show(ShowInterface callback, Object obj, Player p, String plugin, String pageName,
                            ShowList<Object> list, HashMap<String, Object> data, int pageNow, int listNow,
                            List<FancyMessage> front, List<FancyMessage> behind) {
        ShowManager.show(callback, obj, p, plugin, pageName, list, data, pageNow, listNow, front, behind);
    }

    /**
     * @see #show(com.fyxridd.lib.core.api.inter.ShowInterface, Object, org.bukkit.entity.Player, String, String, com.fyxridd.lib.core.api.inter.ShowList, java.util.HashMap, int, int, java.util.List, java.util.List, java.util.HashMap)
     */
    public static void show(ShowInterface callback, Object obj, Player p, String plugin, String pageName,
                            ShowList<Object> list, HashMap<String, Object> data,
                            List<FancyMessage> front, List<FancyMessage> behind, HashMap<String, ItemStack> itemHash) {
        ShowManager.show(callback, obj, p, plugin, pageName, list, data, front, behind, itemHash);
    }

    /**
     * 显示页面
     * @param callback 回调类,用来页面跳转(刷新),null时页面跳转时不刷新
     * @param obj 功能自定义的额外保存数据,可为null
     * @param p 玩家,不为null
     * @param plugin 插件名,不为null
     * @param pageName 页面名,不为null
     * @param list 列表,可为null
     * @param data 名称-值的映射表,可为null
     * @param pageNow 当前页,>0
     * @param listNow 列表当前页,>0
     * @param front 前面额外附加的行列表,可为null
     * @param behind 后面额外附加的行列表,可为null
     * @param itemHash 物品信息替换,可为null
     */
    public static void show(ShowInterface callback, Object obj, Player p, String plugin, String pageName,
                            ShowList<Object> list, HashMap<String, Object> data, int pageNow, int listNow,
                            List<FancyMessage> front, List<FancyMessage> behind, HashMap<String, ItemStack> itemHash) {
        ShowManager.show(callback, obj, p, plugin, pageName, list, data, pageNow, listNow, front, behind, itemHash);
    }

    /**
     * @see #tip(org.bukkit.entity.Player, java.util.List, boolean)
     */
    public static void tip(Player p, String msg, boolean force) {
        if (msg == null) return;
        ShowManager.tip(p, MessageApi.convert(msg), force);
    }

    /**
     * @see #tip(org.bukkit.entity.Player, java.util.List, boolean)
     */
    public static void tip(Player p, FancyMessage msg, boolean force) {
        List<FancyMessage> tipList = new ArrayList<FancyMessage>();
        tipList.add(msg);
        ShowManager.tip(p, tipList, force);
    }

    /**
     * 各种操作对玩家进行提示的时候适合调用此方法<br>
     * 会自动根据玩家是否正在查看页面而改变显示方式<br>
     * 可以防止因玩家查看页面而显示不了,也可以让提示玩家的时候不用考虑玩家的显示状态<br>
     * 适合必须让玩家看到提示时调用
     * @param p 玩家,不为null
     * @param msgList 提示信息列表s,可为null
     * @param force false表示玩家在显示界面时不提示,true表示不管玩家有没显示界面都提示
     */
    public static void tip(Player p, List<FancyMessage> msgList, boolean force) {
        ShowManager.tip(p, msgList, force);
    }

    /**
     * 由行号获取行
     * @param lines 行列表
     * @param line 行号,-1表示页面控制行,-2表示列表控制行
     * @return 异常返回null
     */
    public static FancyMessage getMsg(LinkedHashMap<Integer, Page.LineContext> lines, int line) {
        return ShowManager.getMsg(lines, line);
    }

    /**
     * @see #reShow(com.fyxridd.lib.core.api.inter.PlayerContext, boolean)
     */
    public static void reShow(PlayerContext pc) {
        ShowManager.reShow(pc);
    }

    /**
     * 页面跳转,重新显示<br>
     * 以下情况下需要调用:<br>
     *     - 操作提示改变<br>
     *     - 分页控制:当前页改变<br>
     *     - 列表控制:列表当前页改变<br>
     *     - 其它功能自行设置的刷新操作
     * @param pc 玩家页面上下文,null时不显示
     * @param noRefresh 是否禁止刷新(一般在重新显示页面方法出错时设为true)
     */
    public static void reShow(PlayerContext pc, boolean noRefresh) {
        ShowManager.reShow(pc, noRefresh);
    }

    /**
     * @see #setTip(org.bukkit.entity.Player, java.util.List)
     */
    public static void setTip(Player p, FancyMessage tip) {
        ShowManager.setTip(p, tip);
    }

    /**
     * 设置提示信息
     * @param p 玩家,不为null
     * @param tip 提示列表,可为null
     */
    public static void setTip(Player p, List<FancyMessage> tip) {
        ShowManager.setTip(p, tip);
    }

    public static HashMap<String, HashMap<String, Page>> getPageHash() {
        return ShowManager.getPageHash();
    }

    /**
     * 检测指定插件的页面是否有注册
     * @param plugin 插件名,可为null
     * @param page 页面名,可为null
     * @return 是否有注册
     */
    public static boolean hasRegistered(String plugin, String page) {
        return ShowManager.hasRegistered(plugin, page);
    }

    /**
     * 获取插件注册的页面<br>
     * 注意: 如果指定插件的指定页面被注册,这个页面可能仍未生成或读取异常,因此会返回null<br>
     * 也就是说返回null不代表指定插件的页面没有注册<br>
     * 当然,没有注册的情况下也是返回null
     * @param plugin 插件名,可为null
     * @param page 页面名,可为null
     * @return 页面,异常返回null
     */
    public static Page getPage(String plugin, String page) {
        return ShowManager.getPage(plugin, page);
    }

    /**
     * 玩家请求返回上一页
     * @param p 玩家,不为null
     */
    public static void back(Player p) {
        ShowManager.back(p);
    }

    /**
     * @see #exit(org.bukkit.entity.Player, boolean, boolean)
     */
    public static void exit(Player p, boolean tip) {
        ShowManager.exit(p, tip);
    }

    /**
     * 玩家请求退出页面
     * @param p 玩家,不为null
     * @param tip 当前没有查看的页面时是否提示
     * @param successTip 退出页面成功时是否提示
     */
    public static void exit(Player p, boolean tip, boolean successTip) {
        ShowManager.exit(p, tip, successTip);
    }

    /**
     * 获取玩家是否正在查看界面
     * @param p 玩家,不为null
     * @return 是否正在查看界面
     */
    public static boolean isInPage(Player p) {
        return ShowManager.isInPage(p);
    }

    /**
     * 清除玩家的提示
     * @param p 玩家,不为null
     * @param show 是否重新显示
     * @param cancelInput 是否取消输入
     */
    public static void clearTip(Player p, boolean show, boolean cancelInput) {
        ShowManager.clearTip(p, show, cancelInput);
    }

    /**
     * @param type 传入的列表类型,0指List类型,1指Object[]类型,2指Collection类型,3指HashList类型
     * @param list 列表,可为null
     */
    public static ShowList getShowList(int type, Object list) {
        return new ShowListImpl(type, list);
    }

    /**
     * @see #getShowList(int, Object)
     */
    public static ShowList getShowList(int type, Object list, Class classType) {
        return new ShowListImpl(type, list, classType);
    }

    /**
     * 从plugins/plugin/show/page.yml里读取页面信息
     * @param plugin 插件名,不为null
     * @param page 页面名,不为null
     * @return 页面,异常返回null
     */
    public static Page load(String plugin, String page) {
       return ShowManager.load(plugin, page);
    }

    /**
     * 读取页面信息
     * @param plugin 插件名
     * @param page 页面名
     * @param config 页面信息保存的yml文件,不为null
     * @return 页面信息,异常返回null
     */
    public static Page load(String plugin, String page, YamlConfiguration config) {
        return ShowManager.load(plugin, page, config);
    }

    /**
     * 保存页面到文件
     * @param page 页面,不为null
     * @return 是否成功
     */
    public static boolean save(Page page) {
        return ShowManager.save(page);
    }
}

