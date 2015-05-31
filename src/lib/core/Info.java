package lib.core;

import lib.core.api.ConfigApi;
import lib.core.api.CorePlugin;
import lib.core.api.event.ReloadConfigEvent;
import lib.core.api.event.TimeEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;

public class Info implements Listener {
    //配置

    //未保存的改动数量达到多少时进行保存
    private static int saveDiff = 100;
    //两次更新的最小间隔,单位毫秒
    private static int saveInterval = 1500;
    //两次更新的最大间隔,单位毫秒
    private static int mustSaveInterval = 63000;

    //缓存

    //需要删除的不会保存在其中
    //动态读取
    //玩家名 属性名 信息
    private static HashMap<String, HashMap<String, InfoUser>> infoHash = new HashMap<String, HashMap<String, InfoUser>>();
    //在此hash表内的属性表示是需要更新的(InfoUser内data值为null表示需要删除)
    private static HashMap<InfoUser, Boolean> diffHash = new HashMap<InfoUser, Boolean>();

    //上次更新的时间点
    private static long lastUpdate;

    public Info() {
        //读取配置文件
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTime(TimeEvent e) {
        //没有需要更新的
        if (diffHash.isEmpty()) return;

        //速度太快
        long now = System.currentTimeMillis();
        long past = now-lastUpdate;
        if (past < saveInterval) return;

        //必须更新或改动数量达到
        if (past > mustSaveInterval || diffHash.size() > saveDiff) update();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        String name = e.getPlayer().getName();
        if (!infoHash.containsKey(name)) {
            HashMap<String, InfoUser> hash = new HashMap<String, InfoUser>();
            infoHash.put(name, hash);
            for (InfoUser info : Dao.getInfos(name)) hash.put(info.getFlag(), info);
        }
    }

    public static void onDisable() {
        update();
    }

    /*
     * 获取玩家的属性值
     * @param name 玩家名,不为null
     * @param flag 属性名,不为null
     * @return 属性值,不存在返回null
     */
    public static String getInfo(String name, String flag) {
        return get(name, flag).getData();
    }

    /**
     * 设置玩家的属性信息(玩家不存在此属性信息会新建)
     * @param name 玩家名,不为null
     * @param flag 属性名,不为null
     * @param data 属性值,null表示删除属性信息
     */
    public static void setInfo(String name, String flag, String data) {
        InfoUser info = get(name, flag);
        //一样的
        if (data == null) {
            if (info.getData() == null) return;
        }else {
            if (data.equals(info.getData())) return;
        }
        //设置
        info.setData(data);
        diffHash.put(info, true);
    }

    /**
     * 获取玩家的属性信息(先从缓存中读取,没有再从数据库中读取并保存缓存)
     * @param name 玩家名,不为null
     * @param flag 属性名,不为null
     * @return 不为null(返回的属性信息勿修改,修改请调用set方法)
     */
    private static InfoUser get(String name, String flag) {
        //数据
        HashMap<String, InfoUser> hash = infoHash.get(name);
        if (hash == null) {
            hash = new HashMap<String, InfoUser>();
            infoHash.put(name, hash);
        }

        //先从缓存读取
        InfoUser info = hash.get(flag);
        if (info != null) return info;

        //再从数据库中读取
        info = Dao.getInfo(name, flag);
        if (info == null) info = new InfoUser(name, flag, null);
        //保存缓存
        hash.put(flag, info);

        //返回
        return info;
    }

    /**
     * 更新未保存的信息到数据库
     */
    private static void update() {
        //保存
        Dao.updateInfos(diffHash.keySet());

        //更新缓存
        diffHash.clear();
        lastUpdate = System.currentTimeMillis();
    }

    private static void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        saveDiff = config.getInt("info.saveDiff", 100);
        if (saveDiff < 1) {
            saveDiff = 1;
            ConfigApi.log(CorePlugin.pn, "info.saveDiff < 1");
        }
        saveInterval = config.getInt("info.saveInterval", 1500);
        if (saveInterval < 0) {
            saveInterval = 0;
            ConfigApi.log(CorePlugin.pn, "info.saveInterval < 0");
        }
        mustSaveInterval = config.getInt("info.mustSaveInterval", 63000);
        if (mustSaveInterval < 0) {
            mustSaveInterval = 0;
            ConfigApi.log(CorePlugin.pn, "info.mustSaveInterval < 0");
        }
    }
}
