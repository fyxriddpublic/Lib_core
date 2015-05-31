package lib.core.api.inter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 页面配置
 */
public interface Page {
    /**
     * 页面上下文
     */
    public static class PageContext {
        //页面编号,>=1
        private int num;
        //页面描述,如"5 10 15"
        private String text;
        //页面内容,行号
        private List<Integer> content;

        public PageContext(int num, String text, List<Integer> content) {
            this.num = num;
            this.text = text;
            this.content = content;
        }

        public int getNum() {
            return num;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<Integer> getContent() {
            return content;
        }
    }

    /**
     * 行上下文
     */
    public static class LineContext {
        //行号,>=1
        private int num;
        //行信息
        private FancyMessage msg;

        public LineContext(int num, FancyMessage msg) {
            this.num = num;
            this.msg = msg;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public FancyMessage getMsg() {
            return msg;
        }

        public void setMsg(FancyMessage msg) {
            this.msg = msg;
        }

        public String getText() {
            return msg.getText();
        }
    }

    /**
     * 额外键值获取信息
     */
    public static class MapInfo {
        private String keyName;
        private String plugin, key;

        public MapInfo(String keyName, String plugin, String key) {
            this.keyName = keyName;
            this.plugin = plugin;
            this.key = key;
        }

        public String getKeyName() {
            return keyName;
        }

        public String getPlugin() {
            return plugin;
        }

        public String getKey() {
            return key;
        }
    }

    public int getPageMax();

    public boolean isRefresh();

    public List<PageContext> getPageList();

    public void setPageMax(int pageMax);

    public void setRefresh(boolean refresh);

    public String getPlugin();

    public String getPage();

    public int getListSize();

    public void setListSize(int listSize);

    public LinkedHashMap<Integer, LineContext> getLines();

    public void setLines(LinkedHashMap<Integer, LineContext> lines);

    public HashMap<String, MapInfo> getMaps();
}
