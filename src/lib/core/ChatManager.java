package lib.core;

import lib.core.api.*;
import lib.core.api.event.PlayerChatEvent;
import lib.core.api.event.ReloadConfigEvent;
import lib.core.api.inter.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatManager implements Listener {
    private class ShowTask implements Runnable {
        @Override
        public void run() {
            Iterator<Map.Entry<Player, Queue<FancyMessage>>> it = delayChats.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Player, Queue<FancyMessage>> entry = it.next();
                if (!ShowApi.isInPage(entry.getKey())) {
                    Queue<FancyMessage> queue = entry.getValue();
                    if (!queue.isEmpty()) ShowApi.tip(entry.getKey(), queue.poll(), true);
                    //删除空聊天信息的
                    if (queue.isEmpty()) it.remove();
                }
            }

            //下个循环
            Bukkit.getScheduler().scheduleSyncDelayedTask(CorePlugin.instance, showTask, delayShowInterval);
        }
    }


    //常量

    private static final Lock chatLock = new ReentrantLock();
    private final ShowTask showTask = new ShowTask();

    //配置

    private int maxSaves;
    private int delayShowInterval;
    private FancyMessage delayShowPrefix;

    //缓存

    private static List<PlayerChatEvent> chatEvents = new ArrayList<PlayerChatEvent>();

    private HashMap<Player, Queue<FancyMessage>> delayChats;

    public ChatManager() {
        //读取配置
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //同步检测发出聊天事件
        Bukkit.getScheduler().scheduleSyncRepeatingTask(CorePlugin.instance, new Runnable() {
            @Override
            public void run() {
                if (chatLock.tryLock()) {
                    try{
                        //发出事件
                        for (PlayerChatEvent event:chatEvents) {
                            if (!event.getP().isOnline()) continue;

                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                FancyMessage msg = get(35, event.getP().getName(), event.getMsg());
                                for (Player tar: Bukkit.getOnlinePlayers()) addChat(tar, msg, false);
                            }
                        }

                        //清空事件列表
                        chatEvents.clear();
                    }finally {
                        chatLock.unlock();
                    }
                }
            }
        }, 1, 1);
        //延时聊天
        Bukkit.getScheduler().scheduleSyncDelayedTask(CorePlugin.instance, showTask, delayShowInterval);
    }

    /**
     * @see lib.core.api.CoreApi#addChat(org.bukkit.entity.Player, lib.core.api.inter.FancyMessage, boolean)
     */
    public void addChat(Player p, FancyMessage msg, boolean force) {
        if (p == null || msg == null) return;

        if (force || !ShowApi.isInPage(p)) ShowApi.tip(p, msg, true);
        else {
            //延时信息添加前缀
            if (delayShowPrefix != null) msg.combine(delayShowPrefix, true);
            //添加到延时显示队列
            Queue<FancyMessage> queue = delayChats.get(p);
            if (queue == null) {
                queue = new ArrayBlockingQueue<FancyMessage>(maxSaves, false);
                delayChats.put(p, queue);
            }
            while (queue.size() >= maxSaves) queue.poll();
            queue.offer(msg);
        }
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        try {
            if (!chatLock.tryLock(3, TimeUnit.SECONDS)) throw new InterruptedException();

            try {
                chatEvents.add(new PlayerChatEvent(e.getPlayer(), e.getMessage()));
            } finally {
                chatLock.unlock();
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent e) {
        delayChats.remove(e.getPlayer());
    }

    private void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        //因为重新读取maxsaves,因此需要重置chats
        delayChats = new HashMap<Player, Queue<FancyMessage>>();

        //maxSaves
        maxSaves = config.getInt("chat.maxSaves");
        if (maxSaves < 0) {
            maxSaves = 0;
            ConfigApi.log(CorePlugin.pn, "chat.maxSaves < 0");
        }

        //delayShowInterval
        delayShowInterval = config.getInt("chat.delayShow.interval");
        if (delayShowInterval < 1) {
            delayShowInterval = 1;
            ConfigApi.log(CorePlugin.pn, "chat.delayShow.interval < 1");
        }

        //delayShowPrefix
        String delayShowPrefixStr = CoreApi.convert(config.getString("chat.delayShow.prefix"));
        if (delayShowPrefixStr == null || delayShowPrefixStr.isEmpty()) delayShowPrefix = null;
        else delayShowPrefix = new FancyMessageImpl(delayShowPrefixStr);
    }

    private FancyMessage get(int id, Object... args) {
        return FormatApi.get(CorePlugin.pn, id, args);
    }
}
