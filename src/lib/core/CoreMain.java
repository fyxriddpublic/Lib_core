package lib.core;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lib.core.api.ConfigApi;
import lib.core.api.CorePlugin;
import lib.core.api.FormatApi;
import lib.core.api.event.PlayerChatEvent;
import lib.core.api.event.ReloadConfigEvent;
import lib.core.eco.EcoManager;
import lib.core.api.hashList.HashList;
import lib.core.api.inter.FancyMessage;
import lib.core.per.PerManager;
import lib.core.show.ShowManager;
import lib.core.transaction.TipTransactionManager;
import lib.core.transaction.TransactionManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoreMain implements Listener{
    public static boolean vaultHook;

    private static final Lock lock = new ReentrantLock();

    //database
    public static Dao dao;

    //other
    public static FormatManager formatManager;
    public static FuncManager funcManager;
    public static ShowManager showManager;
    public static ConfigManager configManager;
    public static PerManager perManager;
    public static EcoManager ecoManager;
    public static TransactionManager transactionManager;
    public static TipTransactionManager tipTransactionManager;
    public static Time time;
    public static Tps tps;
    public static Speed speed;
    public static Names names;
    public static RealDamage realDamage;
    public static InputManager inputManager;
    public static RealName realName;
    public static Info info;

    //配置文件内容
    public static HashList<String> description;
    public static String lib_core_admin;
    public static boolean debug;

    //缓存
    private static List<PlayerChatEvent> chatEventList = new ArrayList<PlayerChatEvent>();

    //启动插件
    public CoreMain() {
       //前置检测
        try {
            Class.forName("net.milkbowl.vault.Vault");
            vaultHook = true;
        } catch (ClassNotFoundException e) {
            vaultHook = false;
        }

        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        FancyMessageImpl.init(pm);
        //manager
        formatManager = new FormatManager();
        funcManager = new FuncManager();
        showManager = new ShowManager();
        //config
        initConfig();
        //database
        dao = new Dao();
        //other
        perManager = new PerManager();
        ecoManager = new EcoManager();
        transactionManager = new TransactionManager();
        tipTransactionManager = new TipTransactionManager();
        time = new Time();
        tps = new Tps();
        speed = new Speed();
        names = new Names();
        realDamage = new RealDamage();
        inputManager = new InputManager();
        realName = new RealName();
        info = new Info();
        //读取配置文件
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //命令
        CorePlugin.instance.getCommand("f").setExecutor(funcManager);
        CorePlugin.instance.getCommand("s").setExecutor(inputManager);
        //同步检测聊天事件
        Bukkit.getScheduler().scheduleSyncRepeatingTask(CorePlugin.instance, new Runnable() {
            @Override
            public void run() {
                checkChat();
            }
        }, 1, 1);
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        try {
            if (!lock.tryLock(3, TimeUnit.SECONDS)) throw new InterruptedException();

            try {
                PlayerChatEvent event = new PlayerChatEvent(e.getPlayer(), e.getMessage());
                chatEventList.add(event);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    private void initConfig() {
        List<String> filter = ConfigManager.getDefaultFilter();
        filter.add("names.yml");

        filter.add("per/group/admin.yml");
        filter.add("per/group/default.yml");
        filter.add("per/group/formal.yml");
        filter.add("per/user/default.yml");

        filter.add("show/ConfigManager.yml");
        filter.add("show/ConfigManager_description.yml");
        filter.add("show/Description.yml");
        filter.add("show/xxx_description.yml");

        ConfigManager.register(CorePlugin.file, CorePlugin.dataPath, filter, CorePlugin.pn, null);

        configManager = new ConfigManager();

        ConfigApi.loadConfig(CorePlugin.pn);
    }

    private void checkChat() {
        if (lock.tryLock()) {
            try{
                //发出事件
                for (PlayerChatEvent event:chatEventList) {
                    if (!event.getP().isOnline()) continue;

                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        String msg = get(35, event.getP().getName(), event.getMsg()).getText();
                        for (Player p:Bukkit.getOnlinePlayers()) p.sendMessage(msg);
                    }
                }

                //清空事件列表
                chatEventList.clear();
            }finally {
                lock.unlock();
            }
        }
    }

    private void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        //权限
        lib_core_admin = config.getString("lib_core_admin");

        //debug
        debug = config.getBoolean("debug");
    }

    private FancyMessage get(int id, Object... args) {
        return FormatApi.get(CorePlugin.pn, id, args);
    }
}
