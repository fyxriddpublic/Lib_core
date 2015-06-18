package com.fyxridd.lib.core.api;

import com.fyxridd.lib.core.ConfigManager;
import com.fyxridd.lib.core.eco.EcoManager;
import com.fyxridd.lib.core.CoreMain;
import com.fyxridd.lib.core.Dao;
import com.fyxridd.lib.core.Info;
import com.fyxridd.lib.core.per.PerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.hibernate.SessionFactory;

import java.io.File;
import java.util.List;

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
        //生成文件
        List<String> filter = ConfigManager.getDefaultFilter();
        filter.add("names.yml");
        filter.add("EcoUser.hbm.xml");
        filter.add("InfoUser.hbm.xml");
        filter.add("User.hbm.xml");

        filter.add("per/group/admin.yml");
        filter.add("per/group/default.yml");
        filter.add("per/group/formal.yml");
        filter.add("per/user/default.yml");

        filter.add("show/ConfigManager.yml");
        filter.add("show/ConfigManager_description.yml");
        filter.add("show/Description.yml");
        filter.add("show/xxx_description.yml");

        CoreApi.generateFiles(getFile(), getDataFolder().getAbsolutePath(), filter);

        //注册hbm
        registerHbm(new File(getDataFolder(), "EcoUser.hbm.xml"));
        registerHbm(new File(getDataFolder(), "InfoUser.hbm.xml"));
        registerHbm(new File(getDataFolder(), "User.hbm.xml"));
    }

    //启动插件
    @Override
    public void onEnable() {
        CoreApi.serverPath = System.getProperty("user.dir");
        CoreApi.pluginPath = getFile().getParentFile().getAbsolutePath();
        CoreApi.serverVer = CoreApi.getMcVersion(Bukkit.getServer());

        instance = this;
        pn = getName();
        file = getFile();
        dataPath = CoreApi.pluginPath+ File.separator+pn;
        ver = CoreApi.getPluginVersion(getFile());

        new CoreMain();

        //成功启动
        CoreApi.sendConsoleMessage(FormatApi.get(pn, 25, pn, ver).getText());
    }

    //停止插件
    @Override
    public void onDisable() {
        //Info
        Info.onDisable();
        //Eco
        EcoManager.onDisable();
        //Per
        PerManager.onDisable();
        //ConfigManager
        ConfigManager.onDisable();
        //计时器
        Bukkit.getScheduler().cancelAllTasks();
        //数据库连接关闭
        Dao.close();
        //显示插件成功停止信息
        CoreApi.sendConsoleMessage(FormatApi.get(pn, 30, pn, ver).getText());
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

}
