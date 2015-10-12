package com.fyxridd.lib.core;

import com.fyxridd.lib.core.api.*;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import com.fyxridd.lib.core.api.event.TimeEvent;
import com.fyxridd.lib.msg.api.MsgApi;
import com.fyxridd.lib.msg.api.SideHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class Speed implements Listener{
    private class SpeedHandler implements SideHandler {
        @Override
        public String get(Player p, String data) {
            Long waitTime = waitHash.get(p);
            if (waitTime != null) return getWaitShow(waitTime);
            return "";
        }
    }

    private static final String HANDLER_NAME = "speed";

    //配置

    private static int clearInterval;
    private static Integer[] levels;

    private static boolean sideEnable;
    private static long sideClear;

    //缓存

    //可能为null
    private static Object speedHandler;

    //长期
	//插件 类型 玩家名 时间
	private static HashMap<String, HashMap<String, HashMap<String, Long>>> speedHash = new HashMap<>();

    //短期
    //玩家 插件 类型
    private static HashMap<Player, HashMap<String, HashMap<String, Long>>> shortHash = new HashMap<>();

    //玩家 开始时间点(与清除提示有关)
    private static HashMap<Player, Long> startHash = new HashMap<>();
    //玩家 需要等待的时间(与清除提示无关)
    private static HashMap<Player, Long> waitHash = new HashMap<>();

	public Speed() {
        if (CoreMain.libMsgHook) {
            speedHandler = new SpeedHandler();
            //注册获取器
            MsgApi.registerSideHandler(HANDLER_NAME, (SideHandler) speedHandler);
        }
        //读取配置文件
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //计时器
        //每1tick检测所有玩家,清除过期数据及侧边栏提示
        Bukkit.getScheduler().scheduleSyncRepeatingTask(CorePlugin.instance, new Runnable() {
            @Override
            public void run() {
                if (sideEnable) {
                    long endTime = System.currentTimeMillis()-sideClear;
                    Iterator<Map.Entry<Player, Long>> it = startHash.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<Player, Long> entry = it.next();
                        if (entry.getValue() < endTime) {
                            it.remove();
                            waitHash.remove(entry.getKey());
                            if (CoreMain.libMsgHook) MsgApi.updateSideShow(entry.getKey(), HANDLER_NAME);
                        }
                    }
                }
            }
        }, 1, 1);
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) {
            loadConfig();
        }
    }

    @EventHandler(priority=EventPriority.LOW)
    public void onTime(TimeEvent e) {
        if (TimeEvent.getTime()%clearInterval == 0) {
            Long now = System.currentTimeMillis();
            long limit = clearInterval*1000;
            Iterator<Player> it1 = shortHash.keySet().iterator();//玩家
            while (it1.hasNext()) {
                Player p = it1.next();
                HashMap<String, HashMap<String, Long>> hash = shortHash.get(p);
                Iterator<String> it2 = hash.keySet().iterator();//插件
                while (it2.hasNext()) {
                    String plugin = it2.next();
                    HashMap<String, Long> hash2 = hash.get(plugin);
                    Iterator<String> it3 = hash2.keySet().iterator();//类型
                    while (it3.hasNext()) {
                        String type = it3.next();
                        Long pre = hash2.get(type);
                        if (now - pre > limit) {//超过清理限制
                            it3.remove();
                        }
                    }
                    if (hash2.isEmpty()) it2.remove();
                }
                if (hash.isEmpty()) it1.remove();
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        //删除短期
        shortHash.remove(e.getPlayer());
        startHash.remove(e.getPlayer());
        waitHash.remove(e.getPlayer());
    }

    /**
     * @see #check(org.bukkit.entity.Player, String, String, int, boolean)
     */
	public static boolean check(Player p, String plugin, String type, int limit) {
        return check(p, plugin, type, limit, true);
	}

    /**
     * 速度检测<br>
     * 会提示在聊天栏
     * @param p 玩家,不为null
     * @param plugin 插件,不为null
     * @param type 类型,不为null
     * @param limit 限制,单位毫秒,>=0
     * @param tip 速度过快时是否提示玩家(不是界面的强制显示)
     * @return true表示速度在允许范围内,false表示速度过快
     */
    public static boolean check(Player p, String plugin, String type, int limit, boolean tip) {
        String name = p.getName();
        long now = System.currentTimeMillis();
        HashMap<String, HashMap<String, Long>> pluginHash = speedHash.get(plugin);
        if (pluginHash == null) {
            pluginHash = new HashMap<>();
            speedHash.put(plugin, pluginHash);
        }
        HashMap<String, Long> typeHash = pluginHash.get(type);
        if (typeHash == null) {
            typeHash = new HashMap<>();
            pluginHash.put(type, typeHash);
        }
        Long pre = typeHash.get(name);
        if (pre != null && now-pre<limit) {
            if (tip) {
                double wait = CoreApi.getDouble(((double) limit - (now - pre)) / 1000, 1);
                ShowApi.tip(p, get(1000, wait), true);
            }
            return false;
        }
        typeHash.put(name, now);
        return true;
    }

    /**
     * 检测短期间隔<br>
     * 会提示在侧边栏<br>
     * 注:短期间隔不用注册
     * @param p 玩家,不为null
     * @param plugin 插件,不为null
     * @param type 类型,不为null
     * @param level 等级,从1开始,配置文件中定义
     * @return true表示速度在允许范围内,false表示速度过快
     */
    public static boolean checkShort(Player p, String plugin, String type, int level) {
        //检测level
        if (level < 1 || level > levels.length) return false;
        //数据c
        HashMap<String, HashMap<String, Long>> hash = shortHash.get(p);
        if (hash == null) {
            hash = new HashMap<>();
            shortHash.put(p, hash);
        }
        HashMap<String, Long> hash2 = hash.get(plugin);
        if (hash2 == null) {
            hash2 = new HashMap<>();
            hash.put(plugin, hash2);
        }
        //检测
        long now = System.currentTimeMillis();
        Long pre = hash2.get(type);
        int limit = levels[level-1];
        if (pre != null && now-pre<limit) {//速度过快
            long wait = limit - (now - pre);
            startHash.put(p, now);
            waitHash.put(p, wait);
            if (sideEnable && speedHandler != null) MsgApi.updateSideShow(p, HANDLER_NAME);
            else ShowApi.tip(p, getWaitShow(wait), false);
            return false;
        }
        //速度正常
        hash2.put(type, now);
        return true;

    }

    private static String getWaitShow(long wait) {
        return Speed.get(1000, CoreApi.getDouble((double)wait/1000, 1)).getText();
    }

    private static void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        //clearInterval
        clearInterval = config.getInt("speed.short.clearInterval");
        if (clearInterval < 1) {
            clearInterval = 1;
            ConfigApi.log(CorePlugin.pn, "speed.short.clearInterval < 1");
        }

        //levels
        try {
            List<Integer> list = config.getIntegerList("speed.short.levels");
            levels = new Integer[list.size()];
            int index = 0;
            for (int limit:list) {
                levels[index++] = limit;
            }
        } catch (Exception e) {
            ConfigApi.log(CorePlugin.pn, "speed.short.levels load error");
        }

        //sideEnable
        sideEnable = config.getBoolean("speed.side.enable");

        //sideClear
        sideClear = config.getLong("speed.side.clear");
        if (sideClear < 1) {
            sideClear = 1;
            ConfigApi.log(CorePlugin.pn, "speed.side.clear < 1");
        }
    }

    private static FancyMessage get(int id, Object... args) {
        return FormatApi.get(CorePlugin.pn, id, args);
    }
}
