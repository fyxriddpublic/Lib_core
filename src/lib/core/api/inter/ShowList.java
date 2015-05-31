package lib.core.api.inter;

import java.util.List;

/**
 * 显示用的列表类
 */
public interface ShowList<T extends Object> {
    /**
     * 获取指定页的对象列表
     * @param pageSize 分页大小,>=0,0时返回空列表
     * @param page 指定页,页面从1开始
     * @return 对象列表,异常返回空列表
     */
    public List<T> getPage(int pageSize, int page);

    /**
     * 获取最大页面数
     * @param pageSize 分页大小,<=0时返回0
     * @return 最大页面数,0表示无元素,有元素则最小页面数为1
     */
    public int getMaxPage(int pageSize);

    public Class getClassType();
}
