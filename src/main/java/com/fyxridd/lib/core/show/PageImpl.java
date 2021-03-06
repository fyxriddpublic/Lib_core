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

    //是否生效
    private boolean enable;

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

    //显示页面需要的权限
    private String per;

    //是否补空行
    private boolean fillEmpty;

    //是否显示页面尾部的操作提示(2行)
    private boolean handleTip;

    //是否记录页面信息(可以返回与刷新)
    private boolean record;

    /**
     * 列表信息
     */
    private ListInfo listInfo;

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
    public PageImpl(String plugin, String page, boolean enable, int pageMax, int listSize, boolean refresh, String per, boolean fillEmpty, boolean handleTip, boolean record, ListInfo listInfo, HashMap<String, MapInfo> maps,
                    List<PageContext> pageList, LinkedHashMap<Integer, LineContext> lines) {
        this.plugin = plugin;
        this.page = page;
        this.enable = enable;
        this.pageMax = pageMax;
        this.listSize = listSize;
        this.refresh = refresh;
        this.per = per;
        this.fillEmpty = fillEmpty;
        this.handleTip = handleTip;
        this.record = record;
        this.listInfo = listInfo;
        this.maps = maps;
        this.pageList = pageList;
        this.lines = lines;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
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

    public String getPer() {
        return per;
    }

    public void setPer(String per) {
        this.per = per;
    }

    public boolean isFillEmpty() {
        return fillEmpty;
    }

    public void setFillEmpty(boolean fillEmpty) {
        this.fillEmpty = fillEmpty;
    }

    public boolean isHandleTip() {
        return handleTip;
    }

    public void setHandleTip(boolean handleTip) {
        this.handleTip = handleTip;
    }

    public boolean isRecord() {
        return record;
    }

    public void setRecord(boolean record) {
        this.record = record;
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

    public ListInfo getListInfo() {
        return listInfo;
    }

    public HashMap<String, MapInfo> getMaps() {
        return maps;
    }
}
