package com.fyxridd.lib.core.per;

import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHandler implements PerHandler, Listener {
    private static Permission permission = null;

    //配置

    private static String vaultDefaultWorld;

    public VaultHandler() {
        //初始化权限
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(Permission.class);
        try {
            permission = permissionProvider.getProvider();
        } catch (Exception e) {
            permission = null;
        }
        //读取配置
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

    public boolean has(Player p, String per) {
        if (p == null) return false;
        if (per == null || per.isEmpty()) return true;

        return permission.has(p, per);
    }

    public boolean has(String name, String per) {
        if (name == null) return false;
        if (per == null || per.isEmpty()) return true;

        return permission.has(vaultDefaultWorld, name, per);
    }

    public boolean add(Player p, String per) {
        if (p == null || per == null || per.isEmpty()) return false;

        return permission.playerAdd(p, per);
    }

    public boolean add(String name, String per) {
        if (name == null || per == null || per.isEmpty()) return false;

        return permission.playerAdd(vaultDefaultWorld, name, per);
    }

    public boolean del(Player p, String per) {
        if (p == null || per == null) return false;

        return permission.playerRemove(p, per);
    }

    public boolean del(String name, String per) {
        if (name == null || per == null) return false;

        return permission.playerRemove(vaultDefaultWorld, name, per);
    }

    /**
     * @see #hasGroup(String, String, boolean)
     */
    public boolean hasGroup(Player p, String groupName, boolean loop) {
        if (p == null || groupName == null) return false;

        return permission.playerInGroup(p, groupName);
    }

    /**
     * @param loop 无效变量
     */
    public boolean hasGroup(String name, String groupName, boolean loop) {
        if (name == null || groupName == null) return false;

        return permission.playerInGroup(vaultDefaultWorld, name, groupName);
    }

    public boolean addGroup(Player p, String groupName) {
        if (p == null || groupName == null) return false;

        return permission.playerAddGroup(p, groupName);
    }

    public boolean addGroup(String name, String groupName) {
        if (name == null || groupName == null) return false;

        return permission.playerAddGroup(vaultDefaultWorld, name, groupName);
    }

    public boolean delGroup(Player p, String groupName) {
        if (p == null || groupName == null) return false;

        return permission.playerRemoveGroup(p, groupName);
    }

    public boolean delGroup(String name, String groupName) {
        if (name == null || groupName == null) return false;

        return permission.playerRemoveGroup(vaultDefaultWorld, name, groupName);
    }

    /**
     * 无效方法,永远返回false
     */
    public boolean checkHasGroup(String tar, String groupName) {
        return false;
    }

    /**
     * 无效方法,永远返回false
     */
    @Override
    public boolean createGroup(String group) {
        return false;
    }

    /**
     * 无效方法,永远返回false
     */
    @Override
    public boolean delGroup(String group) {
        return false;
    }

    @Override
    public boolean groupAddPer(String group, String per) {
        if (group == null || per == null) return false;

        return permission.groupAdd(vaultDefaultWorld, group, per);
    }

    @Override
    public boolean groupRemovePer(String group, String per) {
        if (group == null || per == null) return false;

        return permission.groupRemove(vaultDefaultWorld, group, per);
    }

    /**
     * 无效方法,永远返回false
     */
    @Override
    public boolean groupAddInherit(String group, String inherit) {
        return false;
    }

    /**
     * 无效方法,永远返回false
     */
    @Override
    public boolean groupRemoveInherit(String group, String inherit) {
        return false;
    }

    private static void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        vaultDefaultWorld = config.getString("per.other.vaultDefaultWorld");
    }
}
