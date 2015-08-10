package com.fyxridd.lib.core.transaction;

import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.FormatApi;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.inter.TransactionUser;
import com.fyxridd.lib.core.api.TransactionApi;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import com.fyxridd.lib.core.api.inter.TipTransaction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class TipTransactionManager implements Listener {
    //配置

    //提示前缀,如'提示: '
    private static FancyMessage prefix;

    //缓存

    //玩家 提示事务
    //每个玩家最多只能同时有一个提示事务
    private static HashMap<Player, TipTransaction> playerTipTransactionHashMap = new HashMap<>();

	public TipTransactionManager() {
		//读取配置文件
		loadConfig();
		//注册事件
		Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void onReloadConfig(ReloadConfigEvent e) {
		if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
	}

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        TipTransaction t = TipTransactionManager.getPlayerTipTransactionHashMap().remove(p);
        if (t != null) {
            TransactionUser tu = TransactionApi.getTransactionUser(p.getName());
            tu.delTransaction(t.getId());
        }
    }

    public static FancyMessage getPrefix() {
        return prefix;
    }

    public static HashMap<Player, TipTransaction> getPlayerTipTransactionHashMap() {
        return playerTipTransactionHashMap;
    }

	private static void loadConfig() {
        //prefix
        prefix = get(1200);
    }

	private static FancyMessage get(int id) {
		return FormatApi.get(CorePlugin.pn, id);
	}
}
