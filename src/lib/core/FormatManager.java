package lib.core;

import lib.core.api.ConfigApi;
import lib.core.api.CoreApi;
import lib.core.api.CorePlugin;
import lib.core.api.MessageApi;
import lib.core.api.event.ReloadConfigEvent;
import lib.core.api.inter.FancyMessage;
import lib.core.show.condition.Condition;
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

	//language
	//插件名 文本id 内容
	private static HashMap<String, HashMap<Integer, FancyMessage>> msgHash = new HashMap<String, HashMap<Integer, FancyMessage>>();

	public FormatManager() {
		//注册事件
		Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
	}
	
	/**
	 * 检测重新读取语言文件<br>
	 * 会检测配置文件config.yml中的"language"项
	 */
	@EventHandler(priority=EventPriority.LOWEST)
	public void onReloadConfig(ReloadConfigEvent e) {
		if (!ConfigApi.getConfig(e.getPlugin()).contains("language")) return;
		YamlConfiguration languageConfig = CoreApi.loadConfigByUTF8(new File(ConfigApi.getConfig(e.getPlugin()).getString("language")));
		if (languageConfig != null) register(e.getPlugin(), languageConfig);
	}
	
	/**
	 * 获取文本
	 * @param pluginName 文本所属插件名,null表示lib插件名
	 * @param id 文本id
	 * @return 文本,如果没有或异常则返回null
	 */
	public static FancyMessage get(String pluginName, int id) {
		try {
			if (pluginName == null) pluginName = CorePlugin.pn;
			return msgHash.get(pluginName).get(id).clone();
		} catch (Exception e) {
            //不做处理
		}
		return null;
	}
	
	/**
	 * 格式转换
	 * @param pluginName 文本所属插件名,null表示lib插件名
	 * @param id 文本id
	 * @param args 变量列表,null项变量会被变成"".会将替换符{0},{1},{2}...替换成对应的变量
	 * @return 转换后的字符串(复制版),异常返回null
	 */
	public static FancyMessage get(String pluginName, int id, Object... args) {
		FancyMessage result = get(pluginName, id);
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
	 * @param languageConfig 语言文件config,不为null
	 * @return 成功返回true,否则返回false
	 */
	private boolean register(String pn,YamlConfiguration languageConfig) {
		try {
			HashMap<Integer,FancyMessage> hash = new HashMap<Integer, FancyMessage>();
			for (String key:languageConfig.getKeys(true)) {
				String[] ss = key.split("\\-");
				if (ss.length == 2 && ss[0].equalsIgnoreCase("show")) {
					int id = Integer.parseInt(ss[1]);
					String value = CoreApi.convert(languageConfig.getString(key));
					hash.put(id, load(value, (MemorySection) languageConfig.get("info-" + id)));
				}
            }
			msgHash.put(pn, hash);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
