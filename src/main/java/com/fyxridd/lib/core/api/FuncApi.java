package com.fyxridd.lib.core.api;

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
}
