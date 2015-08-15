package com.fyxridd.lib.core.api;

import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.packetwrapper.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.fyxridd.lib.core.*;
import com.fyxridd.lib.core.api.inter.FancyMessage;
import com.fyxridd.lib.core.api.inter.InputHandler;
import com.fyxridd.lib.core.api.nbt.AttributeStorage;
import net.minecraft.server.v1_8_R2.*;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoreApi {
    //服务端所在的文件夹路径
    public static String serverPath;
    //插件文件夹路径
    public static String pluginPath;
    //服务端版本
    public static String serverVer;

    public static final long SECONDS = 1000;
    public static final long MINUTE = SECONDS*60;
    public static final long HOUR = MINUTE*60;
    public static final long DAY = HOUR*24;
    public static final Random Random = new Random();
    private static final String VERSION_PATTERN = "\\(MC: [0-9.]{5}\\)";

    private static final String PatternStr = "&[0123456789abcdeflmnor]";
    private static Pattern pattern = Pattern.compile(PatternStr);

    private static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    /**
     * 获取生物上保存的数据
     * @param le 生物
     * @param uid uuid
     * @return 保存的数据,可能为null
     */
    public static String getData(LivingEntity le, UUID uid) {
        EntityLiving el = ((CraftLivingEntity) le).getHandle();
        AttributeInstance ai = el.getAttributeInstance(GenericAttributes.maxHealth);
        if (ai == null) return null;
        AttributeModifier am = ai.a(uid);
        if (am == null) return null;
        else return am.b();
    }

    /**
     * 在生物上保存数据
     * @param le 生物
     * @param uid uuid
     * @param data 数据
     * @return 是否成功
     */
    public static boolean setData(LivingEntity le, UUID uid, String data) {
        try {
            EntityLiving el = ((CraftLivingEntity) le).getHandle();
            AttributeInstance ai = el.getAttributeInstance(GenericAttributes.maxHealth);
            if (ai == null) return false;
            AttributeModifier am = new AttributeModifier(uid, data, 0, 0);
            //先删旧的(没有也不会出错)
            ai.c(am);
            //再添加新的
            ai.b(am);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 切换门的开关状态
     * 版本更新后可能异常
     * @param b 门两个方块中的一个,可为null(null时无效果)
     */
    public static void toggleDoor(Block b) {
        if (b == null) return;

        //方块检测
        if (b.getData() >= (byte)8) b = b.getRelative(BlockFace.DOWN);
        if (b == null || (b.getType() != Material.IRON_DOOR_BLOCK && b.getType() != Material.WOODEN_DOOR)) return;

        if (b.getData() <= (byte)3) b.setData((byte) (b.getData()+4));
        else b.setData((byte) (b.getData()-4));
        b.getState().update(true);
        //声音
        try {
            b.getWorld().playEffect(b.getRelative(BlockFace.UP).getLocation(), Effect.DOOR_TOGGLE, 0);
        } catch (Exception e) {
            //声音可能不存在
        }
    }

    /**
     * 获取攻击的玩家
     * @param damager 直接伤害者
     * @return 如果直接伤害者是玩家直接返回玩家;如果是发射物并且发射物是玩家发出的则返回发射者;其它情况均返回null
     */
    public static Player getPlayerDamager(Entity damager) {
        if (damager == null) return null;
        else if (damager instanceof Player) return (Player) damager;
        else if (damager instanceof Projectile) {
            ProjectileSource ps = ((Projectile) damager).getShooter();
            if (ps != null && ps instanceof Player) return (Player) ps;
            else return null;
        }else return null;
    }

    /**
     * 给目标玩家添加生命值并进行提示<br>
     * 不会超过玩家的生命上限
     * @param p 玩家,可为null(null时无效果)
     * @param add 增加的生命,<=0时无效果
     */
    public static void addHealth(Player p, double add) {
        if (p == null || add <= 0) return;

        double origin = p.getHealth();
        p.setHealth(Math.min(p.getMaxHealth(), p.getHealth()+add));
        ShowApi.tip(p, get(50, (p.getHealth() - origin)), false);
    }

    /**
     * 其它聊天插件不能自行给玩家发送聊天信息或调用ShowApi.tip方法,而要调用此方法,否则不会有延时显示聊天信息的功能
     * @param p 玩家,可为null(null时无效果)
     * @param msg 聊天信息,可为null(null时无效果)
     * @param force 是否强制显示
     */
    public static void addChat(Player p, FancyMessage msg, boolean force) {
        CoreMain.chatManager.addChat(p, msg, force);
    }

    /**
     * 以UTF-8格式读入文本文件
     * @param is 输入流
     * @return 字符串形式的文件内容,异常返回null
     */
    public static String getDataAsText(InputStream is) {
        try {
            StringBuilder sb = new StringBuilder();

            BufferedReader br = new BufferedReader(new java.io.InputStreamReader(is, Charset.forName("utf-8")));
            while (br.ready()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(br.readLine());
            }
            br.close();

            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 获取保存在物品上的数据
     * @param is 物品,不为null
     * @param key 唯一的key,不为null
     * @return 数据,不存在返回null
     */
    public static String getData(ItemStack is, UUID key) {
        AttributeStorage as = AttributeStorage.newTarget(is, key);
        return as.getData();
    }

    /**
     * 设置保存在物品上的数据
     * @param is 物品,不为null
     * @param key 唯一的key,不为null
     * @param data 数据,null表示删除
     * @return 设置后的物品
     */
    public static ItemStack setData(ItemStack is, UUID key, String data) {
        AttributeStorage as = AttributeStorage.newTarget(is, key);
        as.setData(data);
        return as.getTarget();
    }

    /**
     * @see Info#getInfo(String, String)
     */
    public static String getInfo(String name, String flag) {
        return CoreMain.info.getInfo(name, flag);
    }

    /**
     * @see Info#setInfo(String, String, String)
     */
    public static void setInfo(String name, String flag, String data) {
        CoreMain.info.setInfo(name, flag, data);
    }

    /**
     * 在控制台显示信息(优先进行有颜色显示)
     */
    public static void sendConsoleMessage(Object message) {
        if (Bukkit.getConsoleSender() != null) Bukkit.getConsoleSender().sendMessage(message.toString());
        else info(message);
    }

    public static void info(Object message) {
        CorePlugin.instance.getLogger().info(message.toString());
    }

    public static void warn(Object message) {
        CorePlugin.instance.getLogger().warning(message.toString());
    }

    public static void severe(Object message) {
        CorePlugin.instance.getLogger().severe(message.toString());
    }

    /**
     * 颜色字符&转换<br>
     * 如'&a'或'&l'会转换,但'&z'不会转换
     * @param msg 要转换的信息,null时会返回null
     * @return 转换后的字符串
     */
    public static String convert(String msg){
        if (msg == null) return null;

        Matcher m = pattern.matcher(msg);
        StringBuffer sb = new StringBuffer();
        while (m.find()) m.appendReplacement(sb, m.group().replace("&", "\u00A7"));
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * @see com.fyxridd.lib.core.InputManager#register(org.bukkit.entity.Player, InputHandler)
     */
    public static void registerInput(Player p, InputHandler inputHandler) {
        InputManager.register(p, inputHandler);
    }

    /**
     * @see InputManager#del(org.bukkit.entity.Player)
     */
    public static void delInput(Player p) {
        InputManager.del(p);
    }

    /**
     * @see InputManager#del(org.bukkit.entity.Player, boolean)
     */
    public static void delInput(Player p, boolean tip) {
        InputManager.del(p, tip);
    }

    /**
     * 输出调试信息<br>
     *     是否输出由配置文件中的debug选项而定
     * @param msg 调试信息,可为null
     */
    public static void debug(String msg) {
        if (CoreMain.debug) System.out.println("["+getSimpleDateTime()+"] "+msg);
    }

    /**
     * 计算字符串表达式<br>
     * 操作数只能是整数<br>
     * 运算符只能包含+-*&frasl;()
     * @param exp 要计算的表达式
     * @return 计算的结果
     */
    public static int calc(String exp) {
        return Expression.calc(exp);
    }

    /**
     * @param file 文件
     * @return 行列表
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws Exception
     */
    public static List<String> readLinesByUTF8(File file) throws FileNotFoundException, IOException, Exception {
        BufferedReader br = null;
        try {
            List<String> lines = new ArrayList<String>();
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("utf-8")));
            String line = br.readLine();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }
            return lines;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 读取utf-8编码的yml文件
     * @param file 要读取的yml文件
     * @return file为null或异常返回null
     */
    public static YamlConfiguration loadConfigByUTF8(File file) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), Charset.forName("utf-8"));
            StringBuilder builder = new StringBuilder();
            BufferedReader input = new BufferedReader(reader);
            try
            {
                String line;
                while ((line = input.readLine()) != null) {
                    builder.append(line);
                    builder.append('\n');
                }
            } finally {
                input.close();
            }

            config.loadFromString(builder.toString());
            return config;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 保存配置到utf-8编码的yml文件
     * @param config 要保存的配置
     * @param file 要保存的yml文件
     * @return 是否成功
     */
    public static boolean saveConfigByUTF8(YamlConfiguration config, File file) {
        BufferedWriter output = null;
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("utf-8"));
            output = new BufferedWriter(writer);
            String data = config.saveToString();
            output.write(data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (output != null) try {
                output.flush();
                output.close();
            } catch (IOException e) {
                return false;
            }
        }
    }

    /**
     * 字符串转u码
     * @param str 字符串
     * @return u码
     */
    public static String strToU(String str) {
        if (str == null || str.isEmpty()) return str;
        StringBuffer sb = new StringBuffer();
        for (char c:str.toCharArray()) {
            sb.append(" "+Integer.toHexString(c));
        }
        return sb.toString();
    }

    /**
     * u码转字符串
     * @param str u码
     * @return 字符串
     */
    public static String uToStr(String str) {
        if (str == null || str.isEmpty()) return str;
        StringBuffer sb = new StringBuffer();
        try {
            for (String s:str.split(" ")) {
                if (s.isEmpty()) continue;
                int i = Integer.parseInt(s, 16);
                sb.append((char)i);
            }
        } catch (Exception e) {
        }
        return sb.toString();
    }

    /**
     * 获取与某个位置最近的玩家
     * @param l 位置
     * @return 不存在返回null
     * @throws IllegalArgumentException 如果l为null
     */
    public static Player getNearestPlayer(Location l) {
        Validate.notNull(l);

        Player p = null;
        double distance = 9999;
        for (Player pp:l.getWorld().getPlayers()) {
            if (p == null) {
                p = pp;
                distance = pp.getLocation().distance(l);
            }else {
                double dis = pp.getLocation().distance(l);
                if (dis < distance) {
                    p = pp;
                    distance = dis;
                }
            }
        }
        return p;
    }

    /**
     * 获取某个位置附近的玩家
     * @param l 位置,可为null(null时返回空列表)
     * @param range 范围,>=0.0(<0.0时返回空列表)
     * @return 不为null
     */
    public static List<Player> getNearbyPlayers(Location l, double range) {
        List<Player> result = new ArrayList<Player>();
        if (l == null || range < 0.0) return result;
        for (Player p:l.getWorld().getPlayers()) {
            if (l.distance(p.getLocation()) <= range) result.add(p);
        }
        return result;
    }

    /**
     * 给附近指定范围内的所有玩家发送方块更新包
     * @param loc 位置,可为null(null时无效果)
     * @param range 范围,>=0.0(<0.0时无效果)
     * @param block 方块,可为null(null时无效果)
     */
    public static void updateBlock(Location loc, double range, Block block) {
        if (loc == null || range < 0.0 || block == null) return;

        WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange();
        wrapper.setLocation(new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        wrapper.setBlockData(WrappedBlockData.createData(block.getType(), block.getData()));
        PacketContainer pc = wrapper.getHandle();
        for (Player p:CoreApi.getNearbyPlayers(loc, range)) {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(p, pc, true);
            } catch (InvocationTargetException e1) {
            }
        }
    }

    /**
     * 改变精度
     * @param num 需要改变的实数
     * @param accuracy 精确度,表示小数点后保留的位数,>=0,小于0会被当作0
     * @return 改变精度后的实数(返回的小数部分长度可能比精确度小)
     */
    public static double getDouble(double num,int accuracy) {
        if (accuracy < 0) accuracy = 0;
        String s = String.valueOf(num);
        if (s.split("\\.").length == 2) {
            String[] ss = s.split("\\.");
            return Double.parseDouble(ss[0]+"."+ss[1].substring(0, Math.min(accuracy, ss[1].length())));
        }else return num;
    }

    /**
     * @see com.fyxridd.lib.core.Tps#getTps()
     */
    public static double getTps() {
        return Tps.getTps();
    }

    /**
     * 从"plugin.yml"里获取插件版本
     * @param plugin 插件对应的jar文件
     * @return 插件版本字符串,出错返回null
     */
    public static String getPluginVersion(File plugin) {
        JarInputStream jis = null;
        try {
            jis = new JarInputStream(new FileInputStream(plugin));
            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                String fileName = entry.getName();
                if (fileName.equalsIgnoreCase("plugin.yml")) {
                    YamlConfiguration config = new YamlConfiguration();
                    config.load(jis);
                    return config.getString("version",null);
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (InvalidConfigurationException e) {
        } finally {
            try {
                if (jis != null) jis.close();
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取客户端版本
     * @param server
     * @return 如"1.4.7"这种模式的,出错返回null
     */
    public static String getMcVersion(Server server) {
        try {
            Pattern p = Pattern.compile(VERSION_PATTERN);
            Matcher m = p.matcher(server.getBukkitVersion());
            if (m.find()) {
                String result = m.group();
                if (result != null && !result.trim().isEmpty()) return result.substring(result.indexOf(" ")+1,result.indexOf(")"));
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 获取服务器的端口
     * @param server
     * @return 端口
     */
    public static int getPort(Server server) {
        return server.getPort();
    }
    /**
     * 将整数转换为byte数组
     * @param i 整数
     * @return byte数组,长度为4
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * 将byte数组转换为整数
     * @param byteArray byte数组,长度为4
     * @return 对应的整数
     */
    public static int byteArrayToInt(byte[] byteArray) {
        int result = 0;
        int b0 = byteArray[0];
        int b1 = byteArray[1];
        int b2 = byteArray[2];
        int b3 = byteArray[3];
        result = result | (b0 << 24);
        result = result | (b1 << 16 & 0x00FF0000);
        result = result | (b2 << 8 & 0x0000FF00);
        result = result | b3 & 0x000000FF;
        return result;
    }

    /**
     * 字节数组转换为字符串
     * @param target 字节数组
     * @return 字符串
     */
    public static String charsToStr(char[] target) {
        StringBuffer buf = new StringBuffer();
        for (int i=0;i<target.length;i++)
            buf.append(target[i]);
        return buf.toString();
    }

    /**
     * 字符串转换为字节数组
     * @param str 字符串
     * @return 字节数组
     */
    public static char[] StrToChars(String str) {
        char[] buf = new char[str.length()];
        for (int i=0;i<str.length();i++)
            buf[i] = str.charAt(i);
        return buf;
    }

    /**
     * 获取物品lore中包含指定信息的行
     * @param itemStack 物品,不为null
     * @param message 信息,不为null
     * @return 返回第一次检测成功的行,没有则返回null
     */
    public static String getLine(ItemStack itemStack, String message) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return null;
        List<String> lore = itemMeta.getLore();
        if (lore == null) return null;
        for (String s:lore) {
            if (s.indexOf(message) != -1) return s;
        }
        return null;
    }

    /**
     * 获取当前时间的简单格式的日期时间显示(格式'yyyy-MM-dd HH:mm')
     * @return 出错返回空字符串
     */
    public static String getSimpleDateTime() {
        return getSimpleDateTime(new Date().getTime());
    }

    /**
     * 获取简单格式的日期时间显示(格式'yyyy-MM-dd HH:mm')
     * @param time 开始计算的时间,单位毫秒
     * @return 出错返回空字符串
     */
    public static String getSimpleDateTime(long time) {
        return getDateTime("yyyy-MM-dd HH:mm", time);
    }

    /**
     * 获取当前时间的适合日志记录文件名格式的日期时间显示(格式'yyyy-MM-dd HH-mm-ss')
     * @return 出错返回空字符串
     */
    public static String getLogDateTime() {
        return getLogDateTime(new Date().getTime());
    }

    /**
     * 获取适合日志记录文件名格式的日期时间显示(格式'yyyy-MM-dd HH-mm-ss')
     * @param time 开始计算的时间,单位毫秒
     * @return 出错返回空字符串
     */
    public static String getLogDateTime(long time) {
        return getDateTime("yyyy-MM-dd HH-mm-ss", time);
    }

    /**
     * 获取日期时间显示
     * @param format 格式
     * @param time 开始计算的时间,单位毫秒
     * @return 出错返回空字符串
     */
    public static String getDateTime(String format, long time) {
        try {
            return new SimpleDateFormat(format).format(new Date(time));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 把变量重新组合成字符串
     * @param args 变量
     * @param separator 变量之间用这个分隔
     * @param start 开始位置,0-(args.length-1)
     * @param end 结束位置,0-args.length
     * @return 组合后的字符串
     */
    public static String combine(String[] args, String separator, int start, int end) {
        String result = "";
        for (int i=0;i<args.length;i++) {
            if (i < start) continue;
            if (i > end) break;
            if (i > start) result += separator;
            result += args[i];
        }
        return result;
    }

    /**
     * 获取实体类型
     * @param s 实体类型定义字符串,可以是实体ID或者实体对应的enum值(非实体名)
     * @return 对应的实体类型,出错返回null
     */
    @SuppressWarnings("deprecation")
    public static EntityType getEntityType(String s) {
        EntityType entityType = null;
        try {
            entityType = EntityType.fromId(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            try {
                entityType = EntityType.valueOf(EntityType.class, s);
            } catch (Exception e1) {
            }
        }
        return entityType;
    }

    /**
     * 获取Material
     * @param s Material定义字符串,可以是方块或物品类型名或ID,可为null(null时返回null)
     * @return 对应的Material,出错返回null
     */
    public static Material getMaterial(String s) {
        if (s == null) return null;
        try {
            try {
                int id = Integer.parseInt(s);
                return Material.getMaterial(id);
            }catch (NumberFormatException e) {
                return Material.getMaterial(s);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 指定一个方向,让实体向这个方向轻轻喷射一下
     * @param entity 实体
     * @param from 用来确定方向
     * @param to 用来确定方向
     */
    public static void eject(Entity entity, Location from, Location to) {
        org.bukkit.util.Vector v = to.subtract(from).toVector();
        double length = v.length();
        v.setX(v.getX()/length);
        v.setY(v.getY()/length);
        v.setZ(v.getZ()/length);
        entity.setVelocity(v);
    }

    /**
     * 指定一个方向,让实体向这个方向喷射
     * @param entity 实体
     * @param from 用来确定方向
     * @param to 用来确定方向
     * @param multiply 乘以多少倍
     */
    public static void eject(Entity entity, Location from, Location to, double multiply) {
        org.bukkit.util.Vector v = to.subtract(from).toVector();
        double length = v.length()/multiply;
        v.setX(v.getX()/length);
        v.setY(1.5);
        v.setZ(v.getZ()/length);
        entity.setVelocity(v);
    }

    /**
     * 让实体随机水平方向,垂直方向偏上轻轻喷射一下
     * @param entity 实体
     */
    public static void ejectRandom(Entity entity) {
        org.bukkit.util.Vector v = new org.bukkit.util.Vector(1.0, 0.3, 1.0);
        double d = Random.nextInt(11)-5;
        if (d == 0) v.setX(0);
        else v.setX(v.getX()/d);
        d = Random.nextInt(11)-5;
        if (d == 0) v.setZ(0);
        else v.setZ(v.getZ()/d);
        double length = v.length();
        v.setX(v.getX() / length);
        v.setZ(v.getZ()/length);
        entity.setVelocity(v);
    }

    /**
     * 方向随机偏移
     * @param originYaw 方向的yaw
     * @param originPitch 方向的pitch
     * @param accuracy 散度,>=0(如1.2)
     * @return 新的偏移后的方向
     */
    public static org.bukkit.util.Vector getRandomVector(double originYaw, double originPitch, double accuracy) {
        double yaw = Math.toRadians(-originYaw - 90.0F);
        double pitch = Math.toRadians(-originPitch);
        double[] spread = { 1.0D, 1.0D, 1.0D };
        for (int t = 0; t < 3; t++) spread[t] = ((CoreApi.Random.nextDouble() - CoreApi.Random.nextDouble()) * accuracy * 0.1D);
        double x = Math.cos(pitch) * Math.cos(yaw) + spread[0];
        double y = Math.sin(pitch) + spread[1];
        double z = -Math.sin(yaw) * Math.cos(pitch) + spread[2];
        org.bukkit.util.Vector dirVel = new org.bukkit.util.Vector(x, y, z);
        return dirVel.normalize();
    }

    /**
     * 检测字符串是否合法,合法表示只包含英文,数字,下划线
     * @param s 字符串
     * @return 是否合法
     */
    public static boolean isValid(String s) {
        return s.matches("^[\\da-zA-Z_]*$");
    }

    /**
     * 分割字符串
     * @param s 字符串
     * @param maxLength 每行最大长度
     * @return
     */
    public static List<String> separateLines(String s, int maxLength) {
        List<String> result = new LinkedList<String>();
        int index = 0;
        while (index < s.length()) {
            result.add(s.substring(index, Math.min(s.length(), index+maxLength)));

            index += maxLength;
        }
        return result;
    }

    /**
     * 分割字符串
     * @param lore 字符串列表
     * @param maxLength 每行最大长度
     * @return
     */
    public static void separateLines(List<String> lore, int maxLength) {
        int index = 0;
        while (index < lore.size()) {
            List<String> lore1 = separateLines(lore.get(index), maxLength);
            if (lore1.size() > 1) {
                lore.set(index, lore1.get(0));
                for (int i=1;i<lore1.size();i++) lore.add(index+i, lore1.get(i));
                index += lore1.size();
                continue;
            }

            index ++;
        }
    }

    /**
     * @see RealName#getRealName(CommandSender, String)
     */
    public static String getRealName(CommandSender sender, String name) {
        return RealName.getRealName(sender, name);
    }

    /**
     * 在指定位置显示闪电+声音
     * @param loc 位置
     * @param range 显示的范围
     */
    public static void strikeLightning(Location loc, int range) {
        CraftWorld cw = (CraftWorld)loc.getWorld();
        net.minecraft.server.v1_8_R2.World w = cw.getHandle();
        EntityLightning lightning = new EntityLightning(w, loc.getX(), loc.getY(), loc.getZ());
        PacketPlayOutSpawnEntityWeather pc = new PacketPlayOutSpawnEntityWeather(lightning);
        PacketPlayOutNamedSoundEffect pc1 = new PacketPlayOutNamedSoundEffect("random.explode", loc.getX(), loc.getY(), loc.getZ(), 2f, 0f);
        PacketPlayOutNamedSoundEffect pc2 = new PacketPlayOutNamedSoundEffect("ambient.weather.thunder", loc.getX(), loc.getY(), loc.getZ(), 2f, 0f);
        for (Player p:loc.getWorld().getPlayers()) {
            if (p.getLocation().distance(loc) < range) {
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(pc);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(pc1);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(pc2);
            }
        }
    }

    /**
     * 在指定位置显示闪电
     * @param loc 位置
     * @param range 显示的范围
     */
    public static void strikeLightningEffect(Location loc, int range) {
        CraftWorld cw = (CraftWorld)loc.getWorld();
        net.minecraft.server.v1_8_R2.World w = cw.getHandle();
        EntityLightning lightning = new EntityLightning(w, loc.getX(), loc.getY(), loc.getZ());
        PacketPlayOutSpawnEntityWeather pc = new PacketPlayOutSpawnEntityWeather(lightning);
        for (Player p:loc.getWorld().getPlayers()) {
            if (p.getLocation().distance(loc) < range) {
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(pc);
            }
        }
    }

    /**
     * 给玩家发送信息(不重要的)<br>
     * 玩家存在且在线时发送
     * @param name 准确的玩家名,不为null
     * @param msg 信息,不为null
     * @param force 是否强制
     */
    public static void sendMsg(String name, String msg, boolean force) {
        Player p = Bukkit.getServer().getPlayerExact(name);
        if (p != null && p.isOnline()) ShowApi.tip(p, msg, force);
    }

    /**
     * 给玩家发送信息(不重要的)<br>
     * 玩家存在且在线时发送
     * @param name 准确的玩家名,不为null
     * @param msg 信息,不为null
     * @param force 是否强制
     */
    public static void sendMsg(String name, FancyMessage msg, boolean force) {
        Player p = Bukkit.getServer().getPlayerExact(name);
        if (p != null && p.isOnline()) ShowApi.tip(p, msg, force);
    }

    /**
     * @see #sendMsg(org.bukkit.Location, double, boolean, com.fyxridd.lib.core.api.inter.FancyMessage, boolean)
     */
    public static void sendMsg(Location l, double range, boolean nearest, String msg, boolean force) {
        FancyMessage fancyMessage = new FancyMessageImpl(msg);
        sendMsg(l, range, nearest, fancyMessage, force);
    }

    /**
     * 给附近玩家显示信息
     * @param l 位置
     * @param range 范围,>0
     * @param nearest true时只给最近的一个玩家显示信息
     * @param msg 信息
     * @param force 是否强制显示
     */
    public static void sendMsg(Location l, double range, boolean nearest, FancyMessage msg, boolean force) {
        Player p = null;
        double d = 0;
        double temp;
        for (Player tar:l.getWorld().getPlayers()) {
            if (tar.isOnline() && (temp=tar.getLocation().distance(l)) <= range) {
                if (!nearest) ShowApi.tip(tar, msg, force);
                else {
                    if (p == null) {
                        p = tar;
                        d = temp;
                    }else {
                        if (temp < d) {
                            p = tar;
                            d = temp;
                        }
                    }
                }
            }
        }
        if (p != null) ShowApi.tip(p, msg, force);
    }

    /**
     * 获取附魔
     * @param s 附魔名(非枚举名)或ID
     * @return 附魔,没有返回null
     */
    public static Enchantment getEnchantment(String s) {
        try {
            int id = Integer.parseInt(s);
            return Enchantment.getById(id);
        } catch (NumberFormatException e) {
            return Enchantment.getByName(s);
        }
    }

    /**
     * 获取药效
     * @param s 药效类型名(非枚举名)或ID
     * @return 药效类型,不存在返回null
     */
    public static PotionEffectType getPotionEffectType(String s) {
        PotionEffectType potionEffectType;
        try {
            potionEffectType = PotionEffectType.getById(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            potionEffectType = PotionEffectType.getByName(s);
        }
        return potionEffectType;
    }

    /**
     * 获取墙上的牌子方块所依附的方块
     * @param block 墙上的牌子方块
     * @return 如果方块类型非墙上的牌子或其它异常返回null
     * @throws IllegalArgumentException 如果block为null
     */
    public static Block getSignAttachedBlock(Block block) {
        Validate.notNull(block);

        if (block.getType() != Material.WALL_SIGN) return null;

        int face = block.getData() & 0x7;
        if (face == 3) {
            return block.getRelative(BlockFace.NORTH);
        }
        if (face == 4) {
            return block.getRelative(BlockFace.EAST);
        }
        if (face == 2) {
            return block.getRelative(BlockFace.SOUTH);
        }
        if (face == 5) {
            return block.getRelative(BlockFace.WEST);
        }

        return null;
    }

    /**
     * 获取陷阱门依附的方块
     * @param block 陷阱门方块
     * @return 如果方块类型非陷阱门或其它异常返回null
     * @throws IllegalArgumentException 如果block为null
     */
    public static Block getTrapDoorAttachedBlock(Block block) {
        Validate.notNull(block);

        if (block.getType() != Material.TRAP_DOOR) return null;

        int face = block.getData() & 0x3;
        if (face == 1) {
            return block.getRelative(BlockFace.NORTH);
        }
        if (face == 2) {
            return block.getRelative(BlockFace.EAST);
        }
        if (face == 0) {
            return block.getRelative(BlockFace.SOUTH);
        }
        if (face == 3) {
            return block.getRelative(BlockFace.WEST);
        }

        return null;
    }

    /**
     * 获取活塞的朝向
     * @param block 活塞(扩展)方块
     * @return 异常返回BlockFace.SELF
     * @throws IllegalArgumentException 如果block为null
     */
    public static BlockFace getPistonFacing(Block block) {
        Validate.notNull(block);

        Material type = block.getType();
        if ((type != Material.PISTON_BASE) &&
                (type != Material.PISTON_STICKY_BASE) &&
                (type != Material.PISTON_EXTENSION)) {
            return BlockFace.SELF;
        }

        int face = block.getData() & 0x7;
        switch (face)
        {
            case 0:
                return BlockFace.DOWN;
            case 1:
                return BlockFace.UP;
            case 2:
                return BlockFace.NORTH;
            case 3:
                return BlockFace.SOUTH;
            case 4:
                return BlockFace.WEST;
            case 5:
                return BlockFace.EAST;
        }

        return BlockFace.SELF;
    }

    /**
     * 检测两个位置是否在相同的方块中
     * @param l1 位置1,可为null(null时返回false)
     * @param l2 位置2,可为null(null时返回false)
     * @return 是否在相同的方块中
     */
    public static boolean isInSameBlock(Location l1, Location l2) {
        if (l1 == null || l2 == null) return false;

        return l1.getWorld().equals(l2.getWorld()) && l1.getBlockX() == l2.getBlockX() &&
                l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ();
    }

    /**
     * 检测玩家是否在线,不在线时会进行提示
     * @param p 玩家,可为null(null时不会进行提示)
     * @param tar (精确的)目标玩家名,可为null(null时不会进行提示并且返回null)
     * @return 在线的玩家,不在线返回null
     */
    public static Player checkOnline(Player p, String tar) {
        if (tar == null) return null;

        Player tarP = Bukkit.getPlayerExact(tar);
        if (tarP == null) {
            if (p != null) ShowApi.tip(p, FormatApi.get(CorePlugin.pn, 40, tar), true);
            return null;
        }
        return tarP;
    }

    /**
     * md5
     * @param msg 原内容
     * @return md5后的结果
     */
    public static String md5(String msg) {
        return MD5.GetMD5Code(msg);
    }

    /**
     * 显示特效<br>
     * 显示目标:<br>
     *      1. 目标玩家(p)<br>
     *      2. 附近玩家(entity+range)<br>
     *
     * @param p 目标玩家(null时目标玩家不会显示)
     *
     * @param entity 会获取entity附近range范围内的所有玩家进行显示(null时附近玩家不会显示)
     * @param range 显示范围,单位格(<=0时附近玩家不会显示)
     *
     * @param loc 显示位置
     * @param type 特效类型,enum值
     * @param count 数量
     * @param offset 最大偏移
     * @param longDistance 是否长距离
     */
    public static void showSpec(Player p, Entity entity, int range, Location loc, String type, int count, float offset, boolean longDistance) {
        WrapperPlayServerWorldParticles particles = new WrapperPlayServerWorldParticles();
        particles.setNumberOfParticles(count);
        particles.setParticleType(EnumWrappers.Particle.valueOf(type));
        particles.setLongDistance(longDistance);
        particles.setX((float) loc.getX());
        particles.setY((float) loc.getY());
        particles.setZ((float) loc.getZ());
        particles.setOffsetX(offset);
        particles.setOffsetY(offset);
        particles.setOffsetZ(offset);
        PacketContainer packet = particles.getHandle();

        try {
            //目标玩家
            if (p != null) protocolManager.sendServerPacket(p, packet, true);
            //附近玩家
            if (entity != null && range > 0) {
                for (Entity e : entity.getNearbyEntities(range, range, range)) {
                    if (e instanceof Player) protocolManager.sendServerPacket((Player) e, packet, true);
                }
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static FancyMessage get(int id, Object... args) {
        return FormatApi.get(CorePlugin.pn, id, args);
    }
}
