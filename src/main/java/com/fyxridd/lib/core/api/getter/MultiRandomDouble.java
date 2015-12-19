package com.fyxridd.lib.core.api.getter;

import com.fyxridd.lib.core.api.hashList.ChanceHashList;
import com.fyxridd.lib.core.api.hashList.ChanceHashListImpl;

/**
 * 随机实数分段获取器
 */
public class MultiRandomDouble implements RandomGetter{
    private ChanceHashList<RandomDouble> chances = new ChanceHashListImpl<>();

    /**
     * @param data '<概率>:<RandomDouble>[,MultiRandomDouble]',概率>=0
     */
    public MultiRandomDouble(String data) {
        String[] args = data.split(",", 2);
        //chances
        {
            String[] args2 = args[0].split(":");
            chances.addChance(new RandomDouble(args2[1]), Integer.parseInt(args2[0]));
        }
        //next
        if (args.length > 1) {
            chances.convert(new MultiRandomDouble(args[1]).getChances(), false);
        }
    }

    public ChanceHashList<RandomDouble> getChances() {
        return chances;
    }

    /**
     * @param extra 此处无法加成
     * @return >=0的实数
     */
    @Override
    public Object get(int extra) {
        return chances.getRandom().get(extra);
    }
}
