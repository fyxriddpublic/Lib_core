package com.fyxridd.lib.core;

import com.fyxridd.lib.core.api.*;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import com.fyxridd.lib.core.api.event.TimeEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Speed implements Listener{
    //是否有前置插件!Lib_msg(可选)
    private static boolean hasLibMsg = true;

    //配置

    private static int clearInterval;
    private static Integer[] levels;

    private static boolean sideEnable;
    private static int sideLine;
    private static int sideClear;

    //缓存

    //长期
	//插件 类型 玩家名 时间
	private static HashMap<String, HashMap<String, HashMap<String, Long>>> speedHash = new HashMap<String, HashMap<String, HashMap<String, Long>>>();

    //短期
    //玩家 插件 类型
    private static HashMap<Player, HashMap<String, HashMap<String, Long>>> shortHash = new HashMap<Player, HashMap<String, HashMap<String, Long>>>();

    //在1-sideClear间循环
    private static int count;
    //1-count 玩家 无用
    private static HashMap<Integer, HashMap<Player, Boolean>> sideHash = new HashMap<Integer, HashMap<Player, Boolean>>();

	public Speed() {
        //读取配置文件
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //计时器
        Bukkit.getScheduler().scheduleSyncRepeatingTask(CorePlugin.instance, new Runnable() {
            @Override
            public void run() {
                checkSide();
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
    }

    /**
	 * 注册插件的类型<br>
     * 会覆盖旧的
	 * @param plugin 插件,不为null
	 * @param type 类型,不为null
	 */
	public static void register(String plugin, String type) {
		if (!speedHash.containsKey(plugin)) speedHash.put(plugin, new HashMap<String, HashMap<String,Long>>());
		speedHash.get(plugin).put(type, new HashMap<String, Long>());
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
        try {
            String name = p.getName();
            long now = System.currentTimeMillis();
            Long pre = speedHash.get(plugin).get(type).get(name);
            if (pre != null && now-pre<limit) {
                if (tip) {
                    double wait = CoreApi.getDouble(((double) limit - (now - pre)) / 1000, 1);
                    ShowApi.tip(p, get(1000, wait), true);
                }
                return false;
            }
            speedHash.get(plugin).get(type).put(name, now);
            return true;
        } catch (Exception e) {
            CoreApi.debug(e.getMessage());
        }
        return false;
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
        //数据
        HashMap<String, HashMap<String, Long>> hash = shortHash.get(p);
        if (hash == null) {
            hash = new HashMap<String, HashMap<String, Long>>();
            shortHash.put(p, hash);
        }
        HashMap<String, Long> hash2 = hash.get(plugin);
        if (hash2 == null) {
            hash2 = new HashMap<String, Long>();
            hash.put(plugin, hash2);
        }
        //检测
        long now = System.currentTimeMillis();
        Long pre = hash2.get(type);
        int limit = levels[level-1];
        if (pre != null && now-pre<limit) {//速度过快
            double wait = CoreApi.getDouble(((double)limit - (now - pre))/1000, 1);
            tip(p, get(1000, wait).getText());
            return false;
        }
        //速度正常
        hash2.put(type, now);
        return true;

    }

    /**
     * 每1tick检测所有玩家,清除侧边栏速度过快提示
     */
    private static void checkSide() {
        if (sideEnable) {
            count ++;
            if (count > sideClear) count = 1;

            HashMap<Player, Boolean> hash = sideHash.get(count);
            if (hash != null && !hash.isEmpty()) {
                for (Player p : hash.keySet()) tip(p, null);
                //清空
                hash.clear();
            }
        }
    }

    /**
     * 提示玩家速度过快
     * @param p 玩家,不为null
     * @param msg 信息,可为null
     */
    private static void tip(Player p, String msg) {
        try {
            if (sideEnable && hasLibMsg) {
                //msg不为null表示非清空提示
                //检测添加到缓存
                if (msg != null) {
                    HashMap<Player, Boolean> hash = sideHash.get(count);
                    if (hash == null) {
                        hash = new HashMap<Player, Boolean>();
                        sideHash.put(count, hash);
                    }
                    hash.put(p, true);
                }
                //提示
                //todo
                //MsgApi.setSideShowItem(p, sideLine, msg);
                return;
            }
        } catch (Exception e) {
            //异常表示无前置插件
            hasLibMsg = false;
        }
        //聊天栏显示
        ShowApi.tip(p, msg, false);
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
        sideLine = config.getInt("speed.side.line");

        //sideClear
        sideClear = config.getInt("speed.side.clear");
        if (sideClear < 1) {
            sideClear = 1;
            ConfigApi.log(CorePlugin.pn, "speed.side.clear < 1");
        }

        //清空重置hash
        sideHash.clear();
    }

    private static FancyMessage get(int id, Object... args) {
        return FormatApi.get(CorePlugin.pn, id, args);
    }
}
