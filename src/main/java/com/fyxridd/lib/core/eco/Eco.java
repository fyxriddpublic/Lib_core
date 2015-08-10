package com.fyxridd.lib.core.eco;

import com.fyxridd.lib.core.CoreMain;
import com.fyxridd.lib.core.api.CoreApi;
import com.fyxridd.lib.core.Dao;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.model.EcoUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.HashSet;

public class Eco implements EcoHandler, Listener {

    //缓存
    //全部读取
    private HashMap<String, EcoUser> ecoHash;

    //优化策略
    //需要保存的玩家列表
    private HashSet<EcoUser> needUpdateList = new HashSet<>();

    public Eco() {
        //读取数据
        loadData();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //计时器: 保存
        Bukkit.getScheduler().scheduleSyncRepeatingTask(CorePlugin.instance, new Runnable() {
            @Override
            public void run() {
                saveAll();
            }
        }, 326, 326);
    }

    public void onDisable() {
        saveAll();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        String name = e.getPlayer().getName();
        if (!ecoHash.containsKey(name)) {//新建
            EcoUser eu = new EcoUser(name, 0);
            ecoHash.put(name, eu);
            needUpdateList.add(eu);
        }
    }

    /**
     * 获取用户信息
     * @param name 用户名,不为null
     * @return 信息,不存在返回null
     */
    public EcoUser getUser(String name) {
        return ecoHash.get(name);
    }

    public double get(Player p) {
        if (p == null) return -1;

        return get(p.getName());
    }

    public double get(String name) {
        if (name == null) return -1;

        EcoUser eu = ecoHash.get(name);
        if (eu == null) return -1;
        return eu.getMoney();
    }

    public boolean set(Player p, int amount) {
        if (p == null) return false;

        return set(p.getName(), (double) amount);
    }

    public boolean set(String name, int amount) {
        if (name == null) return false;

        return set(name, (double) amount);
    }

    public boolean set(Player p, double amount) {
        if (p == null) return false;

        return set(p.getName(), amount);
    }

    public boolean set(String name, double amount) {
        if (name == null) return false;
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //
        EcoUser eu = ecoHash.get(name);
        if (eu == null) return false;
        if (amount < 0) amount = 0;
        else if (amount > EcoManager.max) amount = EcoManager.max;
        eu.setMoney(amount);
        //添加更新
        needUpdateList.add(eu);
        return true;
    }

    public boolean add(Player p, int amount) {
        if (p == null) return false;

        return add(p.getName(), (double) amount);
    }

    public boolean add(String name, int amount) {
        if (name == null) return false;

        return add(name, (double) amount);
    }

    public boolean add(Player p, double amount) {
        if (p == null) return false;

        return add(p.getName(), amount);
    }

    public boolean add(String name, double amount) {
        if (name == null) return false;
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
        if (p == null) return false;

        return del(p.getName(), (double) amount);
    }

    public boolean del(String name, int amount) {
        if (name == null) return false;

        return del(name, (double) amount);
    }

    public boolean del(Player p, double amount) {
        if (p == null) return false;

        return del(p.getName(), amount);
    }

    public boolean del(String name, double amount) {
        if (name == null) return false;
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

    private void saveAll() {
        if (!needUpdateList.isEmpty()) {
            CoreMain.dao.saveOrUpdates(needUpdateList);
            needUpdateList.clear();
        }
    }

    private void loadData() {
        ecoHash = new HashMap<>();
        for (EcoUser eu : Dao.getAllEcoUsers()) ecoHash.put(eu.getName(), eu);
    }
}
