package com.fyxridd.lib.core;

import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CoreApi;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.FormatApi;
import com.fyxridd.lib.core.api.event.PlayerOperateEvent;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.hashList.HashList;
import com.fyxridd.lib.core.api.hashList.HashListImpl;
import com.fyxridd.lib.core.api.inter.FunctionInterface;
import com.fyxridd.lib.core.show.ShowManager;
import com.fyxridd.lib.core.api.inter.FancyMessage;
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

import java.util.HashMap;

public class FuncManager implements Listener, CommandExecutor {

    //配置

	private static ChatColor invalidColor;
	private static ChatColor[] invalidFormat;
	private static ChatColor offColor;
	private static ChatColor[] offFormat;
	private static HashList<String> banFuncs;
    //使用名 功能名
	private static HashMap<String, String> funcMap;
    //功能名 使用名
    private static HashMap<String, String> funcMap2;

    //缓存

    //功能名 功能
    private static HashMap<String, FunctionInterface> funcHash = new HashMap<>();

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
        //功能无效
        if (!isValid(func)) {
            ShowManager.tip(p, get(525), true);
            return true;
        }

        //dataArgs
        String[] dataArgs = new String[length-1];
        if (length > 1) System.arraycopy(args, 1, dataArgs, 0, length-1);

        //发出事件
        PlayerOperateEvent e = new PlayerOperateEvent(p, func, dataArgs);
        Bukkit.getPluginManager().callEvent(e);
        if (e.isCancel()) return true;

        //操作
        func.onOperate(p, dataArgs);
        return true;
    }

	@EventHandler(priority=EventPriority.LOW)
	public void onReloadConfig(ReloadConfigEvent e) {
		if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
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
     * @see com.fyxridd.lib.core.api.FuncApi#register(com.fyxridd.lib.core.api.inter.FunctionInterface)
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
     * @see com.fyxridd.lib.core.api.FuncApi#convert(String, String)
     */
    public String convert(String funcName, String arg) {
        if (arg == null) return null;

        String mapKey = funcMap2.get(funcName);
        if (mapKey == null) return null;
        return "/f "+mapKey+" "+arg;
    }

    /**
     * @see com.fyxridd.lib.core.api.FuncApi#convert(String, String[])
     */
    public String convert(String funcName, String[] args) {
        return convert(funcName, CoreApi.combine(args, " ", 0, args.length));
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
		banFuncs = new HashListImpl<>();
		banFuncs.convert(config.getStringList("banFuncs"), false);
        //func map
        funcMap = new HashMap<>();
        funcMap2 = new HashMap<>();
        for (String func:config.getStringList("funcManager.funcMap")) {
            String[] ss = func.split(" ");
            String key = ss[0];
            String value = ss[1];
            funcMap.put(key, value);
            funcMap2.put(value, key);
        }
	}

	private static FancyMessage get(int id) {
		return FormatApi.get(CorePlugin.pn, id);
	}

	private static FancyMessage get(int id, Object... args) {
		return FormatApi.get(CorePlugin.pn, id, args);
	}
}
