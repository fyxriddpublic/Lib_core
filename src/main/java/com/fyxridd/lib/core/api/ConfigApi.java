package com.fyxridd.lib.core.api;

import com.fyxridd.lib.core.ConfigManager;
import com.fyxridd.lib.core.api.hashList.HashList;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class ConfigApi {
	/**
	 * @see ConfigManager#getDefaultFilter()
	 */
	public static List<String> getDefaultFilter() {
		return ConfigManager.getDefaultFilter();
	}

	/**
	 * @see ConfigManager#register(java.io.File, String, java.util.List, String, com.fyxridd.lib.core.api.hashList.HashList)
	 */
	public static void register(File sourceJarFile,String destPath,List<String> filter,String pluginName, HashList<String> description) {
		ConfigManager.register(sourceJarFile, destPath, filter, pluginName, description);
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
}
