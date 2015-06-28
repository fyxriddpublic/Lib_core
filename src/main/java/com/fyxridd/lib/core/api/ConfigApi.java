package com.fyxridd.lib.core.api;

import com.fyxridd.lib.core.ConfigManager;
import com.fyxridd.lib.core.api.hashList.HashList;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigApi {
    /**
	 * @see ConfigManager#register(java.io.File, String, String, com.fyxridd.lib.core.api.hashList.HashList)
	 */
	public static void register(File sourceJarFile,String destPath, String pluginName, HashList<String> description) {
		ConfigManager.register(sourceJarFile, destPath, pluginName, description);
	}

	/**
	 * @see ConfigManager#loadConfig(String)
	 */
	public static boolean loadConfig(String pluginName) {
		return ConfigManager.loadConfig(pluginName);
	}
	
	/**
	 * @see ConfigManager#getConfig(String)
	 */
	public static YamlConfiguration getConfig(String pluginName) {
		return ConfigManager.getConfig(pluginName);
	}

    /**
     * @see ConfigManager#log(String, String)
     */
    public static void log(String plugin, String msg) {
        ConfigManager.log(plugin, msg);
    }

    /**
     * @see ConfigManager#setDescription(String, com.fyxridd.lib.core.api.hashList.HashList)
     */
    public static void setDescription(String pluginName, HashList<String> description) {
        ConfigManager.setDescription(pluginName, description);
    }

    /**
     * 会将jar内的resources目录内的所有文件放到插件对应的数据文件夹下
     * @param sourceJarFile jar文件,不为null
     * @param pluginName 插件名
     * @return 出现异常返回false
     */
    public static boolean generateFiles(File sourceJarFile, String pluginName){
        return ConfigManager.generateFiles(sourceJarFile, pluginName);
    }
}
