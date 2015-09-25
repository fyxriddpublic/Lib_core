package com.fyxridd.lib.core.api;

import com.fyxridd.lib.core.FancyMessageImpl;
import com.fyxridd.lib.core.api.inter.Page;
import com.fyxridd.lib.core.show.condition.Condition;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageApi {
    /**
     * 获取悬浮提示信息(mc能识别处理的)
     * @param hoverActionString 提示信息字符串,不为null
     * @return 提示信息
     */
    public static String getHoverActionData(String hoverActionString) {
        String[] s3 = hoverActionString.split("\n");
        ItemStack is = new ItemStack(Material.STONE);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(s3[0]);
        List<String> lore = new ArrayList<String>();
        boolean first2 = true;
        for (String s4:s3) {
            if (first2) first2 = false;
            else lore.add(s4);
        }
        im.setLore(lore);
        is.setItemMeta(im);
        return getHoverActionData(is);
    }

    /**
     * 根据物品获取悬浮提示信息(mc能识别处理的)
     * @param is 物品,不为null
     * @return 提示信息
     */
    public static String getHoverActionData(ItemStack is) {
        return CraftItemStack.asNMSCopy(is).save(new NBTTagCompound()).toString();
    }
    public static void color(FancyMessage.MessagePart mp, final ChatColor color) {
        if (!color.isColor()) {
            throw new IllegalArgumentException(color.name() + " is not a color");
        }
        mp.color = color;
    }

    public static void style(FancyMessage.MessagePart mp, final ChatColor... styles) {
        for (final ChatColor style : styles) {
            if (!style.isFormat()) {
                throw new IllegalArgumentException(style.name() + " is not a style");
            }
        }
        mp.styles = styles;
    }

    public static void file(FancyMessage.MessagePart mp, final String path) {
        onClick(mp, "open_file", path);
    }

    public static void link(FancyMessage.MessagePart mp, final String url) {
        onClick(mp, "open_url", url);
    }

    public static void suggest(FancyMessage.MessagePart mp, final String command) {
        onClick(mp, "suggest_command", command);
    }

    public static void command(FancyMessage.MessagePart mp, final String command) {
        onClick(mp, "run_command", command);
    }

    public static void itemTooltip(FancyMessage.MessagePart mp, final String itemJSON, final String hoverActionString) {
        onHover(mp, "show_item", itemJSON, hoverActionString);
    }

    public static void itemTooltip(FancyMessage.MessagePart mp, final ItemStack itemStack, final String hoverActionString) {
        itemTooltip(mp, CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound()).toString(), hoverActionString);
    }

    /**
     * @see FancyMessage.MessagePart#getCon(String)
     */
    public static List<Condition> getCon(String s) {
        return FancyMessage.MessagePart.getCon(s);
    }

    /**
     * @see FancyMessage.MessagePart#convert(FancyMessage, Object...)
     */
    public static void convert(FancyMessage msg, Object... replace) {
        FancyMessage.MessagePart.convert(msg, replace);
    }

    /**
     * @see FancyMessage.MessagePart#convert(FancyMessage, java.util.HashMap)
     */
    public static void convert(FancyMessage msg, HashMap<String, Object> replace) {
        FancyMessage.MessagePart.convert(msg, replace);
    }

    /**
     * @see FancyMessage.MessagePart#convert(FancyMessage.MessagePart, java.util.HashMap)
     */
    public static void convert(FancyMessage.MessagePart mp, HashMap<String, Object> replace) {
        FancyMessage.MessagePart.convert(mp, replace);
    }

    /**
     * 将纯文字转化为FancyMessage格式
     * @param msg 纯文字
     * @return 对应的FancyMessage
     */
    public static FancyMessage convert(String msg) {
        return new FancyMessageImpl(msg);
    }

    private static void onClick(FancyMessage.MessagePart mp, final String name, final String data) {
        mp.clickActionName = name;
        mp.clickActionData = data;
    }

    private static void onHover(FancyMessage.MessagePart mp, final String name, final String data, final String hoverActionString) {
        mp.hoverActionName = name;
        mp.hoverActionData = data;
        mp.hoverActionString = hoverActionString;
    }

    /**
     * 从plugins/plugin/show/page.yml里读取页面信息
     * @param plugin 插件名,不为null
     * @param page 页面名,不为null
     * @return 页面,异常返回null
     */
    public static Page load(String plugin, String page) {
        return ShowApi.load(plugin, page);
    }

    /**
     * 读取页面信息
     * @param plugin 插件名
     * @param page 页面名
     * @param config 页面信息保存的yml文件,不为null
     * @return 页面信息,异常返回null
     */
    public static Page load(String plugin, String page, YamlConfiguration config) {
        return ShowApi.load(plugin, page, config);
    }

    /**
     * 保存页面到文件
     * @param page 页面,不为null
     * @return 是否成功
     */
    public static boolean save(Page page) {
        return ShowApi.save(page);
    }

    /**
     * 保存信息为字符串
     * @param msg 可为null(null时返回null)
     * @return 可为null
     */
    public static String save(FancyMessage msg) {
        if (msg == null) return null;

        //保存到config
        YamlConfiguration config = new YamlConfiguration();
        FormatApi.save(1, config, msg);
        return config.saveToString();
    }

    /**
     * 从字符串中读取信息
     * @param data 可为null(null时返回null)
     * @return 可为null
     */
    public static FancyMessage load(String data) {
        if (data == null) return null;

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(data);
            return FormatApi.load(config.getString("show-1"), (MemorySection) config.get("info-1"));
        } catch (Exception e) {
            return null;
        }
    }
}
