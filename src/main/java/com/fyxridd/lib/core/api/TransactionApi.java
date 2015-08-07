package com.fyxridd.lib.core.api;

import com.fyxridd.lib.core.api.inter.TipTransaction;
import com.fyxridd.lib.core.api.inter.TransactionUser;
import com.fyxridd.lib.core.transaction.TransactionManager;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import com.fyxridd.lib.core.transaction.TipTransactionImpl;

import java.util.HashMap;
import java.util.List;

public class TransactionApi {
    /**
     * @see com.fyxridd.lib.core.transaction.TransactionManager#getTransactionUser(String)
     */
    public static TransactionUser getTransactionUser(String name) {
        return TransactionManager.getTransactionUser(name);
    }

    /**
     * @see TransactionManager#delTransaction(String)
     */
    public static void delTransaction(String name) {
        TransactionManager.delTransaction(name);
    }

    /**
     * @see #newTipTransaction(boolean, String, long, int, String, java.util.List, java.util.HashMap, java.util.HashMap, String)
     */
    public static TipTransaction newTipTransaction(boolean instant, String name, long last, int tipInterval, String cmd,
                                                   List<FancyMessage> tip, HashMap<String, Object> map, String key){
        return new TipTransactionImpl(instant, name, last, tipInterval, cmd, tip, map, key);
    }

    /**
     * 创建新的提示事务<br>
     * 会更新值到tip显示里,但不会进行显示,需要调用updateShow()方法
     * @param instant 是否输入后自动确认提交
     * @param name 玩家名
     * @param last 持续时间
     * @param tipInterval 提示间隔
     * @param cmd 命令,可为null
     * @param tip 提示列表
     * @param map 名-值映射表,可为null,值可以为null或空,但名必须全
     * @param recommend 名-推荐值列表,可为null或空,名可以不全
     * @param key 正在修改的名,可为null
     */
    public static TipTransaction newTipTransaction(boolean instant, String name, long last, int tipInterval, String cmd,
                                                   List<FancyMessage> tip, HashMap<String, Object> map,
                                                   HashMap<String, List<Object>> recommend, String key) {
        return new TipTransactionImpl(instant, name, last, tipInterval, cmd, tip, map, recommend, key);
    }
}
