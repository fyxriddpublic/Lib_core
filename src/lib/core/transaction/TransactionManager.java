package lib.core.transaction;

import lib.core.api.*;
import lib.core.api.inter.FunctionInterface;
import lib.core.api.inter.FancyMessage;
import lib.core.api.inter.Transaction;
import lib.core.api.inter.TransactionUser;
import lib.core.api.event.TimeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Iterator;

public class TransactionManager implements Listener,FunctionInterface {
    private static final String FUNC_NAME = "TransactionManager";

	//玩家名 玩家事务信息
	private static HashMap<String, TransactionUser> transHash = new HashMap<String, TransactionUser>();
	
	public TransactionManager() {
        //注册事件
		Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
        //注册功能
        FuncApi.register(this);
    }

	@EventHandler(priority=EventPriority.NORMAL)
	public void onTime(TimeEvent e) {
		Iterator<String> it = transHash.keySet().iterator();
		while (it.hasNext()) {
			String name = it.next();
			TransactionUser user = transHash.get(name);
			//过期与提示
			user.onTime();
			//检测事务为空
			if (user.isEmpty()) it.remove();//玩家事务为空,删除事务
		}
	}

    @Override
    public String getName() {
        return FUNC_NAME;
    }

    @Override
    public boolean isOn(String name, String data) {
        return true;
    }

    @Override
    public void onOperate(Player p, String... args) {
        operate(p, args);
    }

    /**
     * 获取玩家事务信息
     * @param name 玩家名,不为null
     * @return 玩家事务信息,没有则新建,异常返回null
     */
    public static TransactionUser getTransactionUser(String name) {
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return null;
        //
        if (!transHash.containsKey(name)) transHash.put(name, new TransactionUser(name));
        return transHash.get(name);
    }

    /**
     * 删除玩家的所有事务
     * @param name 玩家名,可为null
     */
    public static void delTransaction(String name) {
        if (name == null) return;
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return;
        //
        TransactionUser tu = transHash.remove(name);
        if (tu != null) tu.delAllTransaction();
    }

    /**
     * 事务处理
     * @param p 玩家,不为null
     * @param args 操作内容
     * @return 操作结果
     */
    private static boolean operate(Player p, String... args) {
        TransactionUser user = transHash.get(p.getName());
        if (user == null) {//当前没有事务
            ShowApi.tip(p, get(1110), true);
            return false;
        }
        long id = user.getRunning();
        Transaction trans = user.getTransaction(id);
        if (trans == null) {//指定的事务不存在或已经结束
            ShowApi.tip(p, get(1105), true);
            return false;
        }
        //执行事务
        trans.onOperate(args);
        return true;
    }

    private static FancyMessage get(int id) {
        return FormatApi.get(CorePlugin.pn, id);
    }
}
