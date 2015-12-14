package com.fyxridd.lib.core.api.getter;

/**
 * 随机获取器
 */
public interface RandomGetter {
    /**
     * 获取值(可能随机获取,即每次获取到的都不一样)
     * @param extra 加成比例,0-100(但加成不会超过随机范围的上限)
     */
    public Object get(int extra);
}
