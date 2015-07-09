package com.fyxridd.lib.core.per;

import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CoreApi;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.event.TimeEvent;
import com.fyxridd.lib.core.api.hashList.HashListImpl;
import com.fyxridd.lib.core.api.hashList.HashList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class Per implements Listener,PerHandler {
    //默认权限组名
    private static final String DEFAULT_GROUP = "default";

    //权限文件夹(per)路径
    private static String path;

    //默认权限组,所有玩家都有的
    private static Group defaultGroup;
    //权限组名 权限组
    //不包括默认权限组
    private static HashMap<String, Group> groupHash;

    //优化策略
    //玩家名 权限信息
    //动态读取
    private static HashMap<String, PerUser> userHash = new HashMap<String, PerUser>();

    //优化策略
    //需要保存的玩家列表
    private static HashList<String> needSaveList = new HashListImpl<String>();

    //优化策略
    //玩家最近一次保存的时间点
    private static HashMap<String, Long> lastSaveHash = new HashMap<String, Long>();

    //优化策略
    //玩家名 拥有的权限列表
    //动态读取
    private static HashMap<String, HashList<String>> persHash = new HashMap<String, HashList<String>>();

    //优化策略
    //玩家最近一次操作的时间点
    private static HashMap<String, Long> lastHandleHash = new HashMap<String, Long>();

    //玩家两次文件保存的最小间隔,单位毫秒
    private static int saveInterval = 1000;
    //为了节省内存,玩家删除缓存的最大间隔,单位毫秒
    private static int removeTime = 600000;

    public Per() {
        path = CorePlugin.dataPath+ File.separator+"per";
        //读取配置文件
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
    }

    public void onDisable() {
        saveAll();
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        checkInit(e.getPlayer().getName());
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        PerUser pu = checkInit(e.getPlayer().getName());
        if (pu != null) save(pu, true);
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onTime(TimeEvent e) {
        if (TimeEvent.getTime()%100 == 0) {
            long now = System.currentTimeMillis();
            Iterator<String> it = lastHandleHash.keySet().iterator();
            while (it.hasNext()) {
                String name = it.next();
                if (lastHandleHash.containsKey(name)) {
                    long pre = lastHandleHash.get(name);
                    if (now - pre > removeTime) {//删除缓存
                        PerUser pu = userHash.get(name);
                        if (pu != null) save(pu, true);
                        userHash.remove(name);
                        persHash.remove(name);
                        it.remove();
                    }
                }
            }
        }
    }

    @Override
	public boolean has(Player p, String per) {
        if (per == null || per.isEmpty()) return true;

        return has(p.getName(), per);
	}

    @Override
	public boolean has(String name, String per) {
        if (per == null || per.isEmpty()) return true;

        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //优化策略
        lastHandleHash.put(name, System.currentTimeMillis());
        if (!persHash.containsKey(name)) updatePers(name);
        return persHash.get(name).has(per);
	}

    @Override
	public boolean add(Player p, String per) {
		return add(p.getName(), per);
	}

    @Override
	public boolean add(String name, String per) {
        if (per == null || per.isEmpty()) return false;
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //
        PerUser pu = checkInit(name);
        if (pu == null) return false;//异常
        if (pu.getPers().has(per)) return false;
        pu.getPers().add(per);
        //优化策略
        //更新persHash
        updatePers(name);
        //保存
        needSaveList.add(pu.getName());
        save(pu, false);
		return true;
	}

    @Override
	public boolean del(Player p, String per) {
        return del(p.getName(), per);
	}

    @Override
    public boolean del(String name, String per) {
        if (per == null || per.isEmpty()) return false;
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //
        PerUser pu = checkInit(name);
        if (pu == null) return false;//异常
        if (!pu.getPers().has(per)) return false;
        pu.getPers().remove(per);
        //优化策略
        //更新persHash
        updatePers(name);
        //保存
        needSaveList.add(pu.getName());
        save(pu, false);
        return true;
    }

    @Override
	public boolean hasGroup(Player p,String groupName, boolean loop) {
        return hasGroup(p.getName(), groupName, loop);
	}

    @Override
    public boolean hasGroup(String name, String groupName, boolean loop) {
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //异常
        PerUser pu = checkInit(name);
        if (pu == null) return false;
        //权限组不存在
        Group group = groupHash.get(groupName);
        if (group == null) return false;
        //检测
        if (loop) {
            if (pu.getGroups().has(groupName)) return true;
            for (String s:pu.getGroups()) {
                if (checkHasGroup(s, groupName)) return true;
            }
            return false;
        }else return pu.getGroups().has(groupName);
    }

    @Override
	public boolean addGroup(Player p,String groupName) {
		return addGroup(p.getName(), groupName);
	}

    @Override
    public boolean addGroup(String name, String groupName) {
        if (groupName == null || groupName.isEmpty()) return false;

        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //异常
        PerUser pu = checkInit(name);
        if (pu == null) return false;
        //权限组不存在
        Group group = groupHash.get(groupName);
        if (group == null) return false;
        //已经有此权限组
        if (pu.getGroups().has(groupName)) return false;
        //添加权限组成功
        pu.getGroups().add(groupName);
        //优化策略
        //更新persHash
        updatePers(name);
        //保存
        needSaveList.add(pu.getName());
        save(pu, false);
        return true;
    }

    @Override
    public boolean delGroup(Player p,String groupName) {
        return delGroup(p.getName(), groupName);
    }

    @Override
    public boolean delGroup(String name, String groupName) {
        if (groupName == null || groupName.isEmpty()) return false;

        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //异常
        PerUser pu = checkInit(name);
        if (pu == null) return false;
        //权限组不存在
        Group group = groupHash.get(groupName);
        if (group == null) return false;
        //没有此权限组
        if (!pu.getGroups().has(groupName)) return false;
        //删除权限组成功
        pu.getGroups().remove(groupName);
        //优化策略
        //更新persHash
        updatePers(name);
        //保存
        needSaveList.add(pu.getName());
        save(pu, false);
        return true;
    }

    @Override
    public boolean checkHasGroup(String tar, String groupName) {
        Group group = groupHash.get(tar);
        if (group == null) return false;
        if (group.getInherits().has(groupName)) return true;
        for (String s:group.getInherits()) {
            if (checkHasGroup(s, groupName)) return true;
        }
        return false;
    }

    /**
     * 优化策略<br>
     * (根据默认权限组与PerUser)更新玩家的权限信息<br>
     *     更新过后persHash中必然包含玩家
     * @param name 玩家名,不为null
     */
    private static void updatePers(String name) {
        HashList<String> list = new HashListImpl<String>();
        //读取
        PerUser pu = checkInit(name);
        if (pu != null) {
            //默认权限组
            checkAdd(list, DEFAULT_GROUP);
            //本身权限
            for (String per:pu.getPers()) list.add(per);
            //本身权限组
            for (String group:pu.getGroups()) checkAdd(list, group);
        }
        //设置
        persHash.put(name, list);
    }

    /**
     * 将权限组内的所有权限加入列表
     * @param list 列表,不为null
     * @param groupName 权限组名,不为null
     */
    private static void checkAdd(HashList<String> list, String groupName) {
        Group group;
        if (groupName.equals(DEFAULT_GROUP)) group = defaultGroup;
        else group = groupHash.get(groupName);
        if (group != null) {
            //权限组内权限
            for (String per:group.getPers()) list.add(per);
            //权限组内继承的权限组列表
            for (String inherit:group.getInherits()) checkAdd(list, inherit);
        }
    }

    /**
     * 检测初始化玩家的权限信息<br>
     *     如果不存在会新建
     * @param name 玩家名,不为null
     * @return 异常返回null
     */
    private static PerUser checkInit(String name) {
        if (userHash.containsKey(name)) return userHash.get(name);
        else {
            PerUser pu = load(name);
            if (pu == null) pu = create(name);
            if (pu != null) userHash.put(name, pu);
            return pu;
        }
    }

    /**
     * 新建玩家权限信息文件并读取<br>
     *     如果原来已经存在此会删除原来的
     * @param name 玩家名,不为null
     * @return 玩家权限信息,异常返回null
     */
    private static PerUser create(String name) {
        String path = Per.path+File.separator+"user"+File.separator+name+".yml";
        File file = new File(path);
        file.delete();
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
            return new PerUser(name, new HashListImpl<String>(), new HashListImpl<String>());
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 从文件中读取玩家权限信息
     * @param name 玩家名,不为null
     * @return 权限信息,文件不存在或其它读取异常返回null
     */
    private static PerUser load(String name) {
        String path = Per.path+File.separator+"user"+File.separator+name+".yml";
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            YamlConfiguration config = CoreApi.loadConfigByUTF8(file);
            if (config != null) return PerUser.load(name, config);
        }
        return null;
    }

    /**
     * 保存所有缓存的未保存的信息到文件
     */
    private static void saveAll() {
        HashList<String> copy = new HashListImpl<String>();
        for (String name:needSaveList) copy.add(name);
        for (String name:copy) {
            PerUser pu = checkInit(name);
            if (pu != null) save(pu, true);
        }
    }

    /**
     * 保存玩家的权限信息到文件
     * @param pu 权限信息,不为null
     * @param force 是否强制保存
     */
    private static void save(PerUser pu, boolean force) {
        //不需要保存
        if (!needSaveList.has(pu.getName())) return;
        //不保存
        long now = System.currentTimeMillis();
        long pre = 0;
        if (lastSaveHash.containsKey(pu.getName())) pre = lastSaveHash.get(pu.getName());
        if (now - pre <= saveInterval && !force) return;
        //要保存
        //更新
        lastSaveHash.put(pu.getName(), now);
        needSaveList.remove(pu.getName());
        //设置
        String path = Per.path+File.separator+"user"+File.separator+pu.getName()+".yml";
        File file = new File(path);
        YamlConfiguration config = new YamlConfiguration();
        PerUser.save(config, pu);
        //save
        CoreApi.saveConfigByUTF8(config, file);
    }

    private static void loadConfig() {
        //先将缓存的未保存的信息保存到文件
        saveAll();

        //读取
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);
        //saveInterval
        saveInterval = config.getInt("per.saveInterval");
        //removeTime
        removeTime = config.getInt("per.removeTime");
        //defaultGroup
        String path = Per.path+File.separator+"group"+File.separator+DEFAULT_GROUP+".yml";
        File file = new File(path);
        file.getParentFile().mkdirs();
        config = CoreApi.loadConfigByUTF8(file);
        defaultGroup = Group.load(config);
        defaultGroup.getInherits().clear();
        //groupHash
        path = Per.path+File.separator+"group";
        file = new File(path);
        file.getParentFile().mkdirs();
        groupHash = new HashMap<String, Group>();
        if (file.exists() && file.isDirectory()) {
            for (File f:file.listFiles()) {
                String groupName = f.getName().split("\\.")[0];
                if (!groupName.equals(DEFAULT_GROUP)) {//非默认权限组
                    config = CoreApi.loadConfigByUTF8(f);
                    if (config != null){
                        Group group = Group.load(config);
                        if (group != null) {
                            groupHash.put(groupName, group);
                        }
                    }
                }
            }
        }
        //优化策略
        //清空重置persHash
        persHash.clear();
        //清空重置userHash
        userHash.clear();
    }
}
