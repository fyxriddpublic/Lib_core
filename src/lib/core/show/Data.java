package lib.core.show;

import lib.core.api.inter.ShowList;

import java.util.HashMap;

/**
 * 功能调用显示页面时需要传入的数据
 */
public class Data {
    /**
     * 当前页
     */
    private int pageNow;
    /**
     * 列表分页大小,列表当前页
     */
    private int listSize, listNow;
    /**
     * 列表
     */
    private ShowList list;
    /**
     * 名称-值的映射表<br>
     * 值会调用toString()方法转换成字符串,<br>
     * 然后替换相应{名称}的替换符
     */
    private HashMap<String, Object> data;
}
