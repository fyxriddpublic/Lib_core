package lib.core.show;

import lib.core.api.inter.MapHandler;

import java.util.HashMap;

/**
 * 键值获取器
 */
public class ShowMap {
    //插件名 键名 键值获取器
    private static HashMap<String, HashMap<String, MapHandler>> handleHash = new HashMap<String, HashMap<String, MapHandler>>();

    public static void register(String plugin, String key, MapHandler mapHandler) {
        if (!handleHash.containsKey(plugin)) handleHash.put(plugin, new HashMap<String, MapHandler>());
        handleHash.get(plugin).put(key, mapHandler);
    }

    /**
     * 获取键值
     * @param plugin 插件名
     * @param key 键名
     * @return 键值,异常返回null
     */
    public static Object getObject(String name, String plugin, String key) {
        try {
            return handleHash.get(plugin).get(key).handle(name);
        } catch (Exception e) {
            return null;
        }
    }
}
