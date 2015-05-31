package lib.core;

import lib.core.api.*;
import lib.core.api.event.PlayerOperateEvent;
import lib.core.api.hashList.HashListImpl;
import lib.core.api.event.ReloadConfigEvent;
import lib.core.api.hashList.HashList;
import lib.core.api.inter.FancyMessage;
import lib.core.api.inter.FunctionInterface;
import lib.core.show.ShowManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;

public class FuncManager implements Listener, CommandExecutor {
	private static ChatColor invalidColor;
	private static ChatColor[] invalidFormat;
	private static ChatColor offColor;
	private static ChatColor[] offFormat;
	private static HashList<String> banFuncs;
	private static boolean perPass;
	private static String passPer;
	private static boolean cancelCmdNoConfig;
	//命令(忽略大小写) 功能名
	private static HashMap<String, String> cmdToFuncHash;
	//功能名 功能
	private static HashMap<String, FunctionInterface> funcHash = new HashMap<String, FunctionInterface>();
    //使用名 功能名
	private static HashMap<String, String> funcMap;

	public FuncManager() {
		//注册事件
		Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
	}

    /**
     * '/f 使用名 [数据]'<br>
     * 使用名会被映射到功能名
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //命令只能由玩家发出
        Player p = null;
        if (sender instanceof Player) p = (Player) sender;
        if (p == null) {
            sender.sendMessage(get(127).getText());
            return true;
        }
        //未指定功能使用名
        int length = args.length;
        if (length < 1) {
            ShowManager.tip(p, get(515), true);
            return true;
        }
        //未找到使用名映射的功能名
        String funcName = funcMap.get(args[0]);
        if (funcName == null) {
            ShowManager.tip(p, get(500, args[0]), true);
            return true;
        }
        //未找到功能名映射的功能
        FunctionInterface func = funcHash.get(funcName);
        if (func == null) {
            ShowManager.tip(p, get(505, funcName), true);
            return true;
        }
        //调用功能
        String data;
        if (length <= 1) data = null;
        else data = CoreApi.combine(args, " ", 1, length);
        //限制检测
        if (operate(p, funcName, null)) func.onOperate(p, data);
        return true;
    }

	@EventHandler(priority=EventPriority.LOW)
	public void onReloadConfig(ReloadConfigEvent e) {
		if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
	}

	@EventHandler(priority=EventPriority.LOW,ignoreCancelled=true)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		//有权限不进行限制检测
		if (perPass && PerApi.has(e.getPlayer(), passPer)) return;
		//限制检测
		try {
			String[] ss = e.getMessage().split(" ");
			String cmdName = ss[0].substring(1).toLowerCase();
			String funcName = cmdToFuncHash.get(cmdName);
			if (funcName == null) {//没有对应的命令->功能配置
				if (cancelCmdNoConfig) e.setCancelled(true);
			}else {//功能检测
				if (!operate(e.getPlayer(), funcName, null)) e.setCancelled(true);
			}
		} catch (Exception e1) {//命令异常,取消
			e.setCancelled(true);
		}
	}

    /**
     * 玩家操作时必须调用<br>
     * 比如命令调用,功能调用
     * @param p 玩家,不为null
     * @param funcName 想要操作的功能名,不为null
     * @param subFunc 子功能,可为null
     * @return 操作是否有效,false表示需要取消操作
     */
	public boolean operate(Player p, String funcName, String subFunc) {
        //功能不存在
		FunctionInterface func = getFunc(funcName);
		if (func == null) {
            ShowManager.tip(p, get(520), true);
            return false;
        }
        //功能无效
        if (!isValid(func)) {
            ShowManager.tip(p, get(525), true);
            return false;
        }
        //功能对玩家未开启
        if (!func.isOn(p.getName(), subFunc)) {
			ShowManager.tip(p, get(530), true);
			return false;
		}
        //检测
        PlayerOperateEvent e = new PlayerOperateEvent(p, func, subFunc);
        Bukkit.getPluginManager().callEvent(e);
        return !e.isCancel();
	}
	
	/**
	 * 获取功能
	 * @param name 功能名,不为null
	 * @return 功能,没有返回null
	 */
	public static FunctionInterface getFunc(String name) {
		return funcHash.get(name);
	}

    /**
     * @see lib.core.api.FuncApi#register(lib.core.api.inter.FunctionInterface)
     */
	public static boolean register(FunctionInterface func) {
        if (func == null) return false;

		if (funcHash.containsKey(func.getName())) return false;
		funcHash.put(func.getName(), func);
		return true;
	}
	
	/**
	 * 更新显示
	 * @param mp MessagePart
	 * @param name 玩家名
	 */
	public static void update(FancyMessage.MessagePart mp, String name) {
		if (mp.func != null && !mp.func.isEmpty()) {
			FunctionInterface func = getFunc(mp.func);
			if (func != null && isValid(func)) {//找到功能
				if (!func.isOn(name, mp.data)) {
					//color
					mp.color = offColor;
					//format
					setFormat(mp, offFormat);
				}
			}else {//未找到功能
				//color
				mp.color = invalidColor;
				//format
				setFormat(mp, invalidFormat);
			}
		}
	}

    /**
     * 检测功能是否有效
     * @param func 功能,不为null
     * @return 是否有效
     */
    public static boolean isValid(FunctionInterface func) {
        return !banFuncs.has(func.getName());
    }

	/**
	 * 设置格式
	 * @param mp MessagePart
	 * @param format 要附加的格式列表
	 */
	private static void setFormat(FancyMessage.MessagePart mp, ChatColor[] format) {
		HashMap<ChatColor, Boolean> result = new HashMap<ChatColor, Boolean>();
		if (format != null) {
			for (ChatColor c:format) result.put(c, true);
		}
		if (mp.styles != null) {
			for (ChatColor c:mp.styles) result.put(c, true);
		}
		if (result.isEmpty()) mp.styles = null;
		else {
			ChatColor[] cc = new ChatColor[result.size()];
			int index = 0;
			for (ChatColor c:result.keySet()) cc[index++] = c;
			mp.styles = cc;
		}
	}

	private void loadConfig() {
		YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);
		//invalid
		invalidColor = ChatColor.getByChar(config.getString("funcManager.invalid.color"));
		String s = config.getString("funcManager.invalid.format");
		invalidFormat = new ChatColor[s.length()];
		int index = 0;
		for (char c:s.toCharArray()) invalidFormat[index++] = ChatColor.getByChar(c);
        //off
		offColor = ChatColor.getByChar(config.getString("funcManager.notOn.color"));
		s = config.getString("funcManager.notOn.format");
        offFormat = new ChatColor[s.length()];
		index = 0;
		for (char c:s.toCharArray()) offFormat[index++] = ChatColor.getByChar(c);
		//banFuncs
		banFuncs = new HashListImpl<String>();
		banFuncs.convert(config.getStringList("banFuncs"), false);
		//perPass,passPer,cancelCmdNoConfig
		perPass = config.getBoolean("funcManager.perPass");
		passPer = config.getString("funcManager.passPer");
		cancelCmdNoConfig = config.getBoolean("funcManager.cancelCmdNoConfig");
		//cmdToFunc
		cmdToFuncHash = new HashMap<String, String>();
		for (String ss:config.getStringList("funcManager.cmdToFunc")) {
			String cmd = ss.split(" ")[0].toLowerCase();
			String funcName = ss.split(" ")[1];
			cmdToFuncHash.put(cmd, funcName);
		}
        //func map
        funcMap = new HashMap<String, String>();
        for (String func:config.getStringList("funcManager.funcMap")) {
            String[] ss = func.split(" ");
            String key = ss[0];
            String value = ss[1];
            funcMap.put(key, value);
        }
	}

	private static FancyMessage get(int id) {
		return FormatApi.get(CorePlugin.pn, id);
	}

	private static FancyMessage get(int id, Object... args) {
		return FormatApi.get(CorePlugin.pn, id, args);
	}
}
