package com.fyxridd.lib.core.api.getter;

import com.fyxridd.lib.core.api.CoreApi;
import com.fyxridd.lib.core.api.MathApi;

/**
 * 随机整数获取器
 * 可以是任意整数
 */
public class RandomInt implements RandomGetter{
    private int min, max;

    /**
     * @param data 'min整数->max整数'或'整数',其中max>=min
     */
    public RandomInt(String data) {
        if (data.contains("->")) {
            String[] args = data.split("\\->");
            min = Integer.parseInt(args[0]);
            max = Integer.parseInt(args[1]);
        }else {
            min = Integer.parseInt(data);
            max = min;
        }
    }

    /**
     * @param extra 此处可以加成
     * @return >=0的整数
     */
    @Override
    public Object get(int extra) {
        int result = MathApi.nextInt(min, max);
        if (extra > 0) {
            //检测加成
            long tmp = (long)result*(100+extra);
            result = (int)(tmp/100);
            if (CoreApi.Random.nextInt(100) < (int)(tmp%100)) result++;
            //检测上限
            result = Math.min(result, max);
        }
        return result;
    }
}
