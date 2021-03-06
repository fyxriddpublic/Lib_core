package com.fyxridd.lib.core;

import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.event.EnterBlockTypeEvent;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class EnterBlockTypeManager implements Listener {
    private class Check implements Runnable {
        @Override
        public void run() {
            for (Player p:Bukkit.getOnlinePlayers()) {
                check(p, false);
            }

            //下个计时
            Bukkit.getScheduler().scheduleSyncDelayedTask(CorePlugin.instance, check, interval);
        }
    }

    //配置
    private long interval;

    //缓存
    private HashMap<Player, Material> inTypeHash = new HashMap<>();//进入方块类型
    private HashMap<Player, Material> onTypeHash = new HashMap<>();//走上方块类型

    private Check check = new Check();

    public EnterBlockTypeManager() {
        //读取配置文件
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //计时器
        Bukkit.getScheduler().scheduleSyncDelayedTask(CorePlugin.instance, check, interval);
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent e) {
        check(e.getPlayer(), true);
        inTypeHash.remove(e.getPlayer());
        onTypeHash.remove(e.getPlayer());
    }

    /**
     * 检测
     * @param p 玩家
     * @param exit 玩家是否退服
     */
    private void check(Player p, boolean exit) {
        {
            Material oldType = inTypeHash.get(p);
            Material newType = exit?null:p.getLocation().getBlock().getType();
            boolean change = false;
            if (newType != null) change = oldType == null || !oldType.equals(newType);
            else if (oldType != null) change = true;
            if (change) {
                inTypeHash.put(p, newType);
                EnterBlockTypeEvent event = new EnterBlockTypeEvent(p, oldType, newType, true);
                Bukkit.getPluginManager().callEvent(event);
            }
        }

        {
            Material oldType = onTypeHash.get(p);
            Material newType = exit?null:p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
            boolean change = false;
            if (newType != null) change = oldType == null || !oldType.equals(newType);
            else if (oldType != null) change = true;
            if (change) {
                onTypeHash.put(p, newType);
                EnterBlockTypeEvent event = new EnterBlockTypeEvent(p, oldType, newType, false);
                Bukkit.getPluginManager().callEvent(event);
            }
        }
    }

    private void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        interval = config.getLong("enterBlockType.interval");
        if (interval < 1) {
            interval = 1;
            ConfigApi.log(CorePlugin.pn, "enterBlockType.interval < 1");
        }
    }
}
