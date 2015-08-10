package com.fyxridd.lib.core;

import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CoreApi;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.FormatApi;
import com.fyxridd.lib.core.api.event.PlayerChatEvent;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import com.fyxridd.lib.core.api.inter.InputHandler;
import com.fyxridd.lib.core.show.ShowManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class InputManager implements Listener, CommandExecutor {
	private static final String INPUT = "input";

    //配置

	private static int interval;
    private static boolean cancelInteract, cancelAnimation;
    private static boolean allowChat, allowCmd;

    //缓存

    //在此缓存中说明玩家在输入状态中
    //玩家 输入处理器
    private static HashMap<Player, InputHandler> inputHash = new HashMap<>();

	public InputManager() {
		//读取配置文件
		loadConfig();
		//注册事件
		Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
		//注册速度
		Speed.register(CorePlugin.pn, INPUT);
	}

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (allowCmd && sender instanceof Player && args.length > 0) {
            Player p = (Player) sender;
            //玩家在输入事件中
            InputHandler inputHandler = inputHash.get(p);
            if (inputHandler != null) {
                String content = CoreApi.combine(args, " ", 0, args.length);
                if (inputHandler.onInput(content)) inputHash.remove(p);
            }
        }
        return true;
    }

	@EventHandler(priority=EventPriority.LOW)
	public void onReloadConfig(ReloadConfigEvent e) {
		if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
        inputHash.remove(e.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
        inputHash.remove(e.getPlayer());
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (cancelInteract) del(e.getPlayer());
	}

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerAnimation(PlayerAnimationEvent e) {
        if (cancelAnimation) del(e.getPlayer());
    }

    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerChat(PlayerChatEvent e) {
        //事件已取消检测
        if (e.isCancelled()) return;

        if (allowChat) {
            //玩家在输入事件中
            InputHandler inputHandler = inputHash.get(e.getP());
            if (inputHandler != null) {
                e.setCancelled(true);
                if (inputHandler.onInput(e.getMsg())) inputHash.remove(e.getP());
            }
        }
    }

    /**
	 * 注册玩家输入事件
	 * @param p 玩家,不为null
	 * @param inputHandler 处理者,不为null
	 * @return 是否注册成功
	 */
	public static boolean register(Player p, InputHandler inputHandler) {
        return register(p, inputHandler, true);
	}

    /**
     * 注册玩家输入事件
     * @param p 玩家,不为null
     * @param inputHandler 处理者,不为null
     * @param tip 成功删除旧的注册输入是否提示玩家
     * @return 是否注册成功
     */
    public static boolean register(Player p, InputHandler inputHandler, boolean tip) {
        //速度检测
        if (!Speed.check(p, CorePlugin.pn, INPUT, interval)) return false;
        //取消先前的
        if (inputHash.remove(p) != null) {
            if (tip) ShowManager.tip(p, get(800), false);
        }

        //注册新的
        inputHash.put(p, inputHandler);
        return true;
    }

    /**
     * @see #del(org.bukkit.entity.Player, boolean)
     */
    public static void del(Player p) {
        del(p, true);
    }

    /**
     * 删除玩家的输入注册
     * @param p 玩家,不为null
     * @param tip 成功删除是否提示玩家
     */
    public static void del(Player p, boolean tip) {
        if (inputHash.remove(p) != null) {
            if (tip) ShowManager.tip(p, get(800), false);
        }
    }

	private static void loadConfig() {
		YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        try {
            interval = config.getInt("input.interval");
            if (interval < 0) {
                interval = 0;
                ConfigApi.log(CorePlugin.pn, "input.interval < 0");
            }
            cancelInteract = config.getBoolean("input.cancel.interact");
            cancelAnimation = config.getBoolean("input.cancel.animation");

            allowChat = config.getBoolean("input.allow.chat");
            allowCmd = config.getBoolean("input.allow.cmd");
        } catch (Exception e) {//异常
            ConfigApi.log(CorePlugin.pn, "InputManager -> config error");
        }
    }

	private static FancyMessage get(int id) {
		return FormatApi.get(CorePlugin.pn, id);
	}
}
