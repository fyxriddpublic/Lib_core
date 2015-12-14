package com.fyxridd.lib.core.api.getter;

import com.fyxridd.lib.core.api.CoreApi;

/**
 * 随机非负整数获取器(>=0)
 */
public class RandomInt implements RandomGetter{
    private int min, max;

    /**
     * @param data 'min整数-max整数'或'整数',其中max>=min>=0
     */
    public RandomInt(String data) {
        if (data.contains("-")) {
            min = Integer.parseInt(data.split("\\-")[0]);
            max = Integer.parseInt(data.split("\\-")[1]);
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
        int result = CoreApi.Random.nextInt(max-min+1)+min;
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
