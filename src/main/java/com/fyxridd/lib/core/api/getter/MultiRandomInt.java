package com.fyxridd.lib.core.api.getter;

import com.fyxridd.lib.core.api.hashList.ChanceHashList;
import com.fyxridd.lib.core.api.hashList.ChanceHashListImpl;

/**
 * 随机整数分段获取器
 */
public class MultiRandomInt implements RandomGetter{
    private ChanceHashList<RandomInt> chances = new ChanceHashListImpl<>();

    /**
     * @param data '<概率>:<RandomInt>[,MultiRandomInt]',概率>=0
     */
    public MultiRandomInt(String data) {
        String[] args = data.split(",", 2);
        //chances
        {
            String[] args2 = args[0].split(":");
            chances.addChance(new RandomInt(args2[1]), Integer.parseInt(args2[0]));
        }
        //next
        if (args.length > 1) {
            MultiRandomInt next = new MultiRandomInt(args[1]);
            chances.convert(next.chances, false);
        }
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
