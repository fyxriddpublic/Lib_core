package com.fyxridd.lib.core.show;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.fyxridd.lib.core.CoreMain;
import com.fyxridd.lib.core.api.*;
import com.fyxridd.lib.core.api.event.RealDamageEvent;
import com.fyxridd.lib.core.api.event.ReloadConfigEvent;
import com.fyxridd.lib.core.api.inter.*;
import com.fyxridd.lib.core.InputManager;
import com.fyxridd.lib.core.api.event.PlayerChatEvent;
import com.fyxridd.lib.core.FuncManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 显示管理
 */
public class ShowManager implements Listener, FunctionInterface, ShowInterface {
    /**
     * 默认pageNow的值
     */
    private static final int DEFAULT_PAGE_NOW = 1;

    /**
     * 默认listNow的值
     */
    private static final int DEFAULT_LIST_NOW = 1;

    /**
     * 功能名
     */
    private static final String FUNC_NAME = "ShowManager";
    private static final String GET_PLAYER_CONTEXT = "getPlayerContext";

    public static ShowListManager showListManager;
    public static ShowMapManager showMapManager;

    private static boolean inCancelChat;
    private static int deadLoopLevel = 5;//循环最大层次,超过则判定为死循环
    private static boolean cancelInteract, cancelAnimation, cancelAttack, cancelChat, cancelShoot;
    private static int maxBackPage = 10;
    private static int line;
    private static FancyMessage add;
    private static FancyMessage operateTipMenu;
    private static FancyMessage operateTipEmpty;
    private static FancyMessage pageControl, listControl;

    /**
     * 插件名 页面名 页面
     */
    private static HashMap<String, HashMap<String, Page>> pageHash = new HashMap<>();

    /**
     * 玩家 玩家页面上下文
     */
    private static HashMap<Player, PlayerContext> playerContextHash = new HashMap<>();
    /**
     * 玩家 返回页面保存列表
     */
    private static HashMap<Player, List<PlayerContext>> backHash = new HashMap<>();
    /**
     * 玩家 提示
     */
    private static HashMap<Player, List<FancyMessage>> tipHash = new HashMap<>();
    /**
     * 正在重新显示的中的玩家列表(用来防止显示死循环),玩家 调用层数
     */
    private static HashMap<Player, Integer> reShowHash = new HashMap<>();

    public ShowManager() {
        showListManager = new ShowListManager();
        showMapManager = new ShowMapManager();
        //注册事件
        Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //注册功能
        FuncManager.register(this);
        //监听限制聊天包
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(CorePlugin.instance, PacketType.Play.Server.CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (isInPage(event.getPlayer())) event.setCancelled(true);
            }
        });
        //注册Params获取器
        Bukkit.getScheduler().scheduleSyncDelayedTask(CorePlugin.instance, new Runnable() {
            @Override
            public void run() {
                TransactionApi.registerParamsHandler(CorePlugin.pn, GET_PLAYER_CONTEXT, new TipParamsHandler() {
                    @Override
                    public Object get(Player p, String arg) {
                        //当前没有查看的页面
                        PlayerContext pc = playerContextHash.get(p);
                        if (pc == null) {
                            tip(p, ShowManager.get(665), true);
                            return null;
                        }
                        //返回
                        return pc;
                    }
                });
            }
        });
    }

    @EventHandler(priority= EventPriority.LOW)
    public void onReloadConfig(ReloadConfigEvent e) {
        if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
    }

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent e) {
        if (cancelShoot && e.getEntity() instanceof Player) exit((Player) e.getEntity(), false);
    }

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (cancelInteract) exit(e.getPlayer(), false);
    }

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerAnimation(PlayerAnimationEvent e) {
        if (cancelAnimation) exit(e.getPlayer(), false);
    }

    @EventHandler(priority= EventPriority.LOW, ignoreCancelled = false)
    public void onPlayerChat(PlayerChatEvent e) {
        if (cancelChat) exit(e.getP(), false);
        else if (inCancelChat && !e.isCancelled() && isInPage(e.getP())) e.setCancelled(true);
    }

    @EventHandler(priority= EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        exit(e.getPlayer(), false);
    }

    @EventHandler(priority= EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        exit(e.getEntity(), false);
    }

    @EventHandler(priority= EventPriority.LOWEST)
    public void onRealDamage(RealDamageEvent e) {
        if (cancelAttack) {
            EntityDamageByEntityEvent event = e.getEntityDamageByEntityEvent();

            //受攻击者
            if (event.getEntity() instanceof Player) {
                ShowManager.exit((Player) event.getEntity(), false, true);
            }

            //攻击者
            Player damager = null;
            if (event.getDamager() instanceof Player) damager = (Player) event.getDamager();
            else if (event.getDamager() instanceof Projectile) {
                ProjectileSource ps = ((Projectile) event.getDamager()).getShooter();
                if (ps instanceof Player) damager = (Player) ps;
            }
            if (damager != null) ShowManager.exit(damager, false, true);
        }
    }

    public static FancyMessage getPageControl() {
        return pageControl;
    }

    public static FancyMessage getListControl() {
        return listControl;
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#register(String)
     */
    public static void register(String plugin) {
        //重新注册插件的所有页面
        pageHash.put(plugin, new HashMap<String, Page>());
        //读取
        File dir = new File(CoreApi.pluginPath, plugin+File.separator+"show");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files!= null) {
                for (File file:files) {
                    if (file.isFile() && file.getName().endsWith(".yml") && !file.getName().endsWith("_description.yml")) {
                        register(plugin, file.getName().substring(0, file.getName().length()-4));
                    }
                }
            }
        }
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#register(String, String)
     */
    public static void register(String plugin, String name) {
        //读取页面信息
        Page page = load(plugin, name);
        if (!pageHash.containsKey(plugin)) pageHash.put(plugin, new HashMap<String, Page>());
        pageHash.get(plugin).put(name, page);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#show(com.fyxridd.lib.core.api.inter.ShowInterface, Object, org.bukkit.entity.Player, String, String, com.fyxridd.lib.core.api.inter.ShowList, java.util.HashMap, int, int, java.util.List, java.util.List, java.util.HashMap)
     */
    public static void show(ShowInterface callback, Object obj, Player p, String plugin, String pageName,
                            ShowList list, HashMap<String, Object> data, List<FancyMessage> front, List<FancyMessage> behind) {
        show(callback, obj, p, plugin, pageName, list, data, DEFAULT_PAGE_NOW, DEFAULT_LIST_NOW, front, behind, null);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#show(com.fyxridd.lib.core.api.inter.ShowInterface, Object, org.bukkit.entity.Player, String, String, com.fyxridd.lib.core.api.inter.ShowList, java.util.HashMap, int, int, java.util.List, java.util.List, java.util.HashMap)
     */
    public static void show(ShowInterface callback, Object obj, Player p, String plugin, String pageName,
                            ShowList<Object> list, HashMap<String, Object> data, int pageNow, int listNow,
                            List<FancyMessage> front, List<FancyMessage> behind) {
        show(callback, obj, p, plugin, pageName, list, data, pageNow, listNow, front, behind, null);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#show(com.fyxridd.lib.core.api.inter.ShowInterface, Object, org.bukkit.entity.Player, String, String, com.fyxridd.lib.core.api.inter.ShowList, java.util.HashMap, int, int, java.util.List, java.util.List, java.util.HashMap)
     */
    public static void show(ShowInterface callback, Object obj, Player p, String plugin, String pageName,
                            ShowList<Object> list, HashMap<String, Object> data,
                            List<FancyMessage> front, List<FancyMessage> behind, HashMap<String, ItemStack> itemHash) {
        show(callback, obj, p, plugin, pageName, list, data, DEFAULT_PAGE_NOW, DEFAULT_LIST_NOW, front, behind, itemHash);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#show(com.fyxridd.lib.core.api.inter.ShowInterface, Object, org.bukkit.entity.Player, String, String, com.fyxridd.lib.core.api.inter.ShowList, java.util.HashMap, int, int, java.util.List, java.util.List, java.util.HashMap)
     */
    public static void show(ShowInterface callback, Object obj, Player p, String plugin, String pageName,
                            ShowList<Object> list, HashMap<String, Object> data, int pageNow, int listNow,
                            List<FancyMessage> front, List<FancyMessage> behind, HashMap<String, ItemStack> itemHash) {
        //注意!此方法内不能调用tip()方法,而应该用setTip()代替,否则会出现死循环
        try {
            //玩家页面上下文
            PlayerContext pc = playerContextHash.get(p);
            //读取页面
            Page page = null;
            //读取是否成功
            boolean read;
            try {
                page = pageHash.get(plugin).get(pageName);
                read = true;
            } catch (Exception e) {
                read = false;
            }
            if (!read || page == null) { //页面异常,未知页面
                if (pc != null &&
                    pc.callback == callback &&
                    pc.obj == obj &&
                    pc.p == p &&
                    pc.plugin.equals(plugin) &&
                    pc.pageName.equals(pageName) &&
                    pc.list == list &&
                    pc.data == data &&
                    pc.pageNow == pageNow &&
                    pc.listNow == listNow) {//是当前页面上下文在调用
                    playerContextHash.remove(p);
                }else setTip(p, get(645));
                return;
            }

            //页面未生效
            if (!page.isEnable()) {
                setTip(p, get(740));
                return;
            }

            //权限检测
            if (!PerApi.checkPer(p, page.getPer())) return;

            //页面控制
            int pageMax = page.getPageMax();//最大页
            //指定页面不存在
            if (pageNow < 1 || pageNow > pageMax) {
                if (pageNow < 1) pageNow = 1;
                else if (pageNow > pageMax) pageNow = pageMax;
                setTip(p, get(650));
            }
            List<Integer> showPage = page.getPageList().get(pageNow-1).getContent();//显示的页面
            List<FancyMessage> resultPage = new ArrayList<>();//结果页面,复制版
            for (int line: showPage) {
                FancyMessage msg = getMsg(page.getLines(), line);
                if (msg == null) continue;
                resultPage.add(msg.clone());
            }

            //列表控制
            int listSize = page.getListSize();
            int listMax = 0;
            int getSize = 0;
            if (list == null && page.getListInfo() != null) list = ShowListManager.getShowList(p.getName(), page.getListInfo().getPlugin(), page.getListInfo().getKey());
            if (list != null) {
                listMax = list.getMaxPage(listSize);//列表最大页
                List showList = list.getPage(listSize, listNow);
                getSize = showList.size();
                for (FancyMessage msg : resultPage) {
                    for (FancyMessage.MessagePart mp : msg.getMessageParts()) {
                        if (mp.listFix != null) {
                            HashMap<String, Object> replace = new HashMap<String, Object>();
                            for (String fix : mp.listFix) {
                                try {
                                    int num;
                                    String value;
                                    String[] ss = fix.split("\\.");
                                    num = Integer.parseInt(ss[0]);
                                    if (fix.charAt(fix.length() - 1) == ')') {//调用方法
                                        if (num >= 0 && num < getSize) {
                                            String methodName = ss[1];
                                            Object o = showList.get(num);
                                            if (methodName.charAt(methodName.length()-2) == '(') {
                                                methodName = methodName.substring(0, methodName.length()-2);
                                                Method method;
                                                if (list.getClassType() == null) method = o.getClass().getDeclaredMethod(methodName);
                                                else method = list.getClassType().getDeclaredMethod(methodName);
                                                boolean accessible = method.isAccessible();
                                                method.setAccessible(true);
                                                value = String.valueOf(method.invoke(o));
                                                method.setAccessible(accessible);
                                            }else {
                                                methodName = methodName.substring(0, methodName.length()-6);
                                                Method method;
                                                if (list.getClassType() == null) method = o.getClass().getDeclaredMethod(methodName, String.class);
                                                else method = list.getClassType().getDeclaredMethod(methodName, String.class);
                                                boolean accessible = method.isAccessible();
                                                method.setAccessible(true);
                                                value = String.valueOf(method.invoke(o, p.getName()));
                                                method.setAccessible(accessible);
                                            }
                                        } else value = "";
                                    } else {//调用属性
                                        if (num >= 0 && num < getSize) {
                                            Object o = showList.get(num);
                                            Field field;
                                            if (list.getClassType() == null) field = o.getClass().getDeclaredField(ss[1]);
                                            else field = list.getClassType().getDeclaredField(ss[1]);
                                            boolean accessible = field.isAccessible();
                                            field.setAccessible(true);
                                            value = String.valueOf(field.get(o));
                                            field.setAccessible(accessible);
                                        } else value = "";
                                    }
                                    //添加替换
                                    replace.put(fix, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    CoreApi.debug(e.getMessage());
                                }
                            }
                            MessageApi.convert(mp, replace);
                        }
                    }
                }
            }

            //内置替换符
            HashMap<String, Object> replace = new HashMap<String, Object>();
            replace.put("name", p.getName());
            replace.put("displayName", p.getDisplayName());
            replace.put("pageNow", pageNow);
            replace.put("pageMax", pageMax);
            replace.put("listSize", listSize);
            replace.put("listNow", listNow);
            replace.put("listMax", listMax);
            replace.put("getSize", getSize);
            for (FancyMessage msg:resultPage) MessageApi.convert(msg, replace);

            //协议替换符
            for (FancyMessage msg:resultPage) MessageApi.convert(msg, data);

            //额外替换符
            if (page.getMaps() != null) {
                replace = new HashMap<String, Object>();
                for (Page.MapInfo mi: page.getMaps().values())
                    replace.put(mi.getKeyName(), ShowMapManager.getObject(p.getName(), mi.getPlugin(), mi.getKey()));
                for (FancyMessage msg:resultPage) MessageApi.convert(msg, replace);
            }

            //物品信息替换
            if (itemHash != null) {
                for (FancyMessage msg:resultPage) {
                    for (FancyMessage.MessagePart mp : msg.getMessageParts()) {
                        if (mp.item != null) {
                            ItemStack is = itemHash.get(mp.item);
                            if (is != null) {
                                mp.hoverActionName = "show_item";
                                mp.hoverActionData = MessageApi.getHoverActionData(is);
                                mp.hoverActionString = null;
                            }
                        }
                    }
                }
            }

            //条件显示
            for (FancyMessage msg:resultPage) msg.checkCondition();

            //成功

            //更新玩家页面上下文
            if (page.isRecord()) {
                if (pc == null) {
                    pc = new PlayerContext();
                    playerContextHash.put(p, pc);
                }else if (!pc.plugin.equals(plugin) ||
                        !pc.pageName.equals(pageName)) {//显示的页面与原来不同,保存返回页面
                    List<PlayerContext> backList = backHash.get(p);
                    if (backList == null) {
                        backList = new ArrayList<PlayerContext>();
                        backHash.put(p, backList);
                    }
                    backList.add(pc.clone());
                    if (backList.size() > maxBackPage) backList.remove(0);
                }
                //更新玩家页面上下文信息
                pc.callback = callback;
                pc.obj = obj;
                pc.p = p;
                pc.plugin = plugin;
                pc.pageName = pageName;
                pc.listSize = listSize;
                pc.list = list;
                pc.data = data;
                pc.pageNow = pageNow;
                pc.listNow = listNow;
                pc.front = front;
                pc.behind = behind;
                pc.itemHash = itemHash;
            }

            //补空行
            if (page.isFillEmpty()) {
                int empty = line-(page.isHandleTip()?2:0)-resultPage.size()-(front != null?front.size():0)-(behind != null?behind.size():0);
                if (empty > 0) {
                    for (int i=0;i<empty;i++) add.send(p, false);
                }
            }
            //显示页面
            if (front != null) {
                for (FancyMessage msg: front) msg.send(p, false);
            }
            for (FancyMessage msg:resultPage) msg.send(p, false);
            if (behind != null) {
                for (FancyMessage msg: behind) msg.send(p, false);
            }
            //显示页面尾部操作提示
            if (page.isHandleTip()) {
                operateTipMenu.send(p, false);
                List<FancyMessage> tipList = tipHash.get(p);
                if (tipList != null) {
                    for (FancyMessage tip: tipList) tip.send(p, false);
                }else operateTipEmpty.send(p, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CoreApi.debug(e.getMessage());
            playerContextHash.remove(p);
            setTip(p, get(655));
        }
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#getMsg(java.util.LinkedHashMap, int)
     */
    public static FancyMessage getMsg(LinkedHashMap<Integer, Page.LineContext> lines, int line) {
        try {
            if (line == -1) return getPageControl();
            else if (line == -2) return getListControl();
            else return lines.get(line).getMsg();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#reShow(com.fyxridd.lib.core.api.inter.PlayerContext, boolean)
     */
    public static void reShow(PlayerContext pc) {
        reShow(pc, false);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#reShow(com.fyxridd.lib.core.api.inter.PlayerContext, boolean)
     */
    public static void reShow(PlayerContext pc, boolean noRefresh) {
        if (pc == null) return;
        Page page = getPage(pc.plugin, pc.pageName);
        if (page == null) return;
        Player p = pc.p;
        if (p == null) return;
        //防止死循环
        if (!reShowHash.containsKey(p)) reShowHash.put(p, 1);
        else reShowHash.put(p, reShowHash.get(p)+1);
        if (reShowHash.get(p) > deadLoopLevel) {//判定为死循环
            CoreApi.debug("Dead Loop Checked!!!Auto remove!");
            reShowHash.remove(p);//去除
            exit(pc.p, false);
            return;
        }
        //显示
        if (!noRefresh && page.isRefresh() && pc.callback != null) {//刷新
            pc.callback.show(pc);
        }else {//不刷新
            show(pc.callback, pc.obj, pc.p, pc.plugin, pc.pageName, pc.list, pc.data, pc.pageNow,
                    pc.listNow, pc.front, pc.behind, pc.itemHash);
        }
        //显示正常结束,去除
        reShowHash.remove(p);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#back(org.bukkit.entity.Player)
     */
    public static void back(Player p) {
        List<PlayerContext> backList = backHash.get(p);
        if (backList == null || backList.isEmpty()) {//退出页面
            exit(p, true);
            return;
        }
        //返回
        PlayerContext backContext = backList.remove(backList.size()-1);
        playerContextHash.put(p, backContext);
        List<FancyMessage> tipList = new ArrayList<FancyMessage>();
        tipList.add(get(660));
        tipHash.put(p, tipList);
        reShow(backContext);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#exit(org.bukkit.entity.Player, boolean)
     */
    public static void exit(Player p, boolean tip) {
        exit(p, tip, true);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#exit(org.bukkit.entity.Player, boolean, boolean)
     */
    public static void exit(Player p, boolean tip, boolean successTip) {
        //缓存
        reShowHash.remove(p);
        backHash.remove(p);
        tipHash.remove(p);
        //删除注册输入
        CoreApi.delInput(p, false);
        //当前没有查看的页面
        if (playerContextHash.remove(p) == null) {
            if(tip) tip(p, get(665), true);
            return;
        }
        //提示
        if (successTip) tip(p, get(675), true);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#tip(org.bukkit.entity.Player, java.util.List, boolean)
     */
    public static void tip(Player p, String msg, boolean force) {
        if (msg == null) return;
        tip(p, MessageApi.convert(msg), force);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#tip(org.bukkit.entity.Player, java.util.List, boolean)
     */
    public static void tip(Player p, FancyMessage msg, boolean force) {
        List<FancyMessage> tipList = new ArrayList<FancyMessage>();
        tipList.add(msg);
        tip(p, tipList, force);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#tip(org.bukkit.entity.Player, java.util.List, boolean)
     */
    public static void tip(Player p, List<FancyMessage> msgList, boolean force) {
        if (msgList == null) return;
        PlayerContext pc = playerContextHash.get(p);
        if (pc == null) {
            for (FancyMessage tip:msgList) tip.send(p, true);
        }else if (force){
            tipHash.put(p, msgList);
            reShow(pc);
        }
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#setTip(org.bukkit.entity.Player, java.util.List)
     */
    public static void setTip(Player p, FancyMessage tip) {
        List<FancyMessage> list = new ArrayList<FancyMessage>();
        list.add(tip);
        setTip(p, list);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#setTip(org.bukkit.entity.Player, java.util.List)
     */
    public static void setTip(Player p, List<FancyMessage> tip) {
        tipHash.put(p, tip);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#clearTip(org.bukkit.entity.Player, boolean, boolean)
     */
    public static void clearTip(Player p, boolean show, boolean cancelInput) {
        tipHash.remove(p);
        if (show) {
            //玩家开启界面的情况下才进行重新显示
            PlayerContext pc = playerContextHash.get(p);
            if (pc != null) reShow(pc);
        }
        if (cancelInput) InputManager.del(p, false);
    }

    public static HashMap<String, HashMap<String, Page>> getPageHash() {
        return pageHash;
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#hasRegistered(String, String)
     */
    public static boolean hasRegistered(String plugin, String page) {
        if (plugin == null || page == null) return false;
        return pageHash.containsKey(plugin) && pageHash.get(plugin).containsKey(page);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#getPage(String, String)
     */
    public static Page getPage(String plugin, String page) {
        if (plugin == null || page == null) return null;
        if (hasRegistered(plugin, page)) return pageHash.get(plugin).get(page);
        else return null;
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#load(String, String)
     */
    public static Page load(String plugin, String page) {
        String path = CoreApi.pluginPath+ File.separator+plugin+File.separator+"show"+File.separator+page+".yml";
        File file = new File(path);
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            ConfigApi.log(CorePlugin.pn, get(615).getText());
            return null;
        }
        return load(plugin, page, file);
    }

    public static Page load(String plugin, String page, File file) {
        return load(plugin, page, CoreApi.loadConfigByUTF8(file));
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#load(String, String, org.bukkit.configuration.file.YamlConfiguration)
     */
    public static Page load(String plugin, String page, YamlConfiguration config) {
        //enable
        boolean enable = config.getBoolean("enable", true);
        //pageMax
        int pageMax = config.getInt("pageMax", 0);
        if (pageMax < 0) {
            ConfigApi.log(CorePlugin.pn, get(620).getText());
            return null;
        }
        //listSize
        int listSize = config.getInt("listSize", 0);
        if (listSize < 0) {
            ConfigApi.log(CorePlugin.pn, get(625).getText());
            return null;
        }
        //refresh
        boolean refresh = config.getBoolean("refresh", false);
        //per
        String per = config.getString("per");
        //fillEmpty
        boolean fillEmpty = config.getBoolean("fillEmpty", true);
        //handleTip
        boolean handleTip = config.getBoolean("handleTip", true);
        //record
        boolean record = config.getBoolean("record", true);
        //list
        String listStr = config.getString("list");
        Page.ListInfo listInfo = listStr == null || listStr.isEmpty()?null:new Page.ListInfo(listStr.split(" ")[0], listStr.split(" ")[1]);
        //maps
        HashMap<String, Page.MapInfo> maps = null;
        List<String> mapsList = config.getStringList("maps");
        if (mapsList != null && !mapsList.isEmpty()) {
            maps = new HashMap<>();
            for (String s:mapsList) {
                String keyName = s.split(" ")[0];
                String keyValue = s.split(" ")[1];
                String pluginName = keyValue.split(":")[0];
                String pluginKey = keyValue.split(":")[1];
                maps.put(keyName, new Page.MapInfo(keyName, pluginName, pluginKey));
            }
        }
        //lines
        LinkedHashMap<Integer, Page.LineContext> lines = new LinkedHashMap<>();
        for (String key: config.getValues(false).keySet()) {//遍历所有的show-xxx
            if (key.startsWith("show-")) {
                int num = Integer.parseInt(key.substring(5));
                String msg = CoreApi.convert(config.getString("show-" + num, null));
                if (msg == null) {//show-xxx为null
                    ConfigApi.log(CorePlugin.pn, get(600, num).getText());
                    continue;
                }
                FancyMessage line = FormatApi.load(msg, (MemorySection) config.get("info-" + num));
                if (line == null) {//info-xxx配置错误
                    ConfigApi.log(CorePlugin.pn, get(605, num).getText());
                    continue;
                }
                line.fix();//优化修正
                Page.LineContext lc = new Page.LineContext(num, line);
                lines.put(num, lc);
            }
        }
        //pageList
        List<Page.PageContext> pageList = new ArrayList<>();
        for (int index = 1;index<=pageMax;index++) {
            String s = config.getString("page-"+index);
            if (s == null) s = "";
            String[] ss = s.split(" ");
            List<Integer> list = new ArrayList<Integer>();
            pageList.add(new Page.PageContext(index, s, list));
            for (String check:ss) {
                if (check.isEmpty()) continue;
                try {
                    int line = Integer.parseInt(check);
                    list.add(line);
                } catch (Exception e) {
                    ConfigApi.log(CorePlugin.pn, get(610, check).getText());
                    list.clear();
                    break;
                }
            }
        }
        return new PageImpl(plugin, page, enable, pageMax, listSize, refresh, per, fillEmpty, handleTip, record, listInfo, maps, pageList, lines);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#save(com.fyxridd.lib.core.api.inter.Page)
     */
    public static boolean save(Page page) {
        //基本信息
        YamlConfiguration config = new YamlConfiguration();
        config.set("pageMax", page.getPageMax());
        config.set("listSize", page.getListSize());
        config.set("refresh", page.isRefresh());
        if (page.getMaps() != null) {
            List<String> mapsList = new ArrayList<String>();
            for (Page.MapInfo mi:page.getMaps().values()) mapsList.add(mi.getKeyName()+" "+mi.getPlugin()+":"+mi.getKey());
            config.set("maps", mapsList);
        }
        //页面信息
        for (Page.PageContext pc:page.getPageList()) {
            int num = pc.getNum();
            config.set("page-"+num, pc.getText());
        }
        //行信息
        for (Page.LineContext lc:page.getLines().values()) {
            FormatApi.save(lc.getNum(), config, lc.getMsg());
        }
        //保存到文件
        String path = CoreApi.pluginPath+ File.separator+page.getPlugin()+File.separator+"show"+File.separator+page.getPage()+".yml";
        File file = new File(path);
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            ConfigApi.log(CorePlugin.pn, get(640).getText());
            return false;
        }
        return CoreApi.saveConfigByUTF8(config, file);
    }

    @Override
    public String getName() {
        return FUNC_NAME;
    }

    @Override
    public boolean isOn(String name, String data) {
        return true;
    }

    /**
     * data:<br>
     * 'p b' 返回上一页<br>
     * 'p e' 退出页面<br>
     * 'p/l p/n/f/l' 页面/列表 前一页/后一页/第一页/最后页<br>
     * 'p/l to 页面' 页面/列表 前往指定页<br>
     * 's 页面名' 显示自定义页面<br>
     */
    @Override
    public void onOperate(Player p, String... args) {
        if (args.length > 0) {
            try {
                //显示自定义页面
                if (args.length == 2 && args[0].equalsIgnoreCase("s")) {
                    show(CoreMain.showManager, args[1], p, CorePlugin.pn, args[1], null, null, 1, 1, null, null, null);
                    return;
                }

                //当前没有查看的页面
                PlayerContext pc = playerContextHash.get(p);
                if (pc == null) {
                    tip(p, get(665), true);
                    return;
                }
                //检测操作
                int length = args.length;
                //变量不定长
                if (length >= 2) {
                    if (args[0].equalsIgnoreCase("p")) {
                        if (args[1].equalsIgnoreCase("to")) {
                            int page;
                            if (length == 2) page = 1;
                            else page = Integer.parseInt(args[2]);
                            toPage(p, page, true);
                            return;
                        }
                    } else if (args[0].equalsIgnoreCase("l")) {
                        if (args[1].equalsIgnoreCase("to")) {
                            int page;
                            if (length == 2) page = 1;
                            else page = Integer.parseInt(args[2]);
                            toListPage(p, page, true);
                            return;
                        }
                    }
                }
                //变量定长
                switch (length) {
                    case 2:
                        if (args[0].equalsIgnoreCase("p")) {
                            if (args[1].equalsIgnoreCase("p")) {//前一页
                                toPage(p, pc.pageNow - 1, false);
                                return;
                            } else if (args[1].equalsIgnoreCase("n")) {//后一页
                                toPage(p, pc.pageNow + 1, false);
                                return;
                            } else if (args[1].equalsIgnoreCase("f")) {//第一页
                                Page page = getPage(pc.plugin, pc.pageName);
                                if (page == null) return;//异常
                                toPage(p, 1, false);
                                return;
                            } else if (args[1].equalsIgnoreCase("l")) {//最后页
                                Page page = getPage(pc.plugin, pc.pageName);
                                if (page == null) return;//异常
                                toPage(p, page.getPageMax(), false);
                                return;
                            } else if (args[1].equalsIgnoreCase("b")) {//返回上一页
                                back(p);
                                return;
                            } else if (args[1].equalsIgnoreCase("e")) {//退出页面
                                exit(p, true);
                                return;
                            }
                        } else if (args[0].equalsIgnoreCase("l")) {
                            if (args[1].equalsIgnoreCase("p")) {//列表前一页
                                toListPage(p, pc.listNow - 1, false);
                                return;
                            } else if (args[1].equalsIgnoreCase("n")) {//列表后一页
                                toListPage(p, pc.listNow + 1, false);
                                return;
                            } else if (args[1].equalsIgnoreCase("f")) {//列表第一页
                                Page page = getPage(pc.plugin, pc.pageName);
                                if (page == null) return;//异常
                                toListPage(p, 1, false);
                                return;
                            } else if (args[1].equalsIgnoreCase("l")) {//列表最后页
                                int listMax;
                                if (pc.list == null) listMax = 0;
                                else listMax = pc.list.getMaxPage(pc.listSize);
                                toListPage(p, listMax, false);
                                return;
                            }
                        }
                        break;
                }
            } catch (NumberFormatException e) {//数字格式错误
                tip(p, get(126), true);
                return;
            }  catch (Exception e) {//操作异常
                tip(p, get(410), true);
                return;
            }
        }
        //输入格式错误
        ShowApi.tip(p, get(5), true);
    }

    /**
     * @see com.fyxridd.lib.core.api.ShowApi#isInPage(org.bukkit.entity.Player)
     */
    public static boolean isInPage(Player p) {
        return playerContextHash.containsKey(p);
    }

    @Override
    public void show(PlayerContext pc) {
        ShowManager.show(pc.callback, pc.obj, pc.p, pc.plugin, pc.pageName, pc.list, pc.data, pc.pageNow,
                pc.listNow, pc.front, pc.behind, pc.itemHash);
    }

    /**
     * 前往指定页
     * @param p 玩家
     * @param page 页面号
     * @param tip 成功时是否提示(失败时必然提示)
     */
    private void toPage(Player p, int page, boolean tip) {
        PlayerContext pc = playerContextHash.get(p);
        //当前没有查看的页面
        if (pc == null) {
            tip(p, get(665), true);
            return;
        }
        Page pa = getPage(pc.plugin, pc.pageName);
        if (pa == null) return;//异常
        if (page < 1 || page > pa.getPageMax()) {//页面超出范围
            tip(p, get(685), true);
            return;
        }
        if (page == pc.pageNow) {//已经处于这一页了
            tip(p, get(695), true);
            return;
        }
        //成功
        if (tip) {
            List<FancyMessage> list = new ArrayList<FancyMessage>();
            list.add(get(700, page));
            tipHash.put(p, list);
        }
        pc.pageNow = page;
        reShow(pc);
    }

    /**
     * 前往列表指定页
     * @param p 玩家
     * @param page 列表页面号
     * @param tip 成功时是否提示(失败时必然提示)
     */
    private void toListPage(Player p, int page, boolean tip) {
        PlayerContext pc = playerContextHash.get(p);
        //当前没有查看的页面
        if (pc == null) {
            tip(p, get(665), true);
            return;
        }
        ShowList list = pc.list;
        if (list == null) {//页面没有列表
            tip(p, get(670), true);
            return;
        }
        int listMax = list.getMaxPage(pc.listSize);//列表最大页
        if (page < 1 || page > listMax) {//页面超出范围
            tip(p, get(685), true);
            return;
        }
        if (page == pc.listNow) {//已经处于这一页了
            tip(p, get(695), true);
            return;
        }
        //成功
        if (tip) {
            List<FancyMessage> result = new ArrayList<FancyMessage>();
            result.add(get(705, page));
            tipHash.put(p, result);
        }
        pc.listNow = page;
        reShow(pc);
    }

    private void loadConfig() {
        YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);

        //inCancelChat
        inCancelChat = config.getBoolean("show.cancel.chatCancel");

        //deadLoopLevel
        deadLoopLevel = config.getInt("show.deadLoopLevel");
        if (deadLoopLevel < 1) {
            deadLoopLevel = 1;
            ConfigApi.log(CorePlugin.pn, "show.deadLoopLevel < 1");
        }
        //cancelInteract,cancelAnimation,cancelChat
        cancelInteract = config.getBoolean("show.cancel.interact");
        cancelAnimation = config.getBoolean("show.cancel.animation");
        cancelAttack = config.getBoolean("show.cancel.attack");
        cancelChat = config.getBoolean("show.cancel.chat");
        cancelShoot = config.getBoolean("show.cancel.shoot");
        //maxBackPage
        maxBackPage = config.getInt("show.maxBackPage");
        if (maxBackPage < 0) {
            maxBackPage = 0;
            ConfigApi.log(CorePlugin.pn, "show.maxBackPage < 0");
        }
        //line
        line = config.getInt("show.line");
        if (line < 1) {
            line = 1;
            ConfigApi.log(CorePlugin.pn, "show.line < 1");
        }
        //add
        add = get(730);
        //operateTipMenu
        operateTipMenu = get(710);
        //operateTipEmpty
        operateTipEmpty = get(715);
        //pageControl
        pageControl = get(720);
        //listControl
        listControl = get(725);
    }

    private static FancyMessage get(int id) {
        return FormatApi.get(CorePlugin.pn, id);
    }

    private static FancyMessage get(int id, Object... args) {
        return FormatApi.get(CorePlugin.pn, id, args);
    }
}
