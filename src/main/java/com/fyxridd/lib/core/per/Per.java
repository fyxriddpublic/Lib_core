package com.fyxridd.lib.core.per;

import com.fyxridd.lib.core.CoreMain;
import com.fyxridd.lib.core.api.CoreApi;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.model.PerGroup;
import com.fyxridd.lib.core.api.model.PerUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.HashSet;

public class Per implements Listener,PerHandler {
    //默认权限组名
    private static final String DEFAULT_GROUP = "default";

    //默认权限组,所有玩家都有的
    private PerGroup defaultGroup;
    //权限组名 权限组
    //不包括默认权限组
    private HashMap<String, PerGroup> groupHash;

    //优化策略
    //玩家名 权限用户
    //动态读取
    private HashMap<String, PerUser> userHash = new HashMap<>();
    //优化策略
    //玩家名 拥有的权限列表
    //动态读取
    private HashMap<String, HashSet<String>> persHash = new HashMap<>();
    //优化策略
    //需要保存的玩家列表
    private HashSet<PerUser> needUpdateList = new HashSet<>();

    public Per() {
        //(重新)读取数据
        loadData();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //计时器: 保存
        Bukkit.getScheduler().scheduleSyncRepeatingTask(CorePlugin.instance, new Runnable() {
            @Override
            public void run() {
                saveAll();
            }
        }, 319, 319);
    }

    public void onDisable() {
        saveAll();
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        checkInit(e.getPlayer().getName());
    }

    @Override
	public boolean has(Player p, String per) {
        if (p == null) return false;

        return has(p.getName(), per);
	}

    @Override
	public boolean has(String name, String per) {
        if (name == null) return false;
        if (per == null || per.isEmpty()) return true;

        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;

        //检测
        HashSet<String> set = persHash.get(name);
        if (set == null) set = updatePers(name);
        return set.contains(per);
	}

    @Override
	public boolean add(Player p, String per) {
        if (p == null) return false;

		return add(p.getName(), per);
	}

    @Override
	public boolean add(String name, String per) {
        if (name == null || per == null || per.isEmpty()) return false;
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //读取
        PerUser pu = checkInit(name);
        if (pu.getPers().contains(per)) return false;
        pu.getPers().add(per);
        //优化策略
        //更新persHash
        updatePers(name);
        //保存
        needUpdateList.add(pu);
		return true;
	}

    @Override
	public boolean del(Player p, String per) {
        if (p == null) return false;

        return del(p.getName(), per);
	}

    @Override
    public boolean del(String name, String per) {
        if (name == null || per == null) return false;
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //读取
        PerUser pu = checkInit(name);
        if (!pu.getPers().contains(per)) return false;
        pu.getPers().remove(per);
        //优化策略
        //更新persHash
        updatePers(name);
        //保存
        needUpdateList.add(pu);
        return true;
    }

    @Override
	public boolean hasGroup(Player p,String groupName, boolean loop) {
        if (p == null) return false;

        return hasGroup(p.getName(), groupName, loop);
	}

    @Override
    public boolean hasGroup(String name, String groupName, boolean loop) {
        if (name == null || groupName == null) return false;
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //权限组不存在
        PerGroup group = groupHash.get(groupName);
        if (group == null) return false;
        //检测
        PerUser pu = checkInit(name);
        if (loop) {
            if (pu.getGroups().contains(groupName)) return true;
            for (String s:pu.getGroups()) {
                if (checkHasGroup(s, groupName)) return true;
            }
            return false;
        }else return pu.getGroups().contains(groupName);
    }

    @Override
	public boolean addGroup(Player p,String groupName) {
        if (p == null) return false;

		return addGroup(p.getName(), groupName);
	}

    @Override
    public boolean addGroup(String name, String groupName) {
        if (name == null || groupName == null) return false;

        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //权限组不存在
        PerGroup group = groupHash.get(groupName);
        if (group == null) return false;
        //已经有此权限组
        PerUser pu = checkInit(name);
        if (pu.getGroups().contains(groupName)) return false;
        //添加权限组成功
        pu.getGroups().add(groupName);
        //优化策略
        //更新persHash
        updatePers(name);
        //保存
        needUpdateList.add(pu);
        return true;
    }

    @Override
    public boolean delGroup(Player p,String groupName) {
        if (p == null) return false;

        return delGroup(p.getName(), groupName);
    }

    @Override
    public boolean delGroup(String name, String groupName) {
        if (name == null || groupName == null) return false;

        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //权限组不存在
        PerGroup group = groupHash.get(groupName);
        if (group == null) return false;
        //没有此权限组
        PerUser pu = checkInit(name);
        if (!pu.getGroups().contains(groupName)) return false;
        //删除权限组成功
        pu.getGroups().remove(groupName);
        //优化策略
        //更新persHash
        updatePers(name);
        //保存
        needUpdateList.add(pu);
        return true;
    }

    @Override
    public boolean checkHasGroup(String tar, String groupName) {
        if (tar == null || groupName == null) return false;

        PerGroup group = groupHash.get(tar);
        if (group == null) return false;
        if (group.getInherits().contains(groupName)) return true;
        for (String s:group.getInherits()) {
            if (checkHasGroup(s, groupName)) return true;
        }
        return false;
    }

    @Override
    public boolean createGroup(String group) {
        //group不能为null,不能与默认组名相同
        if (group == null || group.equalsIgnoreCase(DEFAULT_GROUP)) return false;

        //组已经存在
        PerGroup perGroup = groupHash.get(group);
        if (perGroup != null) return false;

        //新建成功,保存数据
        perGroup = new PerGroup(group, new HashSet<String>(), new HashSet<String>());
        groupHash.put(group, perGroup);
        CoreMain.dao.saveOrUpdate(perGroup);
        //清空用户缓存
        clearUsers();
        return true;
    }

    @Override
    public boolean delGroup(String group) {
        //group不能为null,不能与默认组名相同
        if (group == null || group.equalsIgnoreCase(DEFAULT_GROUP)) return false;

        //组不存在
        PerGroup perGroup = groupHash.get(group);
        if (perGroup == null) return false;

        //删除成功,保存数据
        groupHash.remove(group);
        CoreMain.dao.delete(perGroup);
        //清空用户缓存
        clearUsers();
        return true;
    }

    @Override
    public boolean groupAddPer(String group, String per) {
        if (group == null || per == null) return false;

        //权限组不存在(允许使用默认权限组)
        PerGroup perGroup = group.equalsIgnoreCase(DEFAULT_GROUP)?defaultGroup:groupHash.get(group);
        if (perGroup == null) return false;
        //已经包含此权限
        if (!perGroup.getPers().add(per)) return false;
        //添加成功,保存数据库
        CoreMain.dao.saveOrUpdate(perGroup);
        //清空用户缓存
        clearUsers();
        return true;
    }

    @Override
    public boolean groupRemovePer(String group, String per) {
        if (group == null || per == null) return false;

        //权限组不存在(允许使用默认权限组)
        PerGroup perGroup = group.equalsIgnoreCase(DEFAULT_GROUP)?defaultGroup:groupHash.get(group);
        if (perGroup == null) return false;
        //不包含此权限
        if (!perGroup.getPers().remove(per)) return false;
        //删除成功,保存数据库
        CoreMain.dao.saveOrUpdate(perGroup);
        //清空用户缓存
        clearUsers();
        return true;
    }

    @Override
    public boolean groupAddInherit(String group, String inherit) {
        if (group == null || inherit == null) return false;

        //权限组不存在
        PerGroup perGroup = groupHash.get(group);
        if (perGroup == null) return false;
        if (!groupHash.containsKey(inherit)) return false;
        //已经包含此继承
        if (!perGroup.getInherits().add(inherit)) return false;
        //添加成功,保存数据库
        CoreMain.dao.saveOrUpdate(perGroup);
        //清空用户缓存
        clearUsers();
        return true;
    }

    @Override
    public boolean groupRemoveInherit(String group, String inherit) {
        if (group == null || inherit == null) return false;

        //权限组不存在
        PerGroup perGroup = groupHash.get(group);
        if (perGroup == null) return false;
        //不包含此继承
        if (!perGroup.getInherits().remove(inherit)) return false;
        //删除成功,保存数据库
        CoreMain.dao.saveOrUpdate(perGroup);
        //清空用户缓存
        clearUsers();
        return true;
    }

    /**
     * 优化策略<br>
     * (根据默认权限组与PerUser)更新玩家的权限信息<br>
     *     更新过后persHash中必然包含玩家
     * @param name 玩家名,不为null
     */
    private HashSet<String> updatePers(String name) {
        HashSet<String> set = new HashSet<>();

        //默认权限组
        checkAdd(set, DEFAULT_GROUP);
        //本身权限
        PerUser pu = checkInit(name);
        for (String per:pu.getPers()) set.add(per);
        //本身权限组
        for (String group:pu.getGroups()) checkAdd(set, group);

        //设置
        persHash.put(name, set);
        //返回
        return set;
    }

    /**
     * 将权限组内的所有权限加入集合
     * @param set 集合,不为null
     * @param groupName 权限组名,不为null
     */
    private void checkAdd(HashSet<String> set, String groupName) {
        PerGroup group;
        if (groupName.equals(DEFAULT_GROUP)) group = defaultGroup;
        else group = groupHash.get(groupName);
        if (group != null) {
            //权限组内权限
            for (String per:group.getPers()) set.add(per);
            //权限组内继承的权限组列表
            for (String inherit:group.getInherits()) checkAdd(set, inherit);
        }
    }

    /**
     * 检测初始化玩家的权限信息<br>
     *     如果不存在会新建
     * @param name 玩家名,不为null
     * @return 不为null
     */
    private PerUser checkInit(String name) {
        //先从缓存中读取
        PerUser user = userHash.get(name);

        //再从数据库中读取
        if (user == null) {
            user = CoreMain.dao.getPerUser(name);
            //新建
            if (user == null) {
                user = new PerUser(name, new HashSet<String>(), new HashSet<String>());
                needUpdateList.add(user);
            }
            //添加缓存
            userHash.put(name, user);
        }

        return user;
    }

    /**
     * 保存所有缓存的未保存的信息
     */
    private void saveAll() {
        if (!needUpdateList.isEmpty()) {
            CoreMain.dao.saveOrUpdates(needUpdateList);
            needUpdateList.clear();
        }
    }

    /**
     * 清空用户缓存
     */
    private void clearUsers() {
        saveAll();
        userHash.clear();
        persHash.clear();
    }

    /**
     * (重新)读取数据
     */
    private void loadData() {
        //清空缓存
        clearUsers();

        //读取所有权限组
        groupHash = new HashMap<>();
        for (PerGroup group:CoreMain.dao.getPerGroups()) {
            if (group.getName().equalsIgnoreCase(DEFAULT_GROUP)) {
                defaultGroup = group;
                //检测清空默认权限组的继承列表
                if (!defaultGroup.getInherits().isEmpty()) {
                    defaultGroup.getInherits().clear();
                    CoreMain.dao.saveOrUpdate(defaultGroup);
                }
            }else groupHash.put(group.getName(), group);
        }

        //未生成默认权限组
        if (defaultGroup == null) {
            defaultGroup = new PerGroup(DEFAULT_GROUP, new HashSet<String>(), new HashSet<String>());
            CoreMain.dao.saveOrUpdate(defaultGroup);
        }
    }
}
