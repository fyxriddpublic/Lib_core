package com.fyxridd.lib.core.per;

import com.fyxridd.lib.core.api.hashList.HashList;
import com.fyxridd.lib.core.api.hashList.HashListImpl;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public class PerUser {
    //玩家名
    private String name;

    //玩家拥有的权限组列表,可为空不为null
    private HashList<String> groups;

    //玩家拥有的权限列表,可为空不为null
    private HashList<String> pers;

    public PerUser() {
    }

    public PerUser(String name, HashList<String> groups, HashList<String> pers) {
        this.name = name;
        this.groups = groups;
        this.pers = pers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashList<String> getGroups() {
        return groups;
    }

    public void setGroups(HashList<String> groups) {
        this.groups = groups;
    }

    public HashList<String> getPers() {
        return pers;
    }

    public void setPers(HashList<String> pers) {
        this.pers = pers;
    }

    /**
     * 从配置文件内读取玩家权限信息
     * @param name 玩家名,不为null
     * @param config 配置文件,可为null
     * @return 玩家权限信息,异常返回null
     */
    public static PerUser load(String name, YamlConfiguration config) {
        if (config == null) return null;
        try {
            //groups
            HashList<String> groups = new HashListImpl<String>();
            List<String> list = config.getStringList("groups");
            if (list != null) {
                for (String s : list) groups.add(s);
            }
            //pers
            HashList<String> pers = new HashListImpl<String>();
            list = config.getStringList("pers");
            if (list != null) {
                for (String s : list) pers.add(s);
            }
            PerUser pu = new PerUser(name, groups, pers);
            return pu;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 保存玩家的权限信息到文件
     * @param config 保存到的配置文件,不为null
     * @param pu 权限信息,不为null
     */
    public static void save(YamlConfiguration config, PerUser pu) {
        //groups
        List<String> list = new ArrayList<String>();
        for (String s:pu.getGroups()) list.add(s);
        config.set("groups", list);
        //pers
        list = new ArrayList<String>();
        for (String s:pu.getPers()) list.add(s);
        config.set("pers", list);
    }
}
