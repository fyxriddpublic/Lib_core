package com.fyxridd.lib.core.api;

import com.fyxridd.lib.core.CoreMain;
import com.fyxridd.lib.core.api.inter.FunctionInterface;
import com.fyxridd.lib.core.FuncManager;

public class FuncApi {
    /**
     * 注册功能
     * @param func 功能,可为null(null时返回false)
     * @return 是否注册成功,功能名被占用或异常时注册失败
     */
    public static boolean register(FunctionInterface func) {
        return FuncManager.register(func);
    }

    /**
     * 转换操作内容为(玩家可执行的)命令
     * @param funcName 功能名
     * @param arg 操作内容(null时返回null)
     * @return 玩家可执行的命令,异常返回null
     */
    public static String convert(String funcName, String arg) {
        return CoreMain.funcManager.convert(funcName, arg);
    }

    /**
     * @see #convert(String, String)
     */
    public static String convert(String funcName, String[] args) {
        return CoreMain.funcManager.convert(funcName, args);
    }
}
