package lib.core.api;

import lib.core.ConfigManager;
import lib.core.CoreMain;
import lib.core.Dao;
import lib.core.Info;
import lib.core.eco.EcoManager;
import lib.core.per.PerManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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
}
