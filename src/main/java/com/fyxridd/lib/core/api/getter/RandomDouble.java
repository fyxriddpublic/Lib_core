package com.fyxridd.lib.core.api.getter;

import com.fyxridd.lib.core.api.MathApi;

/**
 * 随机实数获取器
 * 可以是任意实数
 */
public class RandomDouble implements RandomGetter{
    private long minLong, maxLong;

    /**
     * @param data 'min实数->max实数'或'实数',其中max>=min,且abs(max)<Long型值上限/100,abs(min)<Long型值上限/100
     */
    public RandomDouble(String data) {
        double min,max;
        if (data.contains("->")) {
            String[] args = data.split("\\->");
            min = Double.parseDouble(args[0]);
            max = Double.parseDouble(args[1]);
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
        double result = (double)(MathApi.nextLong(minLong, maxLong))/100;
        if (extra > 0) result = Math.min(result*(100+extra)/100,(double)maxLong/100);
        if (result < 0) result = 0;
        return result;
    }
}
