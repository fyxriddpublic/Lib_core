package com.fyxridd.lib.core.api;

import com.fyxridd.lib.core.*;
import com.fyxridd.lib.core.api.event.ServerCloseEvent;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.SessionFactory;

import java.io.File;

public class CorePlugin extends JavaPlugin{
    public static CorePlugin instance;

    //插件名
    public static String pn;
    //插件jar文件
    public static File file;
    //插件数据文件夹路径
    public static String dataPath;
    //插件版本
    public static String ver;

    @Override
    public void onLoad() {
        CoreApi.serverPath = System.getProperty("user.dir");
        CoreApi.pluginPath = getFile().getParentFile().getAbsolutePath();
        CoreApi.serverVer = CoreApi.getMcVersion(Bukkit.getServer());

        instance = this;
        pn = getName();
        file = getFile();
        dataPath = CoreApi.pluginPath + File.separator+pn;
        ver = CoreApi.getPluginVersion(getFile());

        //生成文件
        ConfigApi.generateFiles(getFile(), pn);

        //注册hbm
        registerHbm(new File(getDataFolder(), "EcoUser.hbm.xml"));
        registerHbm(new File(getDataFolder(), "InfoUser.hbm.xml"));
        registerHbm(new File(getDataFolder(), "User.hbm.xml"));
        registerHbm(new File(getDataFolder(), "PerGroup.hbm.xml"));
        registerHbm(new File(getDataFolder(), "PerUser.hbm.xml"));
    }

    //启动插件
    @Override
    public void onEnable() {
        new CoreMain();

        //成功启动
        CoreApi.sendConsoleMessage(FormatApi.get(pn, 25, pn, ver).getText());
    }

    //停止插件
    @Override
    public void onDisable() {
        //Info
        CoreMain.info.onDisable();
        //Eco
        CoreMain.ecoManager.onDisable();
        //Per
        CoreMain.perManager.onDisable();
        //ConfigManager
        ConfigManager.onDisable();
        //计时器
        Bukkit.getScheduler().cancelAllTasks();
        //数据库连接关闭
        Dao.close();
        //显示插件成功停止信息
        CoreApi.sendConsoleMessage(FormatApi.get(pn, 30, pn, ver).getText());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("stop")) {
            if (sender instanceof Player && !sender.isOp()) return true;
            //先T人
            for (Player p:Bukkit.getOnlinePlayers()) p.kickPlayer(get(70).getText());
            //发出关服事件
            Bukkit.getPluginManager().callEvent(new ServerCloseEvent());
            //再关服
            Bukkit.shutdown();
        }
        return true;
    }

    /**
     * 注册hbm<br>
     * 所有使用hibernate的插件都必须使用此方法注册hbm文件
     * @param hbm hbm文件
     */
    public static void registerHbm(File hbm) {
        Dao.registerHbm(hbm);
    }

    /**
     * 获取SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        return Dao.getSessionFactory();
    }

    private static FancyMessage get(int id, Object... args) {
        return FormatApi.get(CorePlugin.pn, id, args);
    }
}
