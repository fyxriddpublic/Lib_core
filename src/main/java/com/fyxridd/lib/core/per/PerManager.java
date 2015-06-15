package com.fyxridd.lib.core.per;

import com.fyxridd.lib.core.CoreMain;
import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class PerManager implements Listener {
    //自带实现(完整)
    private static Per per;
    //Vault实现(不完整)
    private static VaultHandler vaultHandler;

    //配置

    private static HashMap<String, Integer> priorityHash;

    //缓存

    //处理器名 权限处理器
    private static HashMap<String, PerHandler> handlerHash = new HashMap<String, PerHandler>();

    //当前使用的权限处理器
    private static int nowPriority;
    public static PerHandler perHandler;

    public PerManager() {
        per = new Per();
        if (CoreMain.vaultHook) vaultHandler = new VaultHandler();

        //添加默认的权限处理器
        register("per", per);
        if (CoreMain.vaultHook) register("vault", vaultHandler);

        //读取配置
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

    public static void onDisable() {
        per.onDisable();
    }

    public static void register(String name, PerHandler perHandler) {
        handlerHash.put(name, perHandler);
    }

    /**
     * (根据优先级)更新权限处理器
     */
    private static void update() {
        for (Map.Entry<String, PerHandler> entry:handlerHash.entrySet()) {
            Integer priority = priorityHash.get(entry.getKey());
            if (priority == null) priority = 0;
            if (perHandler == null || priority > nowPriority) {
                nowPriority = priority;
                perHandler = entry.getValue();
            }
        }
    }

    private static void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        //读取优先级
        priorityHash = new HashMap<String, Integer>();
        for (String s:config.getStringList("per.priority")) {
            String[] ss = s.split(" ");
            String name = ss[0];
            int priority = Integer.parseInt(ss[1]);

            priorityHash.put(name, priority);
        }

        //更新
        update();
    }
}
