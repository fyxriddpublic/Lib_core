package com.fyxridd.lib.core.per;

import com.fyxridd.lib.core.api.ConfigApi;
import com.fyxridd.lib.core.api.CorePlugin;
import com.fyxridd.lib.core.api.FormatApi;
import com.fyxridd.lib.core.api.PerApi;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * 命令操作权限
 */
public class CmdManager implements CommandExecutor, Listener {
    //配置
    private String lib_core_admin;

    public CmdManager() {
        //读取配置文件
        loadConfig();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //注册命令
        CorePlugin.instance.getCommand("per").setExecutor(this);
        CorePlugin.instance.getCommand("group").setExecutor(this);
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

    /**
     * group create <权限组> 新建权限组
     * group del <权限组> 删除权限组
     * group addPer <权限组> <权限> 权限组添加权限
     * group removePer <权限组> <权限> 权限组删除权限
     * group addInherit <权限组> <继承> 权限组添加继承
     * group removeInherit <权限组> <继承> 权限组删除继承
     * group add <玩家名> <权限组> 给玩家添加权限组
     * group remove <玩家名> <权限组> 给玩家删除权限组
     *
     * per add <玩家名> <权限> 给玩家添加权限
     * per remove <玩家名> <权限> 给玩家删除权限
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //玩家发出命令权限检测
        if (sender instanceof Player && !PerApi.checkPer((Player) sender, lib_core_admin)) return true;

        try {
            if (cmd.getName().equalsIgnoreCase("group")) {
                switch (args.length) {
                    case 2:
                        if (args[0].equalsIgnoreCase("create")) {
                            create(sender, args[1]);
                            return true;
                        }else if (args[0].equalsIgnoreCase("del")) {
                            del(sender, args[1]);
                            return true;
                        }
                        break;
                    case 3:
                        if (args[0].equalsIgnoreCase("addPer")) {
                            groupAddPer(sender, args[1], args[2]);
                            return true;
                        }else if (args[0].equalsIgnoreCase("removePer")) {
                            groupRemovePer(sender, args[1], args[2]);
                            return true;
                        }else if (args[0].equalsIgnoreCase("addInherit")) {
                            groupAddInherit(sender, args[1], args[2]);
                            return true;
                        }else if (args[0].equalsIgnoreCase("removeInherit")) {
                            groupRemoveInherit(sender, args[1], args[2]);
                            return true;
                        }else if (args[0].equalsIgnoreCase("add")) {
                            addGroup(sender, args[1], args[2]);
                            return true;
                        }else if (args[0].equalsIgnoreCase("remove")) {
                            removeGroup(sender, args[1], args[2]);
                            return true;
                        }
                        break;
                }
            }else if (cmd.getName().equalsIgnoreCase("per")) {
                switch (args.length) {
                    case 3:
                        if (args[0].equalsIgnoreCase("add")) {
                            addPer(sender, args[1], args[2]);
                            return true;
                        }else if (args[0].equalsIgnoreCase("remove")) {
                            removePer(sender, args[1], args[2]);
                            return true;
                        }
                        break;
                }
            }
        } catch (Exception e) {
            //操作异常
            sender.sendMessage(get(100).getText());
            return true;
        }

        //输入格式错误
        sender.sendMessage(get(5).getText());
        return true;
    }

    private void create(CommandSender sender, String group) {
        if (PerApi.createGroup(group)) sender.sendMessage(get(1300).getText());
        else sender.sendMessage(get(1310).getText());
    }

    private void del(CommandSender sender, String group) {
        if (PerApi.delGroup(group)) sender.sendMessage(get(1340).getText());
        else sender.sendMessage(get(1350).getText());
    }

    private void groupAddPer(CommandSender sender, String group, String per) {
        if (PerApi.groupAddPer(group, per)) sender.sendMessage(get(1320).getText());
        else sender.sendMessage(get(1330).getText());
    }

    private void groupRemovePer(CommandSender sender, String group, String per) {
        if (PerApi.groupRemovePer(group, per)) sender.sendMessage(get(1340).getText());
        else sender.sendMessage(get(1350).getText());
    }

    private void groupAddInherit(CommandSender sender, String group, String inherit) {
        if (PerApi.groupAddInherit(group, inherit)) sender.sendMessage(get(1320).getText());
        else sender.sendMessage(get(1330).getText());
    }

    private void groupRemoveInherit(CommandSender sender, String group, String inherit) {
        if (PerApi.groupRemoveInherit(group, inherit)) sender.sendMessage(get(1340).getText());
        else sender.sendMessage(get(1350).getText());
    }

    private void addGroup(CommandSender sender, String name, String group) {
        if (PerApi.addGroup(name, group)) sender.sendMessage(get(1360, name, group).getText());
        else sender.sendMessage(get(1370, name, group).getText());
    }

    private void removeGroup(CommandSender sender, String name, String group) {
        if (PerApi.delGroup(name, group)) sender.sendMessage(get(1380, name, group).getText());
        else sender.sendMessage(get(1390, name, group).getText());
    }

    private void addPer(CommandSender sender, String name, String per) {
        if (PerApi.add(name, per)) sender.sendMessage(get(1400, name, per).getText());
        else sender.sendMessage(get(1410, name, per).getText());
    }

    private void removePer(CommandSender sender, String name, String per) {
        if (PerApi.del(name, per)) sender.sendMessage(get(1420, name, per).getText());
        else sender.sendMessage(get(1430, name, per).getText());
    }

    private void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        lib_core_admin = config.getString("lib_core_admin");
    }

    private FancyMessage get(int id, Object... args) {
        return FormatApi.get(CorePlugin.pn, id, args);
    }
}
