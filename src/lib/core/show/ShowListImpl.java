package lib.core.show;

import lib.core.api.hashList.HashList;
import lib.core.api.inter.ShowList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 显示用的列表类
 */
public class ShowListImpl<T extends Object> implements ShowList<T>{
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
        List<T> result = new ArrayList<T>();
        if (list == null) return result;
        if (pageSize == 0) return result;
        int maxPage = getMaxPage(pageSize);
        if (page >= 1 && page <= maxPage) {
            int begin = (page-1)*pageSize;
            int end = (page == maxPage)?getListSize():page*pageSize;
            switch (type) {
                case 0:
                    List<T> list = (List<T>)this.list;
                    for (int i=begin;i<end;i++) {
                        result.add(list.get(i));
                    }
                    break;
                case 1:
                    T[] array = (T[])this.list;
                    for (int i=begin;i<end;i++) {
                        result.add(array[i]);
                    }
                    break;
                case 2:
                    Collection<T> collection = (Collection<T>)this.list;
                    int index = 0;
                    for (T t:collection) {
                        if (index > end) break;//结束
                        if (index >= begin) result.add(t);
                        index ++;
                    }
                    break;
                case 3:
                    HashList<T> hashList = (HashList<T>) this.list;
                    result = hashList.getPage(page, pageSize);
                    break;
            }
        }
        return result;
    }

    /**
     * 获取最大页面数
     * @param pageSize 分页大小,<=0时返回0
     * @return 最大页面数,0表示无元素,有元素则最小页面数为1
     */
    public int getMaxPage(int pageSize) {
        if (list == null) return 0;
        if (pageSize <= 0) return 0;
        int listSize = getListSize();
        if (listSize%pageSize == 0) return listSize/pageSize;
        return listSize/pageSize+1;
    }

    public Class getClassType() {
        return classType;
    }

    /**
     * 获取列表大小
     * @return 列表大小,异常返回0
     */
    private int getListSize() {
        if (list == null) return 0;
        int result = 0;
        switch (type) {
            case 0:
                result = ((List<T>)list).size();
                break;
            case 1:
                result = ((T[])list).length;
                break;
            case 2:
                result = ((Collection<T>)list).size();
                break;
            case 3:
                result = ((HashList<T>)list).size();
                break;
        }
        return result;
    }
}
