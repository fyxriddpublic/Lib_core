package com.fyxridd.lib.core.api;

import com.fyxridd.lib.core.FormatManager;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

public class FormatApi {
    /**
     * 获取文本(默认语言)
     * @param pluginName 文本所属插件名,可为null(null时返回null)
     * @param id 文本id
     * @return 文本(复制版),如果没有或异常则返回null
     */
    public static FancyMessage get(String pluginName, int id) {
        return FormatManager.get(pluginName, id);
    }

    /**
     * 获取格式转换后的文本(默认语言)
     * @param pluginName 文本所属插件名,可为null(null时返回null)
     * @param id 文本id
     * @param args 变量列表,null项变量会被变成"".会将替换符{0},{1},{2}...替换成对应的变量
     * @return 转换后的字符串(复制版),异常返回null
     */
    public static FancyMessage get(String pluginName, int id, Object... args) {
        return FormatManager.get(pluginName, id, args);
    }

    /**
     * 获取文本
     * @param lang 指定语言
     * @param pluginName 文本所属插件名,可为null(null时返回null)
     * @param id 文本id
     * @return 文本(复制版),如果没有或异常则返回null
     */
    public static FancyMessage get(String lang, String pluginName, int id) {
        return FormatManager.get(lang, pluginName, id);
    }

    /**
     * 获取格式转换后的文本
     * @param lang 指定语言
     * @param pluginName 文本所属插件名,可为null(null时返回null)
     * @param id 文本id
     * @param args 变量列表,null项变量会被变成"".会将替换符{0},{1},{2}...替换成对应的变量
     * @return 转换后的字符串(复制版),异常返回null
     */
    public static FancyMessage get(String lang, String pluginName, int id, Object... args) {
        return FormatManager.get(lang, pluginName, id, args);
    }

    /**
     * 读取FancyMessage
     * @param msg 内容,不为null
     * @param ms 包含了FancyMessage信息的MemorySection,可为null
     * @return 读取的FancyMessage,不为null
     */
    public static FancyMessage load(String msg, MemorySection ms) {
        return FormatManager.load(msg, ms);
    }

    /**
     * @see FormatManager#save(int, org.bukkit.configuration.file.YamlConfiguration, FancyMessage)
     */
    public static void save(int num, YamlConfiguration config, FancyMessage fm) {
        FormatManager.save(num, config, fm);
    }
}
