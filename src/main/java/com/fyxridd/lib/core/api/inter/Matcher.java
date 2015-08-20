package com.fyxridd.lib.core.api.inter;

/**
 * 字符串匹配
 */
public interface Matcher {
    /**
     * 检测是否匹配
     * @param str 要检测的字符串
     * @return 是否匹配成功
     */
    public boolean check(String str);
}
