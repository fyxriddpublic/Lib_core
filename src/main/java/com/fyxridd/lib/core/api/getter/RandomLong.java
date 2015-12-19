package com.fyxridd.lib.core.api.getter;

import com.fyxridd.lib.core.api.MathApi;

/**
 * 随机长整数获取器
 * 可以是任意长整数,abs(max) < Long最大值/100,abs(min) < Long最大值/100
 */
public class RandomLong implements RandomGetter{
    private long min, max;

    /**
     * @param data 'min长整数->max长整数'或'长整数',其中max>=min
     */
    public RandomLong(String data) {
        if (data.contains("->")) {
            String[] args = data.split("\\->");
            min = Long.parseLong(args[0]);
            max = Long.parseLong(args[1]);
        }else {
            min = Long.parseLong(data);
            max = min;
        }
    }

    /**
     * @param extra 此处可以加成
     * @return >=0的整数
     */
    @Override
    public Object get(int extra) {
        long result = MathApi.nextLong(min, max);
        if (extra > 0) {
            //检测加成
            long tmp = result*(100+extra);
            result = tmp/100;
            if (MathApi.nextInt(0, 99) < (int)(tmp%100)) result++;
            //检测上限
            result = Math.min(result, max);
        }
        return result;
    }
}
