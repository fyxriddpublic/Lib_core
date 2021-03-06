package com.fyxridd.lib.core;

import com.fyxridd.lib.core.api.*;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.hashList.HashListImpl;
import com.fyxridd.lib.core.api.inter.*;
import com.fyxridd.lib.core.api.hashList.HashList;
import com.fyxridd.lib.core.show.ShowManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

public class ConfigManager implements FunctionInterface, Listener, ShowInterface {
    /**
     * 配置上下文
     */
	private static class ConfigContext {
		File sourceJarFile;
		String destPath;//存放配置文件的路径
		String pluginName;
        HashList<String> description;

		public ConfigContext(File sourceJarFile, String destPath, String pluginName) {
			this.sourceJarFile = sourceJarFile;
			this.destPath = destPath;
			this.pluginName = pluginName;
		}

        public String getDescription() {
            String result = "";
            boolean first = true;
            for (String s:description) {
                if (first) first = false;
                else result += "\n";
                result += s;
            }
            return result;
        }
    }

    private static final String FUNC_NAME = "ConfigManager";
    private static final String PAGE_NAME = "ConfigManager";
    //日志文件夹路径
    private static String logPath;
    //日志文件
    private static File logFile;
    //文件输出流
    private static FileOutputStream fos;
    //日志
    private static PrintStream log;

    //默认过滤器
    private static Pattern filter = Pattern.compile("resources/[\\S]+");

	//插件名 配置上下文
	private static HashMap<String, ConfigContext> contextHash = new HashMap<>();

    //插件名 读取并保存在内存中的配置
	private static HashMap<String, YamlConfiguration> configHash = new HashMap<>();

    //功能管理权限
    private static String adminPer;
    //日志前缀格式
    private static String logPrefix;
    //日志时间格式
    private static SimpleDateFormat sdf;

    public ConfigManager() {
        //初始化
        logPath = CorePlugin.dataPath+File.separator+"log"+File.separator+"Config";
        //日志文件
        String name = CoreApi.getLogDateTime();
        logFile = new File(logPath+File.separator+name+".txt");
        logFile.getParentFile().mkdirs();
        try {
            logFile.createNewFile();
        } catch (IOException e) {
            //do nothing
        }
        try {
            fos = new FileOutputStream(logFile);
            log = new PrintStream(fos);
        } catch (FileNotFoundException e) {
            if (fos != null) try {
                fos.close();
            } catch (IOException e1) {
                //do nothing
            }
            if (log != null) log.close();
            fos = null;
            log = null;
        }
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //注册功能
        FuncManager.register(this);
        //注册界面
        ShowManager.register(CorePlugin.pn);
    }

    /**
     * 读取插件描述信息<br>
     *     会尝试从配置项description中读取
     */
    @EventHandler(priority= EventPriority.LOWEST)
    public void onReloadConfigLowest(ReloadConfigEvent e) {
        YamlConfiguration config = getConfig(e.getPlugin());
        if (config == null) return;

        List<String> list = config.getStringList("description");
        if (list == null) return;

        HashList<String> description = new HashListImpl<>();
        for (String s:list) description.add(CoreApi.convert(s));

        ConfigContext configContext = contextHash.get(e.getPlugin());
        if (configContext != null) configContext.description = description;
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) {
            loadConfig();
            //重新注册界面
            ShowManager.register(CorePlugin.pn);
        }
    }

    public static void onDisable() {
        log.flush();
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e1) {
                //do nothing
            }
        }
        if (log != null) log.close();
    }

    /**
     * 记录配置读取异常信息
     * @param plugin 插件名,不为null
     * @param msg 异常信息,不为null
     */
    public static void log(String plugin, String msg) {
        try {
            if (log != null) {
                String result = logPrefix.replace("{1}", sdf.format(new Date())).replace("{2}", plugin) + msg + "\n";
                log.append(result);
                log.flush();
            }
        } catch (Exception e) {
            //do nothing
        }
    }

	/**
	 * 注册配置,同时生成缺少的文件<br>
	 * 注意: 第一次注册后需要调用一次loadConfig(pluginName)否则不会读取配置
	 * @param sourceJarFile 配置文件所在的jar文件
	 * @param destPath 放置配置文件的目标文件夹路径
	 * @param pluginName 注册的插件名
     * @return 注册是否成功.如果插件名已经被注册则返回false
	 */
	public static boolean register(File sourceJarFile, String destPath, String pluginName) {
        if (contextHash.containsKey(pluginName)) return false;
		ConfigContext configItem = new ConfigContext(sourceJarFile, destPath, pluginName);
		contextHash.put(pluginName, configItem);
		generateFiles(sourceJarFile, pluginName);
        return true;
	}

	/**
	 * 1. 生成,根据配置上下文检测生成缺失文件<br/>
     * 2. 读取,读取配置文件"config.yml",读取成功则保存到内存中替换原来的,失败直接返回不替换<br/>
     * 3. 事件,读取完成后发出重新读取配置事件
	 * @param pluginName 插件名,不为null
	 * @return 成功返回 true,否则返回false
	 */
	public static boolean loadConfig(String pluginName)  {
        //生成
		ConfigContext configContext = contextHash.get(pluginName);
		if (configContext == null) return false;
        generateFiles(configContext.sourceJarFile, pluginName);
        //读取
		YamlConfiguration config = CoreApi.loadConfigByUTF8(new File(configContext.destPath+File.separator+"config.yml"));
		if (config == null) return false;
        configHash.put(pluginName, config);
        //事件
		ReloadConfigEvent e = new ReloadConfigEvent(pluginName);
		Bukkit.getPluginManager().callEvent(e);
		return true;
	}
	
	/**
	 * 获取插件对应的配置文件<br>
	 * 重新读取配置文件loadConfig(pluginName)成功后会改变
	 * @param pluginName 插件名,不为null
	 * @return 对应的读取到内存中的配置文件,不存在返回null
	 */
	public static YamlConfiguration getConfig(String pluginName) {
		return configHash.get(pluginName);
	}

    /**
     * @see com.fyxridd.lib.core.api.ConfigApi#generateFiles(java.io.File, String)
     */
    public static boolean generateFiles(File sourceJarFile, String pluginName){
        JarInputStream jis = null;
        FileOutputStream fos = null;
        try {
            jis = new JarInputStream(new FileInputStream(sourceJarFile));
            String destPath = CoreApi.pluginPath+File.separator+pluginName;
            new File(destPath).mkdirs();
            JarEntry entry;
            byte[] buff = new byte[1024];
            int read;
            while ((entry = jis.getNextJarEntry()) != null) {
                String fileName = entry.getName();
                if (filter.matcher(fileName).matches()) {
                    File file = new File(destPath+File.separator+fileName.substring("resources/".length()));
                    if (!file.exists()) {
                        if (entry.isDirectory()) file.mkdirs();
                        else {
                            file.getParentFile().mkdirs();
                            file.createNewFile();
                            fos = new FileOutputStream(file);
                            while((read = jis.read(buff)) > 0) fos.write(buff, 0, read);
                            fos.close();
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            try {
                if (jis != null) jis.close();
            } catch (IOException e) {
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public String getName() {
        return FUNC_NAME;
    }

    @Override
    public boolean isOn(String name, String subFunc) {
        return PerApi.has(name, adminPer);
    }

    /**
     * 'a' 打开注册的插件列表页面<br>
     * 's 插件名' 重新读取指定插件的配置<br>
     */
    @Override
    public void onOperate(Player p, String... args) {
        //权限检测
        if (!PerApi.checkPer(p, adminPer)) return;

        try {
            FancyMessage msg;

            switch (args.length) {
                case 1:
                    if (args[0].equalsIgnoreCase("a")) {
                        Collection<ConfigContext> collection = contextHash.values();
                        ShowList showList = ShowApi.getShowList(2, collection);
                        ShowManager.show(this, args, p, CorePlugin.pn, PAGE_NAME, showList, null, null, null);
                        return;
                    }
                    break;
                case 2:
                    String pluginName = args[1];
                    if (args[0].equalsIgnoreCase("s")) {//重新读取指定插件的配置
                        if (loadConfig(pluginName)) msg = get(400);
                        else msg = get(405);
                        ShowApi.tip(p, msg, true);
                        return;
                    }
                    break;
            }
        } catch (Exception e) {//操作异常
            ShowApi.tip(p, get(410), true);
            return;
        }
        //输入格式错误
        ShowApi.tip(p, get(5), true);
    }

    @Override
    public void show(PlayerContext pc) {
        if (pc.plugin.equals(CorePlugin.pn)) {
            if (pc.pageName.equals(PAGE_NAME)) {
                ShowApi.show(pc.callback, pc.obj, pc.p, pc.plugin, pc.pageName,
                        pc.list, pc.data,pc.pageNow, pc.listNow, pc.front, pc.behind);
            }
        }
    }

    private void loadConfig() {
        YamlConfiguration config = getConfig(CorePlugin.pn);

        //adminPer
        adminPer = config.getString("config.adminPer");

        //logPrefix
        logPrefix = config.getString("config.log.prefix");
        if (logPrefix == null) {
            logPrefix = "";
            log(CorePlugin.pn, "config.log.prefix is null");
        }

        //sdf
        try {
            String temp = config.getString("config.log.dateFormat");
            if (temp == null) {
                temp = "yyyy-MM-dd HH:mm:ss";
                log(CorePlugin.pn, "config.log.dateFormat is null");
            }
            sdf = new SimpleDateFormat(temp);
        } catch (Exception e) {
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log(CorePlugin.pn, "config.log.dateFormat error");
        }
    }

    private FancyMessage get(int id) {
        return FormatApi.get(CorePlugin.pn, id);
    }

    private FancyMessage get(int id, Object... args) {
        return FormatApi.get(CorePlugin.pn, id, args);
    }
}
