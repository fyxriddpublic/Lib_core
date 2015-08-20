package com.fyxridd.lib.core.show;

import com.fyxridd.lib.core.api.inter.ShowListHandler;

import java.util.HashMap;

/**
 * 列表获取器
 */
public class ShowListManager {
    //插件名 列表名 列表获取器
    private static HashMap<String, HashMap<String, ShowListHandler>> handleHash = new HashMap<>();

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#register(String, String, com.fyxridd.lib.core.api.inter.ShowListHandler)
     */
    public static void register(String plugin, String key, ShowListHandler showListHandler) {
        if (!handleHash.containsKey(plugin)) handleHash.put(plugin, new HashMap<String, ShowListHandler>());
        handleHash.get(plugin).put(key, showListHandler);
    }

    /**
     * 获取显示列表
     * @param plugin 插件名
     * @param key 列表名
     * @return 显示列表,异常返回null
     */
    public static com.fyxridd.lib.core.api.inter.ShowList getShowList(String name, String plugin, String key) {
        try {
            return handleHash.get(plugin).get(key).handle(name);
        } catch (Exception e) {
            return null;
        }
    }
}
