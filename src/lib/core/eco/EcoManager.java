package lib.core.eco;

import lib.core.CoreMain;
import lib.core.api.ConfigApi;
import lib.core.api.CorePlugin;
import lib.core.api.event.ReloadConfigEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class EcoManager implements Listener {
    //自带实现(完整)
    private static Eco eco;
    //Vault实现(不完整)
    private static VaultHandler vaultHandler;

    //配置

    //钱上限
    public static int max = 10000000;
    //优先级
    private static HashMap<String, Integer> priorityHash;

    //缓存

    //处理器名 权限处理器
    private static HashMap<String, EcoHandler> handlerHash = new HashMap<String, EcoHandler>();

    //当前使用的权限处理器
    private static int nowPriority;
    public static EcoHandler ecoHandler;

    public EcoManager() {
        eco = new Eco();
        if (CoreMain.vaultHook) vaultHandler = new VaultHandler();

        //读取配置
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);

        //添加默认的经济处理器
        register("eco", eco);
        if (CoreMain.vaultHook) register("vault", vaultHandler);
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

    public static void onDisable() {
        eco.onDisable();
    }

    public static void register(String name, EcoHandler ecoHandler) {
        handlerHash.put(name, ecoHandler);
    }

    /**
     * (根据优先级)更新经济处理器
     */
    private static void update() {
        for (Map.Entry<String, EcoHandler> entry:handlerHash.entrySet()) {
            Integer priority = priorityHash.get(entry.getKey());
            if (priority == null) priority = 0;
            if (ecoHandler == null || priority > nowPriority) {
                nowPriority = priority;
                ecoHandler = entry.getValue();
            }
        }
    }

    private static void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        //钱上限
        max = config.getInt("eco.max");
        if (max < 0) {
            max = 0;
            ConfigApi.log(CorePlugin.pn, "eco.max < 0");
        }
        //读取优先级
        priorityHash = new HashMap<String, Integer>();
        for (String s:config.getStringList("eco.priority")) {
            String[] ss = s.split(" ");
            String name = ss[0];
            int priority = Integer.parseInt(ss[1]);

            priorityHash.put(name, priority);
        }

        //更新
        update();
    }
}
