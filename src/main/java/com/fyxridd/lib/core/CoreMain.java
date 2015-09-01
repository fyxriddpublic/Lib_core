package com.fyxridd.lib.core;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.hashList.HashList;
import com.fyxridd.lib.core.eco.EcoManager;
import com.fyxridd.lib.core.per.CmdManager;
import com.fyxridd.lib.core.show.ShowManager;
import com.fyxridd.lib.core.transaction.TransactionManager;
import com.fyxridd.lib.core.per.PerManager;
import com.fyxridd.lib.core.transaction.TipTransactionManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CoreMain implements Listener{
    public static boolean vaultHook, libMsgHook;

    //database
    public static Dao dao;

    //other
    public static FormatManager formatManager;
    public static FuncManager funcManager;
    public static ShowManager showManager;
    public static ConfigManager configManager;
    public static PerManager perManager;
    public static CmdManager cmdManager;
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
    public static EnterBlockTypeManager enterBlockTypeManager;
    public static ChatManager chatManager;
    public static Title title;

    //配置文件内容
    public static HashList<String> description;
    public static String lib_core_admin;
    public static boolean debug;

    //启动插件
    public CoreMain() {
       //前置检测
        try {
            Class.forName("net.milkbowl.vault.Vault");
            vaultHook = true;
        } catch (Exception e) {
            vaultHook = false;
        }

        try {
            Class.forName("com.fyxridd.lib.msg.api.MsgApi");
            libMsgHook = true;
        }catch (Exception e) {
            libMsgHook = false;
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
        cmdManager = new CmdManager();
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
        enterBlockTypeManager = new EnterBlockTypeManager();
        chatManager = new ChatManager();
        title = new Title();
        //读取配置文件
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //命令
        CorePlugin.instance.getCommand("f").setExecutor(funcManager);
        CorePlugin.instance.getCommand("s").setExecutor(inputManager);
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

    private void initConfig() {
        ConfigManager.register(CorePlugin.file, CorePlugin.dataPath, CorePlugin.pn, null);

        configManager = new ConfigManager();

        ConfigApi.loadConfig(CorePlugin.pn);
    }

    private void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        //权限
        lib_core_admin = config.getString("lib_core_admin");

        //debug
        debug = config.getBoolean("debug");
    }
}
