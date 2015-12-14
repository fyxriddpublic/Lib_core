package com.fyxridd.lib.core.api.getter;

import com.fyxridd.lib.core.api.CoreApi;

/**
 * 随机非负实数获取器(>=0.0)
 */
public class RandomDouble implements RandomGetter{
    private long minLong, maxLong;

    /**
     * @param data 'min实数-max实数'或'实数',其中max>=min>=0,且max<Long型值上限/100
     */
    public RandomDouble(String data) {
        double min,max;
        if (data.contains("-")) {
            min = Double.parseDouble(data.split("\\-")[0]);
            max = Double.parseDouble(data.split("\\-")[1]);
        }else {
            min = Double.parseDouble(data);
            max = min;
        }

        //数值过大时可能溢出!
        minLong = (long) (min*100);
        maxLong = (long) (max*100);
    }

    /**
     * @param extra 此处可以加成
     * @return >=0的实数
     */
    @Override
    public Object get(int extra) {
        double result = (double)(CoreApi.nextLong(maxLong-minLong+1)+minLong)/100;
        if (extra > 0) result = Math.min(result*(100+extra)/100,(double)maxLong/100);
        if (result < 0) result = 0;
        return result;
    }
}
