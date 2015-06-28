package com.fyxridd.lib.core;

import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CoreApi;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.show.condition.Condition;
import com.fyxridd.lib.core.api.MessageApi;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 语言管理类 <br>
 * 使用配置文件里的language配置
 */
public class FormatManager implements Listener {
	private static final String NUM_LIST = "0123456789";

    //配置
    private static String langDefault, langError;

	//language
	//插件名 文本id 内容
	private static HashMap<String, HashMap<String, HashMap<Integer, FancyMessage>>> msgHash = new HashMap<String, HashMap<String, HashMap<Integer, FancyMessage>>>();

	public FormatManager() {
		//注册事件
		Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
	}
	
	/**
	 * 检测重新读取语言文件<br>
     * 语言文件需要放置在插件数据文件夹内,名字格式为'lang_xx.yml',其中xx为语言名
	 */
	@EventHandler(priority=EventPriority.LOWEST)
	public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();

        register(e.getPlugin(), new File(CoreApi.pluginPath, e.getPlugin()));
	}

    /**
     * @see com.fyxridd.lib.core.api.FormatApi#get(String, int)
     */
	public static FancyMessage get(String pluginName, int id) {
        return get(langDefault, pluginName, id);
	}

    /**
     * @see com.fyxridd.lib.core.api.FormatApi#get(String, int, Object...)
     */
	public static FancyMessage get(String pluginName, int id, Object... args) {
        return get(langDefault, pluginName, id, args);
	}

    /**
     * @see com.fyxridd.lib.core.api.FormatApi#get(String, String, int)
     */
    public static FancyMessage get(String lang, String pluginName, int id) {
        //无插件信息
        HashMap<String, HashMap<Integer, FancyMessage>> langHash = msgHash.get(pluginName);
        if (langHash == null) return null;
        //无指定语言信息
        HashMap<Integer, FancyMessage> hash = langHash.get(lang);
        if (hash == null) hash = langHash.get(langError);
        if (hash == null) return null;
        //返回
        FancyMessage msg = hash.get(id);
        if (msg != null) return msg.clone();
        else return null;
    }

    /**
     * @see com.fyxridd.lib.core.api.FormatApi#get(String, String, int, Object...)
     */
    public static FancyMessage get(String lang, String pluginName, int id, Object... args) {
        FancyMessage result = get(lang, pluginName, id);
        if (result != null) MessageApi.convert(result, args);
        return result;
    }

	/**
	 * 把字符串以占位符分割,分割后的长度=占位符数量+1
	 * @param s 字符串,不为null
	 * @return 分割后的字符串序列
	 */
	public static String[] split(String s) {
		List<String> result = new ArrayList<String>();
		StringBuffer add = new StringBuffer();
		for (int i=0;i<s.length();i++) {
			if (s.charAt(i) == '<') {
				boolean hasNum = false;
				boolean hasEnd = false;
				int end = i;
				for (int j=i+1;j<s.length();j++) {
					if (NUM_LIST.indexOf(s.charAt(j)) != -1) {
						hasNum = true;
					}else if (s.charAt(j) == '>') {
						hasEnd = true;
						end = j;
						break;
					}else {
						break;
					}
				}
				//有数字,有结束标志>,说明已经成功检测到了一个完整的替代符
				//start - end
				if (hasNum && hasEnd) {
					result.add(add.toString());
					add = new StringBuffer();
					i = end;
					continue;
				}
			}
			add.append(s.charAt(i));
		}
		result.add(add.toString());
		return result.toArray(new String[result.size()]);
	}

	/**
	 * 读取FancyMessage
	 * @param msg 内容,不为null
	 * @param ms 包含了FancyMessage信息的MemorySection,可为null
	 * @return 读取的FancyMessage,不为null
	 */
	public static FancyMessage load(String msg, MemorySection ms) {
		String[] ss = split(msg);
		FancyMessage fm = null;
		boolean first = true;
		int index = 0;
		for (String s:ss) {
			if (first) first = false;
			else {//读取一个MessagePart
				if (ms != null) {
                    List<Condition> con = MessageApi.getCon(ms.getString(index+".con", ""));
					String temp = ms.getString(index+".func", "");
					if (temp == null) temp = "";
					String func;
					String data;
                    if (temp.contains(":")) {
						func = temp.split(":")[0];
						data = temp.split(":")[1];
					}else {
						func = temp;
						data = "";
					}
                    String item = ms.getString(index+".item", "");
					String text = CoreApi.convert(ms.getString(index+".text", ""));
					String color = ms.getString(index+".color", "");
					String formats = ms.getString(index+".formats", "");
					String onClick = CoreApi.convert(ms.getString(index+".onClick", ""));
					String onHover = CoreApi.convert(ms.getString(index+".onHover", ""));
					//index+1
					index++;
					//text
                    if (fm == null) fm = new FancyMessageImpl(text);
					else fm.then(text);
                    //con
                    if (con != null) fm.con(con);
					//func
					if (!func.isEmpty()) fm.func(func);
					if (!data.isEmpty()) fm.data(data);
                    //item
                    if (item != null && !item.isEmpty()) fm.item(item);
					//color
					if (color != null && !color.isEmpty()) fm.color(ChatColor.getByChar(color));
					//styles
					if (formats != null && !formats.isEmpty()) {
						ChatColor[] styles = new ChatColor[formats.length()];
						int i2 = 0;
						for (char c:formats.toCharArray()) styles[i2++] = ChatColor.getByChar(c);
						fm.style(styles);
					}
					//onClick
					if (onClick != null && !onClick.isEmpty()) {
						String[] s2 = onClick.split(" ");
						String content = CoreApi.combine(s2, " ", 1, s2.length);
						if (s2[0].equals("file")) fm.file(content);
						else if (s2[0].equals("url")) fm.link(content);
						else if (s2[0].equals("suggest")) fm.suggest(content);
						else if (s2[0].equals("cmd")) fm.command(content);
					}
					//onHover
					if (onHover != null && !onHover.isEmpty()) {
						fm.itemTooltip(MessageApi.getHoverActionData(onHover), onHover);
					}
				}
			}
            if (!s.isEmpty()) {
                if (fm == null) fm = new FancyMessageImpl(s);
                else fm.then(s);
            }
		}
        if (fm == null) fm = new FancyMessageImpl("");
		fm.update();
		return fm;
	}

    /**
     * 保存FancyMessage到config<br>
     * 会生成show-num: "xxx"<br>
     * 及info-num相关信息
     * @param num 行号,>0
     * @param config 配置,不为null
     * @param msg 信息,不为null
     */
    public static void save(int num, YamlConfiguration config, FancyMessage msg) {
        int size = msg.getMessageParts().size();
        String str = "";
        for (int i=0;i<size;i++) str += ("<"+i+">");
        config.set("show-"+num, str);
        int index = 0;
        for (FancyMessage.MessagePart mp:msg.getMessageParts()) {
            String path = "info-" + num + "." + (index++);
            mp.save(config, path);
        }
    }

    /**
	 * 注册<b>普通</b>语言与<b>格式</b>语言
	 * @param pn 插件名,不为null
     * @param dataDir 插件数据文件夹
	 * @return 成功返回true,否则返回false
	 */
	private boolean register(String pn, File dataDir) {
        try {
            //langHash
            HashMap<String, HashMap<Integer,FancyMessage>> langHash = new HashMap<String, HashMap<Integer, FancyMessage>>();
            //具体
            if (dataDir.exists() && dataDir.isDirectory()) {
                File[] files = dataDir.listFiles();
                if (files != null) {
                    for (File file:files) {
                        String fileName = file.getName();
                        if (fileName.startsWith("lang_") && fileName.endsWith(".yml")) {
                            //hash
                            HashMap<Integer,FancyMessage> hash = new HashMap<Integer, FancyMessage>();
                            //读取语言
                            YamlConfiguration langConfig = CoreApi.loadConfigByUTF8(file);
                            for (String key:langConfig.getKeys(true)) {
                                String[] ss = key.split("\\-");
                                if (ss.length == 2 && ss[0].equalsIgnoreCase("show")) {
                                    int id = Integer.parseInt(ss[1]);
                                    String value = CoreApi.convert(langConfig.getString(key));
                                    hash.put(id, load(value, (MemorySection) langConfig.get("info-" + id)));
                                }
                            }
                            //添加
                            String lang = fileName.substring(5, fileName.length()-4);
                            langHash.put(lang, hash);
                        }
                    }
                }
            }
            //添加
            msgHash.put(pn, langHash);
            return true;
        } catch (Exception e) {
            return false;
        }
	}

    private static void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        langDefault = config.getString("lang.default");
        langError = config.getString("lang.error");
    }
}
