package com.fyxridd.lib.core.transaction;

import com.fyxridd.lib.core.api.*;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.inter.FunctionInterface;
import com.fyxridd.lib.core.api.inter.TransactionUser;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import com.fyxridd.lib.core.api.inter.TipTransaction;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.*;

public class TipTransactionManager implements Listener, FunctionInterface {
    private static final String FUNC_NAME = "TipTransaction";
    private static final String SHORT_TIP = "st_ttip";

    private class TipInfo {
        String per;
        boolean instant;
        HashMap<String, Object> map;
        HashMap<String, List<Object>> recommend;
        String key;
        List<String> tipList;
        String cmd;

        public TipInfo(String per, boolean instant, HashMap<String, Object> map, HashMap<String, List<Object>> recommend, String key, List<String> tipList, String cmd) {
            this.per = per;
            this.instant = instant;
            this.map = map;
            this.recommend = recommend;
            this.key = key;
            this.tipList = tipList;
            this.cmd = cmd;
        }
    }

    //配置

    //提示前缀,如'提示: '
    private static FancyMessage prefix;

    private HashMap<String, TipInfo> tips;

    //缓存

    //玩家 提示事务
    //每个玩家最多只能同时有一个提示事务
    private static HashMap<Player, TipTransaction> playerTipTransactionHashMap = new HashMap<>();

	public TipTransactionManager() {
		//读取配置文件
		loadConfig();
		//注册事件
		Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //注册功能
        FuncApi.register(this);
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

    @Override
    public String getName() {
        return FUNC_NAME;
    }

    @Override
    public boolean isOn(String name, String data) {
        return true;
    }

    /**
     * '配置名' 提示
     */
    @Override
    public void onOperate(Player p, String... args) {
        switch (args.length) {
            case 1:
                //指定的配置不存在
                TipInfo info = tips.get(args[0]);
                if (info == null) {
                    ShowApi.tip(p, get(1210), true);
                    return;
                }
                //权限检测
                if (!PerApi.checkPer(p, info.per)) return;
                //短期检测
                if (!SpeedApi.checkShort(p, CorePlugin.pn, SHORT_TIP, 2)) return;
                //tipTransaction
                HashMap<String, Object> mapCopy;
                if (info.map == null) mapCopy = null;
                else {
                    mapCopy = new HashMap<>();
                    for (Map.Entry<String, Object> entry:info.map.entrySet()) {
                        mapCopy.put(entry.getKey(), entry.getValue());
                    }
                }
                HashMap<String, List<Object>> recommendCopy;
                if (info.recommend == null) recommendCopy = null;
                else {
                    recommendCopy = new HashMap<>();
                    for (Map.Entry<String, List<Object>> entry:info.recommend.entrySet()) {
                        List<Object> listCopy = new ArrayList<>(entry.getValue());
                        recommendCopy.put(entry.getKey(), listCopy);
                    }
                }
                List<FancyMessage> tip = new ArrayList<>();
                for (String s:info.tipList) tip.add(FormatApi.get(s.split(" ")[0], Integer.parseInt(s.split(" ")[1]), p.getName()));
                TipTransaction tipTransaction = TransactionApi.newTipTransaction(info.instant, p.getName(), -1, -1, info.cmd, tip, mapCopy, recommendCopy, info.key);
                TransactionUser tu = TransactionManager.getTransactionUser(p.getName());
                tu.addTransaction(tipTransaction);
                tu.setRunning(tipTransaction.getId());
                tipTransaction.updateShow();
                return;
        }
        //输入格式错误
        ShowApi.tip(p, get(5), true);
    }

    public static FancyMessage getPrefix() {
        return prefix;
    }

    public static HashMap<Player, TipTransaction> getPlayerTipTransactionHashMap() {
        return playerTipTransactionHashMap;
    }

	private void loadConfig() {
        //prefix
        prefix = get(1200);
        //tips
        tips = new HashMap<>();
        YamlConfiguration tipsConfig = CoreApi.loadConfigByUTF8(new File(CorePlugin.dataPath, "tips.yml"));
        for (String name:tipsConfig.getValues(false).keySet()) {
            String per = tipsConfig.getString(name+".per");
            boolean instant = tipsConfig.getBoolean(name+".instant");
            HashMap<String, Object> map = new HashMap<>();
            for (String s:tipsConfig.getStringList(name+".map")) {
                if (s.split(" ").length == 1) map.put(s, "");
                else map.put(s.split(" ")[0], s.split(" ")[1]);
            }
            if (map.isEmpty()) map = null;
            HashMap<String, List<Object>> recommend = new HashMap<>();
            for (String s:tipsConfig.getStringList(name+".recommend")) {
                List<Object> list = new ArrayList<>();
                Collections.addAll(list, s.split(" ")[1].split(","));
                recommend.put(s.split(" ")[0], list);
            }
            if (recommend.isEmpty()) recommend = null;
            String key = tipsConfig.getString(name+".key");
            if (key.isEmpty()) key = null;
            List<String> tipList = tipsConfig.getStringList(name+".tip");
            String cmd = tipsConfig.getString(name+".cmd");
            tips.put(name, new TipInfo(per, instant, map, recommend, key, tipList, cmd));
        }
    }

	private static FancyMessage get(int id) {
		return FormatApi.get(CorePlugin.pn, id);
	}
}
