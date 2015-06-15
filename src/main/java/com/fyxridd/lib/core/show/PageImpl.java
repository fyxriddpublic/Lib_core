package com.fyxridd.lib.core.show;

import com.fyxridd.lib.core.api.inter.Page;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 页面配置
 */
public class PageImpl implements Page {
    /**
     * 页面所属插件名
     */
    private String plugin;

    /**
     * 页面名
     */
    private String page;

    /**
     * 最大页面,>=0
     */
    private int pageMax;

    /**
     * 列表大小,>=0
     */
    private int listSize;

    /**
     * 页面跳转是否刷新
     */
    private boolean refresh;

    /**
     * 额外键值获取信息
     */
    private HashMap<String, MapInfo> maps;

    /**
     * 页面列表
     */
    private List<PageContext> pageList;

    /**
     * 行号 行上下文
     */
    private LinkedHashMap<Integer, LineContext> lines;

    /**
     * @param pageMax  >0
     * @param pageList 不为null
     */
    public PageImpl(String plugin, String page, int pageMax, int listSize, boolean refresh, HashMap<String, MapInfo> maps,
                    List<PageContext> pageList, LinkedHashMap<Integer, LineContext> lines) {
        this.plugin = plugin;
        this.page = page;
        this.pageMax = pageMax;
        this.listSize = listSize;
        this.refresh = refresh;
        this.maps = maps;
        this.pageList = pageList;
        this.lines = lines;
    }

    public int getPageMax() {
        return pageMax;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public List<PageContext> getPageList() {
        return pageList;
    }

    public void setPageMax(int pageMax) {
        this.pageMax = pageMax;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public String getPlugin() {
        return plugin;
    }

    public String getPage() {
        return page;
    }

    public int getListSize() {
        return listSize;
    }

    public void setListSize(int listSize) {
        this.listSize = listSize;
    }

    public LinkedHashMap<Integer, LineContext> getLines() {
        return lines;
    }

    public void setLines(LinkedHashMap<Integer, LineContext> lines) {
        this.lines = lines;
    }

    public HashMap<String, MapInfo> getMaps() {
        return maps;
    }
}
