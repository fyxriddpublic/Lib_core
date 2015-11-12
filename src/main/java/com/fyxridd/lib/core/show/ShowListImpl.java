package com.fyxridd.lib.core.show;

import com.fyxridd.lib.core.api.CoreApi;
import com.fyxridd.lib.core.api.inter.ShowList;

import java.util.List;

/**
 * 显示用的列表类
 */
public class ShowListImpl<T> implements ShowList<T> {
    /**
     * 传入的列表类型:<br>
     * 0:  指List类型<br>
     * 1:  指Object[]类型
     * 2:  指Collection类型
     * 3:  指HashList类型
     */
    private int type;

    /**
     * 列表,可为null
     */
    private Object list;

    /**
     * null表示使用元素自带类名
     */
    private Class classType;

    public ShowListImpl(int type, Object list) {
        this.type = type;
        this.list = list;
    }

    public ShowListImpl(int type, Object list, Class classType) {
        this.type = type;
        this.list = list;
        this.classType = classType;
    }

    /**
     * 获取指定页的对象列表
     * @param pageSize 分页大小,>=0,0时返回空列表
     * @param page 指定页,页面从1开始
     * @return 对象列表,异常返回空列表
     */
    public List<T> getPage(int pageSize, int page) {
        return CoreApi.getPage(list, type, pageSize, page);
    }

    /**
     * 获取最大页面数
     * @param pageSize 分页大小,<=0时返回0
     * @return 最大页面数,0表示无元素,有元素则最小页面数为1
     */
    public int getMaxPage(int pageSize) {
        return CoreApi.getMaxPage(CoreApi.getTotal(list, type), pageSize);
    }

    public Class getClassType() {
        return classType;
    }
}
