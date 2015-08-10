package com.fyxridd.lib.core;

import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.model.InfoUser;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;

public class Info implements Listener {
    //注: 如果一个属性InfoUser需要删除,则没有与InfoUser的data值为null是一样的
    //在这种情况下,会删除数据库中的InfoUser好节省空间,同时缓存中(infoHash)会保留InfoUser(data为null),这样下次不用再去数据库中读取

    //缓存

    //动态读取
    //玩家名 属性名 信息
    private HashMap<String, HashMap<String, InfoUser>> infoHash = new HashMap<>();

    //需要保存的信息列表
    private HashSet<InfoUser> needUpdateList = new HashSet<>();
    //需要删除的信息列表
    private HashSet<InfoUser> needDeleteList = new HashSet<>();

    public Info() {
        //计时器: 更新
        Bukkit.getScheduler().scheduleSyncRepeatingTask(CorePlugin.instance, new Runnable() {
            @Override
            public void run() {
                saveAll();
            }
        }, 308, 308);
    }

    public void onDisable() {
        saveAll();
    }

    /*
     * 获取玩家的属性值
     * @param name 玩家名,不为null
     * @param flag 属性名,不为null
     * @return 属性值,不存在返回null
     */
    public String getInfo(String name, String flag) {
        return get(name, flag).getData();
    }

    /**
     * 设置玩家的属性信息(玩家不存在此属性信息会新建)
     * @param name 玩家名,不为null
     * @param flag 属性名,不为null
     * @param data 属性值,null表示删除属性信息
     */
    public void setInfo(String name, String flag, String data) {
        InfoUser info = get(name, flag);
        //一样的
        if (data == null) {
            if (info.getData() == null) return;
        }else {
            if (data.equals(info.getData())) return;
        }
        //设置
        info.setData(data);
        //更新
        if (data == null) {
            needUpdateList.remove(info);
            needDeleteList.add(info);
        }else {
            needUpdateList.add(info);
            needDeleteList.remove(info);
        }
    }

    /**
     * 获取玩家的属性信息(先从缓存中读取,没有再从数据库中读取并保存缓存)
     * @param name 玩家名,不为null
     * @param flag 属性名,不为null
     * @return 不为null(返回的属性信息勿修改,修改请调用set方法)
     */
    private InfoUser get(String name, String flag) {
        //数据
        HashMap<String, InfoUser> hash = infoHash.get(name);
        if (hash == null) {
            hash = new HashMap<>();
            infoHash.put(name, hash);
        }

        //先从缓存读取
        InfoUser info = hash.get(flag);
        if (info != null) return info;

        //再从数据库中读取
        info = Dao.getInfo(name, flag);
        if (info == null) info = new InfoUser(name, flag, null);//null时不需要保存到数据库
        //保存缓存
        hash.put(flag, info);

        //返回
        return info;
    }

    /**
     * 更新
     */
    private void saveAll() {
        //保存
        if (!needUpdateList.isEmpty()) {
            CoreMain.dao.saveOrUpdates(needUpdateList);
            needUpdateList.clear();
        }
        //删除
        if (!needDeleteList.isEmpty()) {
            CoreMain.dao.deletes(needDeleteList);
            needDeleteList.clear();
        }
    }
}
