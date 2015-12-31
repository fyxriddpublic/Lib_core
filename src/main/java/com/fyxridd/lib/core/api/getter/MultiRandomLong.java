package com.fyxridd.lib.core.api.getter;

import com.fyxridd.lib.core.api.hashList.ChanceHashList;
import com.fyxridd.lib.core.api.hashList.ChanceHashListImpl;

/**
 * 随机长整数分段获取器
 */
public class MultiRandomLong implements RandomGetter{
    private ChanceHashList<RandomLong> chances = new ChanceHashListImpl<>();

    /**
     * @param data '[<概率>:]<RandomLong>[,MultiRandomLong]',概率>=0
     */
    public MultiRandomLong(String data) {
        String[] args = data.split(",", 2);
        //chances
        {
            String[] args2 = args[0].split(":", 2);
            if (args2.length == 1) chances.addChance(new RandomLong(args2[0]), 1);
            else chances.addChance(new RandomLong(args2[1]), Integer.parseInt(args2[0]));
        }
        //next
        if (args.length > 1) chances.convert(new MultiRandomLong(args[1]).chances, false);
    }

    /**
     * @param extra 此处无法加成
     * @return >=0的整数
     */
    @Override
    public Object get(int extra) {
        return chances.getRandom().get(extra);
    }
}
