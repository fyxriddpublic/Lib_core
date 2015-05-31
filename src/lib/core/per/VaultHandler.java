package lib.core.per;

import lib.core.api.ConfigApi;
import lib.core.api.CorePlugin;
import lib.core.api.event.ReloadConfigEvent;
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

    @Override
    public boolean has(Player p, String per) {
        return permission.has(p, per);
    }

    @Override
    public boolean has(String name, String per) {
        return permission.has(vaultDefaultWorld, name, per);
    }

    @Override
    public boolean add(Player p, String per) {
        return permission.playerAdd(p, per);
    }

    @Override
    public boolean add(String name, String per) {
        return permission.playerAdd(vaultDefaultWorld, name, per);
    }

    @Override
    public boolean del(Player p, String per) {
        return permission.playerRemove(p, per);
    }

    @Override
    public boolean del(String name, String per) {
        return permission.playerRemove(vaultDefaultWorld, name, per);
    }

    /**
     * @see #hasGroup(String, String, boolean)
     */
    @Override
    public boolean hasGroup(Player p, String groupName, boolean loop) {
        return permission.playerInGroup(p, groupName);
    }

    /**
     * @param loop 无效变量
     */
    @Override
    public boolean hasGroup(String name, String groupName, boolean loop) {
        return permission.playerInGroup(vaultDefaultWorld, name, groupName);
    }

    @Override
    public boolean addGroup(Player p, String groupName) {
        return permission.playerAddGroup(p, groupName);
    }

    @Override
    public boolean addGroup(String name, String groupName) {
        return permission.playerAddGroup(vaultDefaultWorld, name, groupName);
    }

    @Override
    public boolean delGroup(Player p, String groupName) {
        return permission.playerRemoveGroup(p, groupName);
    }

    @Override
    public boolean delGroup(String name, String groupName) {
        return permission.playerRemoveGroup(vaultDefaultWorld, name, groupName);
    }

    /**
     * 无效方法,永远返回false
     */
    @Override
    public boolean checkHasGroup(String tar, String groupName) {
        return false;
    }

    private static void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        vaultDefaultWorld = config.getString("per.other.vaultDefaultWorld");
    }
}
