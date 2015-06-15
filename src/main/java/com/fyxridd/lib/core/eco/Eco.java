package com.fyxridd.lib.core.eco;

import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CoreApi;
import com.fyxridd.lib.core.Dao;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.model.EcoUser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;

public class Eco implements EcoHandler, Listener {

    private static HashMap<String, EcoUser> ecoHash;

    //优化策略,由于玩家经济操作会比较频繁,此举动可以减少数据库的操作
    //玩家名 未保存的改动值(大于0表示增加的改动,小于0表示减少的改动)
    //同时在此hash表内的玩家表示是需要保存的
    private static HashMap<String, Integer> diffHash = new HashMap<String, Integer>();

    //优化策略,限制玩家两次数据库保存的最小间隔
    //玩家名 最近一次保存的时间点
    private static HashMap<String, Long> lastHash = new HashMap<String, Long>();

    //配置
    //玩家钱未保存的改动值大于多少时会检测进行保存
    private static int saveDiff = 10000;
    //玩家两次保存的最小间隔
    private static int saveInterval = 1000;
    //玩家两次保存的最大间隔(检测保存时如果钱有改动并超过此间隔则必然保存)
    private static int mustSaveInterval = 60000;

    public Eco() {
        loadData();
        //读取配置文件
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
    }

    public void onDisable() {
        Dao.addOrUpdateEcoUsers(ecoHash, diffHash.keySet());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        checkInit(e.getPlayer().getName());
    }

    /**
     * 检测初始化玩家的经济信息<br>
     * 如果不存在则会新建EcoUser
     *
     * @param name 玩家名,不为null
     */
    public static void checkInit(String name) {
        if (!ecoHash.containsKey(name)) {//新建
            EcoUser eu = new EcoUser(name, 0);
			ecoHash.put(name, eu);
            Dao.addOrUpdateEcoUser(eu);
        }
    }

    /**
     * 获取用户信息
     * @param name 用户名,不为null
     * @return 信息,不存在返回null
     */
    public static EcoUser getUser(String name) {
        return ecoHash.get(name);
    }

    public double get(Player p) {
        return get(p.getName());
    }

    public double get(String name) {
        EcoUser eu = ecoHash.get(name);
        if (eu == null) return -1;
        return eu.getMoney();
    }

    public boolean set(Player p, int amount) {
        return set(p.getName(), (double) amount);
    }

    public boolean set(String name, int amount) {
        return set(name, (double) amount);
    }

    public boolean set(Player p, double amount) {
        return set(p.getName(), amount);
    }

    public boolean set(String name, double amount) {
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //
        EcoUser eu = ecoHash.get(name);
        if (eu == null) return false;
        if (amount < 0) amount = 0;
        else if (amount > EcoManager.max) amount = EcoManager.max;
        //优化策略
        int diff = (int) amount - (int) eu.getMoney();
        eu.setMoney(amount);
        if (diff == 0) return true;//(强转为整数,略有偏差,不影响)此次的钱改动为0
        if (!diffHash.containsKey(name)) {
            diffHash.put(name, diff);
            checkSave(name);
        } else {
            diff = diffHash.get(name) + diff;//总改动=旧的改动+此次的改动
            if (diff == 0) {//总改动变为0时
                diffHash.remove(name);
            } else {
                diffHash.put(name, diff);
                checkSave(name);
            }
        }
        return true;
    }

    public boolean add(Player p, int amount) {
        return add(p.getName(), (double) amount);
    }

    public boolean add(String name, int amount) {
        return add(name, (double) amount);
    }

    public boolean add(Player p, double amount) {
        return add(p.getName(), amount);
    }

    public boolean add(String name, double amount) {
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //
        if (amount <= 0) return true;
        //
        EcoUser eu = ecoHash.get(name);
        if (eu == null) return false;
        double result = eu.getMoney() + amount;
        if (result < 0 || result > EcoManager.max) result = EcoManager.max;
        return set(name, result);
    }

    public boolean del(Player p, int amount) {
        return del(p.getName(), (double) amount);
    }

    public boolean del(String name, int amount) {
        return del(name, (double) amount);
    }

    public boolean del(Player p, double amount) {
        return del(p.getName(), amount);
    }

    public boolean del(String name, double amount) {
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //
        if (amount <= 0) return true;
        //
        EcoUser eu = ecoHash.get(name);
        if (eu == null) return false;
        double result = eu.getMoney() - amount;
        if (result < 0 || result > EcoManager.max) result = 0;
        return set(name, result);
    }

    /**
     * 检测保存
     */
    private static void checkSave(String name) {
        //玩家没有账户,不需要保存
        EcoUser eu = ecoHash.get(name);
        if (eu == null) return;

        //钱没有改动,不需要保存
        if (!diffHash.containsKey(name)) return;

        //保存速度太快检测
        long now = System.currentTimeMillis();
        long past = Long.MAX_VALUE;
        if (lastHash.containsKey(name)) {
            past = now - lastHash.get(name);
            if (past < saveInterval) return;
        }

        //钱改动过小并且与上次改动间隔未达到必须保存的最大间隔,不需要保存
        if (diffHash.get(name) <= saveDiff && past <= mustSaveInterval) return;

        //成功保存
        Dao.addOrUpdateEcoUser(eu);
        diffHash.remove(name);
        lastHash.put(name, now);
    }

    private static void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        saveDiff = config.getInt("eco.in.saveDiff", 10000);
        if (saveDiff < 0) {
            saveDiff = 0;
            ConfigApi.log(CorePlugin.pn, "eco.in.saveDiff < 0");
        }
        saveInterval = config.getInt("eco.in.saveInterval", 1000);
        if (saveInterval < 0) {
            saveInterval = 0;
            ConfigApi.log(CorePlugin.pn, "eco.in.saveInterval < 0");
        }
        mustSaveInterval = config.getInt("eco.in.mustSaveInterval", 60000);
        if (mustSaveInterval < 0) {
            mustSaveInterval = 0;
            ConfigApi.log(CorePlugin.pn, "eco.in.mustSaveInterval < 0");
        }
    }

    private static void loadData() {
        ecoHash = new HashMap<String, EcoUser>();
        for (EcoUser eu : Dao.getAllEcoUsers()) ecoHash.put(eu.getName(), eu);
    }
}
