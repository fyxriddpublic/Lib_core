package com.fyxridd.lib.core;

import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.FormatApi;
import com.fyxridd.lib.core.api.ShowApi;
import com.fyxridd.lib.core.api.event.FirstJoinEvent;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import com.fyxridd.lib.core.api.model.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import java.util.HashMap;

public class RealName implements Listener{
    private static boolean enable = true;

    //不完整,动态读取
	//玩家名(小写) 玩家真名
	private static HashMap<String, User> realNameHash = new HashMap<>();

	public RealName() {
        //读取配置文件
        loadConfig();
		//注册事件
		Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
	}

    @EventHandler(priority=EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerLogin(PlayerLoginEvent e) {
        //已经禁止了
        if (!e.getResult().equals(PlayerLoginEvent.Result.ALLOWED)) return;

        if (enable) {
            //检测真名,禁止非法进入
            String realName = getRealName(null, e.getPlayer().getName());
            if (realName != null && !realName.equals(e.getPlayer().getName())) {
                e.setResult(Result.KICK_OTHER);
                String msg = get(905, e.getPlayer().getName(), realName).getText();
                e.setKickMessage(msg);
            }
        }
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
        //获取用户信息
        User user = get(e.getPlayer().getName());

        //用户无信息
        if (user == null) {
            //新建
            user = new User(e.getPlayer().getName());
            //缓存
            realNameHash.put(e.getPlayer().getName().toLowerCase(), user);
            //db
            CoreMain.dao.saveOrUpdate(user);
            //发出事件
            FirstJoinEvent firstJoinEvent = new FirstJoinEvent(e.getPlayer());
            Bukkit.getPluginManager().callEvent(firstJoinEvent);
        }
	}

    /**
     * 获取玩家的真名
     * @param sender 玩家不存在信息的接收者,可为null
     * @param name 玩家名,不为null
     * @return 真名,没有返回null
     */
    public static String getRealName(CommandSender sender, String name) {
        //获取信息
        User user = get(name);

        //存在,返回真名
        if (user != null) return user.getName();

        //目标玩家不存在,检测提示
        if (sender != null) {
            FancyMessage msg = get(900, name);
            if (sender instanceof Player) {
                Player p = (Player) sender;
                ShowApi.tip(p, msg, true);
            } else sender.sendMessage(msg.getText());
        }
        return null;
    }

    /**
     * 获取玩家信息
     * @param name 玩家名,不为null
     * @return 信息,不存在返回null
     */
    private static User get(String name) {
        //先从缓存中读取
        User result = realNameHash.get(name.toLowerCase());
        if (result != null) return result;

        //再从数据库中读取
        result = Dao.getUser(name);
        if (result != null) {
            //保存缓存
            realNameHash.put(name.toLowerCase(), result);
        }

        return result;
    }

    private static void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        enable = config.getBoolean("realName.enable");
    }

    private static FancyMessage get(int id, Object... args) {
        return FormatApi.get(CorePlugin.pn, id, args);
    }
}
