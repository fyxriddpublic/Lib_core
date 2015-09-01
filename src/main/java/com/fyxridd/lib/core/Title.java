package com.fyxridd.lib.core;

import com.comphenix.packetwrapper.WrapperPlayServerTitle;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.*;

public class Title implements Listener {
    //标题信息
    private class Info {
        private int time;
        //不为null可为""
        private String title;
        //不为null可为""
        private String subTitle;

        public Info(int time, String title, String subTitle) {
            this.time = time;
            this.title = title;
            this.subTitle = subTitle;
        }
    }

    //配置
    private int time;
    private int fadeIn, fadeOut;
    private int interval;

    //缓存
    private HashMap<Player, List<Info>> infos = new HashMap<>();
    //玩家 当前正在显示信息,需要等待的tick
    private HashMap<Player, Integer> waits = new HashMap<>();

    public Title() {
        //读取配置
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //计时器: 检测下一个
        Bukkit.getScheduler().scheduleSyncRepeatingTask(CorePlugin.instance, new Runnable() {
            @Override
            public void run() {
                //检测过期
                Iterator<Map.Entry<Player, Integer>> it = waits.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Player, Integer> entry = it.next();
                    if (entry.getValue() <= 1) it.remove();
                    else entry.setValue(entry.getValue()-1);
                }
                //检测下一个
                for (Player p:Bukkit.getOnlinePlayers()) {
                    if (!waits.containsKey(p)) {
                        List<Info> list = init(p);
                        if (!list.isEmpty()) {
                            Info info = list.remove(0);
                            show(p, info.time, info.title, info.subTitle);
                        }
                    }
                }
            }
        }, 1, 1);
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

    /**
     * @see com.fyxridd.lib.core.api.CoreApi#sendTitleAll(String, String, boolean)
     */
    public void sendTitleAll(String title, String subTitle, boolean instant) {
        int time = ((title == null?0:title.length())+(subTitle == null?0:subTitle.length()))*this.time;
        sendTitleAll(title, subTitle, instant, time);
    }

    /**
     * @see com.fyxridd.lib.core.api.CoreApi#sendTitleAll(String, String, boolean, int)
     */
    public void sendTitleAll(String title, String subTitle, boolean instant, int time) {
        for (Player p:Bukkit.getOnlinePlayers()) sendTitle(p, title, subTitle, instant, time);
    }

    /**
     * @see com.fyxridd.lib.core.api.CoreApi#sendTitle(Player, String, String, boolean)
     */
    public void sendTitle(Player p, String title, String subTitle, boolean instant) {
        int time = ((title == null?0:title.length())+(subTitle == null?0:subTitle.length()))*this.time;
        sendTitle(p, title, subTitle, instant, time);
    }

    /**
     * @see com.fyxridd.lib.core.api.CoreApi#sendTitle(Player, String, String, boolean, int)
     */
    public void sendTitle(Player p, String title, String subTitle, boolean instant, int time) {
        if (instant) show(p, time, title, subTitle);
        else init(p).add(new Info(time, title == null ? "" : title, subTitle == null ? "" : subTitle));
    }

    /**
     * 强制玩家立即显示信息
     */
    private void show(Player p, int time, String title, String subTitle) {
        waits.put(p, time+interval+fadeIn+fadeOut);

        WrapperPlayServerTitle packet;

        //清除
        packet = new WrapperPlayServerTitle();
        packet.setAction(EnumWrappers.TitleAction.CLEAR);
        packet.sendPacket(p);

        //时间
        packet = new WrapperPlayServerTitle();
        packet.setAction(EnumWrappers.TitleAction.TIMES);
        packet.setFadeIn(fadeIn);
        packet.setStay(time);
        packet.setFadeOut(fadeOut);
        packet.sendPacket(p);

        //标题
        packet = new WrapperPlayServerTitle();
        packet.setAction(EnumWrappers.TitleAction.TITLE);
        packet.setTitle(WrappedChatComponent.fromText(title));
        packet.sendPacket(p);

        //副标题
        packet = new WrapperPlayServerTitle();
        packet.setAction(EnumWrappers.TitleAction.SUBTITLE);
        packet.setTitle(WrappedChatComponent.fromText(subTitle));
        packet.sendPacket(p);
    }

    private List<Info> init(Player p) {
        List<Info> list = infos.get(p);
        if (list == null) {
            list = new ArrayList<>();
            infos.put(p, list);
        }
        return list;
    }

    private void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        time = config.getInt("title.time");
        if (time < 0) {
            time = 0;
            ConfigApi.log(CorePlugin.pn, "title.time < 0");
        }
        fadeIn = config.getInt("title.fadeIn");
        if (fadeIn < 0) {
            fadeIn = 0;
            ConfigApi.log(CorePlugin.pn, "title.fadeIn < 0");
        }
        fadeOut = config.getInt("title.fadeOut");
        if (fadeOut < 0) {
            fadeOut = 0;
            ConfigApi.log(CorePlugin.pn, "title.fadeOut < 0");
        }
        interval = config.getInt("title.interval");
        if (interval < 0) {
            interval = 0;
            ConfigApi.log(CorePlugin.pn, "title.interval < 0");
        }
    }
}
