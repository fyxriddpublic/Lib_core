package com.fyxridd.lib.core.api.inter;

import com.fyxridd.lib.core.api.MessageApi;
import com.fyxridd.lib.core.api.hashList.HashList;
import com.fyxridd.lib.core.api.hashList.HashListImpl;
import com.fyxridd.lib.core.show.condition.Condition;
import com.fyxridd.lib.core.show.condition.MathCompareCondition;
import com.fyxridd.lib.core.show.condition.StringCompareCondition;
import com.fyxridd.lib.core.show.condition.StringHasCondition;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.JSONException;
import org.json.JSONWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface FancyMessage extends Cloneable{
    public static class MessagePart implements Cloneable{
        /**
         * text里是否有{名称}这样的替换符,不检测列表符
         */
        public boolean hasFix = false;
        /**
         * 列表符检测,列表元素替换列表,格式'数字.属性'或'数字.'
         */
        public HashList<String> listFix = null;
        /**
         * 条件列表,null表示无条件
         */
        public List<Condition> con = null;
        /**
         * 对应的功能名,null表示无对应的功能
         */
        public String func = null;
        /**
         * 功能对应的子功能,null表示无子功能
         */
        public String data = null;
        /**
         * 物品替换,null表示无,不为null时会将hover提示改为对应的物品信息
         */
        public String item = null;

        public String text;
        public ChatColor color = null;
        public ChatColor[] styles = null;
        public String clickActionName = null;
        public String clickActionData = null;
        public String hoverActionName = null;
        public String hoverActionData = null;
        //如果为null,则hoverActionData不会进行变量替换;如果不为null,则应该与hoverActionData对应
        //hoverActionData的另一种形式的副本
        //格式为'行\n行...'
        public String hoverActionString = null;

        public MessagePart(final String text) {
            this.text = text;
        }

        private MessagePart(boolean hasFix, HashList<String> listFix, List<Condition> con, String func, String data, String item, String text, ChatColor color,
                            ChatColor[] styles, String clickActionName,
                            String clickActionData, String hoverActionName,
                            String hoverActionData, String hoverActionString) {
            super();
            this.hasFix = hasFix;
            this.listFix = listFix;
            this.con = con;
            this.func = func;
            this.data = data;
            this.item = item;
            this.text = text;
            this.color = color;
            this.styles = styles;
            this.clickActionName = clickActionName;
            this.clickActionData = clickActionData;
            this.hoverActionName = hoverActionName;
            this.hoverActionData = hoverActionData;
            this.hoverActionString = hoverActionString;
        }

        /**
         * 获取点击显示名
         * @return cmd,file,url,suggest
         */
        public String getClickName() {
            if (clickActionName == null) return null;
            if (clickActionName.equals("run_command")) return "cmd";
            else if (clickActionName.equals("open_file")) return "file";
            else if (clickActionName.equals("open_url")) return "url";
            else return "suggest";
        }

        /**
         * 获取点击类型的实际名字
         * @param s cmd,file,url,suggest,不为null
         * @return 实际名字,mc可以识别处理的,异常返回null
         */
        public static String getClickName(String s) {
            if (s.equalsIgnoreCase("cmd")) return "run_command";
            else if (s.equalsIgnoreCase("file")) return "open_file";
            else if (s.equalsIgnoreCase("url")) return "open_url";
            else if (s.equalsIgnoreCase("suggest")) return "suggest_command";
            else return null;
        }

        /**
         * 获取格式字符串
         * @return 如'ln',空则返回''
         */
        public String getFormats() {
            if (styles == null) return "";
            String result = "";
            for (ChatColor cc:styles) result += cc.getChar();
            return result;
        }

        /**
         * 获取颜色字符串
         * @return 如'e',空则返回''
         */
        public String getColor() {
            if (color == null) return "";
            else return String.valueOf(color.getChar());
        }

        public JSONWriter writeJson(final JSONWriter json) throws JSONException {
            json.object().key("text").value(text);
            if (color != null) {
                json.key("color").value(color.name().toLowerCase());
            }
            if (styles != null) {
                for (final ChatColor style : styles) {
                    json.key(style == ChatColor.UNDERLINE ? "underlined" : style.name().toLowerCase()).value(true);
                }
            }
            if (clickActionName != null && clickActionData != null) {
                json.key("clickEvent")
                        .object()
                        .key("action").value(clickActionName)
                        .key("value").value(clickActionData)
                        .endObject();
            }
            if (hoverActionName != null && hoverActionData != null) {
                json.key("hoverEvent")
                        .object()
                        .key("action").value(hoverActionName)
                        .key("value").value(hoverActionData)
                        .endObject();
            }
            return json.endObject();
        }

        @Override
        public MessagePart clone() {
            //listFix
            HashList<String> listFix;
            if (this.listFix != null) {
                listFix = new HashListImpl<String>();
                for (String s:this.listFix) listFix.add(s);
            }else listFix = null;
            //con
            List<Condition> con;
            if (this.con != null) {
                con = new ArrayList<Condition>();
                for (Condition c:this.con) con.add(c.clone());
            }else con = null;
            //color
            ChatColor color;
            if (this.color != null) color = ChatColor.getByChar(this.color.getChar());
            else color = null;
            //styles
            ChatColor[] styles;
            if (this.styles != null) styles = this.styles.clone();
            else styles = null;
            //新建
            MessagePart mp = new MessagePart(hasFix, listFix, con, func, data, item, text, color, styles,
                    clickActionName, clickActionData, hoverActionName, hoverActionData, hoverActionString);
            return mp;
        }

        public void save(YamlConfiguration config, String path) {
            //text
            config.set(path+".text", text);
            //con
            if (con != null) {
                String conString = "";
                boolean first = true;
                for (Condition condition : con) {
                    if (first) first = false;
                    else conString += " && ";
                    conString += condition.saveToString();
                }
                config.set(path + ".con", conString);
            }
            //func
            if (func != null) {
                String result = func;
                if (data != null) result += (":"+data);
                config.set(path + ".func", result);
            }
            //color
            if (color != null) config.set(path + ".color", color.getChar());
            //formats
            if (styles != null) {
                String formats = "";
                for (ChatColor cc:styles) formats += cc.getChar();
                config.set(path + ".formats", formats);
            }
            //onClick
            if (clickActionData != null) {
                String onClick = "";
                if (clickActionName.equalsIgnoreCase("open_file")) {
                    onClick += "file ";
                }else if (clickActionName.equalsIgnoreCase("open_url")) {
                    onClick += "url ";
                }else if (clickActionName.equalsIgnoreCase("suggest_command")) {
                    onClick += "suggest ";
                }else if (clickActionName.equalsIgnoreCase("run_command")) {
                    onClick += "cmd ";
                }
                onClick += clickActionData;
                config.set(path + ".onClick", onClick);
            }
            //onHover
            if (hoverActionString != null) config.set(path + ".onHover", hoverActionString);
        }

        /**
         * 检测两个MessagePart是否可以合并<br>
         *     以下情况时,可以合并:<br>
         *     con都为空<br>
         *     func都为空<br>
         *     item都为空<br>
         *     onClick都为空<br>
         *     onHover都为空<br>
         *     listFix都为空<br>
         *     hasFix相同<br>
         *     color相同<br>
         *     formats相同
         * @param mp 检测的MessagePart
         * @return 是否可以进行合并
         */
        public boolean isSame(MessagePart mp) {
            if ((con == null || con.isEmpty()) && (mp.con == null || mp.con.isEmpty())) {
                if ((func == null || func.isEmpty()) && (mp.func == null || mp.func.isEmpty())) {
                    if ((item == null || item.isEmpty()) && (mp.item == null || mp.item.isEmpty())) {
                        if ((clickActionData == null || clickActionData.isEmpty()) &&
                                (mp.clickActionData == null || mp.clickActionData.isEmpty())) {
                            if ((hoverActionData == null || hoverActionData.isEmpty()) &&
                                    (mp.hoverActionData == null || mp.hoverActionData.isEmpty())) {
                                if ((listFix == null || listFix.isEmpty() && (mp.listFix == null || mp.listFix.isEmpty()))) {
                                    //hasFix
                                    if ((hasFix && !mp.hasFix) || (!hasFix && mp.hasFix)) return false;
                                    //color
                                    if (color == null) {
                                        if (mp.color != null) return false;
                                    } else {
                                        if (mp.color == null || !color.equals(mp.color)) return false;
                                    }
                                    //formats
                                    if (styles == null) {
                                        return mp.styles == null;
                                    } else {
                                        if (mp.styles == null) return false;
                                        if (styles.length == mp.styles.length) {
                                            for (ChatColor cc : styles) {
                                                boolean has = false;
                                                for (ChatColor dd : mp.styles) {
                                                    if (cc.equals(dd)) {
                                                        has = true;
                                                        break;
                                                    }
                                                }
                                                if (!has) return false;
                                            }
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        /**
         * 将mp合并进来<br>
         *     (不检测是否可合并)
         * @param mp 要合并进来的MessagePart
         */
        public void combine(MessagePart mp) {
            text += mp.text;
        }

        /**
         * 获取条件的字符串显示
         * @return 条件的字符串显示,空返回''
         */
        public String getConShow() {
            if (con == null) return "";
            String result = "";
            boolean first = true;
            for (Condition condition:con) {
                if (first) first = false;
                else result += " && ";
                result += condition.saveToString();
            }
            return result;
        }

        /**
         * 检测是否为空
         * @return 是否为空
         */
        public boolean isEmpty() {
            return !hasFix && listFix == null && con == null && func == null && data == null && text.isEmpty() &&
                    color == null && styles == null && clickActionName == null && hoverActionName == null;
        }

        /**
         * 从字符串中获取条件
         * @param s 字符串,可为null
         * @return 异常返回null
         */
        public static List<Condition> getCon(String s) {
            List<Condition> result = null;
            if (s != null && !s.isEmpty()) {
                result = new ArrayList<Condition>();
                for (String s0: s.split(" && ")) {
                    try {
                        Condition condition;
                        //读取条件
                        if (s0.indexOf(39) == -1) {//数学比较
                            condition = new MathCompareCondition();
                            if (!condition.loadFromString(s0)) continue;
                        }else {
                            int pos1 = 1;
                            int pos2 = s0.indexOf(39, pos1+1);
                            if (s0.charAt(pos2+2) != '=') {//字符串包含
                                condition = new StringHasCondition();
                                if (!condition.loadFromString(s0)) continue;
                            }else {//字符串比较
                                condition = new StringCompareCondition();
                                if (!condition.loadFromString(s0)) continue;
                            }
                        }
                        //添加条件
                        result.add(condition);
                    } catch (Exception e) {
                    }
                }
            }
            return result;
        }

        /**
         * 格式转换<br>
         * 把{0},{1}...这样的替换符转换成对应的变量,注意顺序<br>
         * 为了效率,变量数量应当尽量少
         * @param msg 要转换的FancyMessageImpl,不为null
         * @param replace 变量列表,可为空
         */
        public static void convert(FancyMessage msg, Object... replace) {
            if (replace.length == 0) return;
            HashMap<String, Object> hash = new HashMap<String, Object>();
            for (int i=0;i<replace.length;i++) {
                hash.put(""+i, replace[i]);
            }
            convert(msg, hash);
        }

        /**
         * 格式转换<br>
         * 把{名称}这样的替换符转换成相应的变量<br>
         * 为了效率,变量数量应当尽量少
         * @param msg 要转换的FancyMessageImpl,不为null
         * @param replace 名称-值映射表,可为null(注意名称中不包含{},值为null时会用""代替)
         */
        public static void convert(FancyMessage msg, HashMap<String, Object> replace) {
            if (replace == null) return;
            for (MessagePart mp:msg.getMessageParts()) {
                if (mp.hasFix) convert(mp, replace);
            }
        }

        /**
         * 格式转换<br>
         * 把{名称}这样的替换符转换成相应的变量<br>
         * 为了效率,变量数量应当尽量少
         * @param mp 要转换的MessagePart,不为null
         * @param replace 名称-值映射表,可为null(注意名称中不包含{},值为null时会用""代替)
         */
        public static void convert(MessagePart mp, HashMap<String, Object> replace) {
            if (replace == null) return;
            for (String key:replace.keySet()){
                //name,show
                String name = "{"+key+"}";
                Object value = replace.get(key);
                String show;
                if (value == null) show = "";
                else show = String.valueOf(value);
                //text
                mp.text = mp.text.replace(name, show);
                //func
                if (mp.func != null) mp.func = mp.func.replace(name, show);
                if (mp.data != null) mp.data = mp.data.replace(name, show);
                //con
                if (mp.con != null) {
                    for (Condition condition:mp.con) condition.replace(name, show);
                }
                //item
                if (mp.item != null) mp.item = mp.item.replace(name, show);
                //click
                if (mp.clickActionData != null) mp.clickActionData = mp.clickActionData.replace(name, show);
                //hover
                if (mp.hoverActionString != null) {
                    mp.hoverActionString = mp.hoverActionString.replace(name, show);
                    mp.hoverActionData = MessageApi.getHoverActionData(mp.hoverActionString);
                }
            }
        }
    }

    public FancyMessage text(final String text);
	
	public FancyMessage func(final String funcName);

	public FancyMessage data(final String data);
	
	public FancyMessage color(final ChatColor color);

	public FancyMessage style(final ChatColor... styles);
	
	public FancyMessage file(final String path);
	
	public FancyMessage link(final String url);
	
	public FancyMessage suggest(final String command);
	
	public FancyMessage command(final String command);
	
	public FancyMessage itemTooltip(final String itemJSON, final String hoverActionString);
	
	public FancyMessage itemTooltip(final ItemStack itemStack, final String hoverActionString);
	
	public FancyMessage tooltip(final String text, final String hoverActionString);

	public FancyMessage then(final Object obj);
	
	public String toJSONString();
	
	/**
	 * 向玩家发送FancyMessage<br>
	 * 会根据功能func检测更改显示!!!
	 * @param player
	 * @param check 是否检测,false的时候聊天信息不会被检测阻隔
	 */
	public void send(Player player, boolean check);

    public FancyMessage clone();

    public List<MessagePart> getMessageParts();

    /**
     * 获取无格式文本(在没有hover与click的情况下信息内容是一样的)
     * @return
     */
    public String getText();

    /**
     * 优化修正
     */
    public void fix();

    /**
     * 条件显示检测
     */
    public void checkCondition();

    /**
     * 检测更新所有MessagePart的hasFix状态与listFix列表
     */
    public void update();

    public FancyMessage item(String item);

    public FancyMessage con(final List<Condition> con);

    /**
     * 将另一个FancyMessage结合进来
     * @param fm 另一个FancyMessage
     * @param front true表示将另一个fm放前面,false表示将另一个fm放后面
     */
    public void combine(FancyMessage fm, boolean front);
}
