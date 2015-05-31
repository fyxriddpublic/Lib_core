package lib.core.api.inter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

/**
 * 玩家页面上下文(当前成功正在查看的)<br>
 * 不提供set方法(其它类可以调用get方法获取,但不能修改值,防止影响页面返回功能)
 */
public class PlayerContext {
    public Object obj;//功能自定义的额外保存数据
    public ShowInterface callback;//回调类,可为null
    public Player p;//玩家
    public String plugin;//插件名
    public String pageName;//页面名
    public int listSize;//列表分页大小
    public ShowList<Object> list;//列表
    public HashMap<String, Object> data;//名称-值的映射表
    public int pageNow;//当前页
    public int listNow;//列表当前页
    public HashMap<String, ItemStack> itemHash;//名称-物品的映射表

    public List<FancyMessage> front, behind;//附加显示的行列表

    public PlayerContext() {
    }

    public PlayerContext(Object obj, ShowInterface callback, Player p, String plugin, String pageName, int listSize,
                         ShowList<Object> list, HashMap<String, Object> data, int pageNow, int listNow,
                         List<FancyMessage> front, List<FancyMessage> behind, HashMap<String, ItemStack> itemHash) {
        this.obj = obj;
        this.callback = callback;
        this.p = p;
        this.plugin = plugin;
        this.pageName = pageName;
        this.listSize = listSize;
        this.list = list;
        this.data = data;
        this.pageNow = pageNow;
        this.listNow = listNow;
        this.front = front;
        this.behind = behind;
        this.itemHash = itemHash;
    }

    @Override
    public PlayerContext clone() {
        return new PlayerContext(obj, callback, p, plugin, pageName, listSize, list, data, pageNow, listNow, front, behind, itemHash);
    }
}
