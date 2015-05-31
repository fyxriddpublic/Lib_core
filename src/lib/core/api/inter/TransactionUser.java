package lib.core.api.inter;

import java.util.HashMap;
import java.util.Iterator;

/**
 * 某个玩家的事务信息
 */
public class TransactionUser {
	//计数器,用来生成事务ID
	private static long counter;

    //玩家名
	private String name;

    //事务ID 事务
	private HashMap<Long, Transaction> transactionHash = new HashMap<Long, Transaction>();
	//进行中的事务ID,-1表示没有进行中的事务(每个玩家最多只能有一个进行中的事务)
	private long running = -1;

    public TransactionUser(String name) {
        this.name = name;
    }

    /**
	 * 添加事务<br>
	 * 同时会设置事务的id
	 * @param trans 事务
	 * @return 是否添加成功
	 */
	public void addTransaction(Transaction trans){
		long id = next();
		trans.setId(id);
		transactionHash.put(id, trans);
	}
	
	/**
	 * 删除事务
	 * @param id 事务id
	 * @return 如果存在此id的事务则返回true
	 */
	public boolean delTransaction(long id) {
		Transaction trans = transactionHash.remove(id);
		if (trans == null) return false;
		trans.onCancel();
		return true;
	}

	public String getName() {
		return name;
	}

    /**
     * 只提供查询之用,勿直接添加/删除
     * @return
     */
    public HashMap<Long, Transaction> getTransactionHash() {
        return transactionHash;
    }

	/**
	 * 获取指定id的事务
	 * @param id 事务id
	 * @return 事务
	 */
	public Transaction getTransaction(long id) {
		return transactionHash.get(id);
	}

    /**
     * 删除所有事务
     */
    public void delAllTransaction() {
        running = -1;
        for (Transaction t:transactionHash.values()) t.onCancel();
        transactionHash.clear();
    }

	/**
	 * 事务是否为空
	 * @return
	 */
	public boolean isEmpty() {
		return transactionHash.isEmpty();
	}
	
	/**
	 * 每过一秒时调用
	 */
	public void onTime() {
		Iterator<Long> it = transactionHash.keySet().iterator();
		while (it.hasNext()) {
			long id = it.next();
			Transaction trans = transactionHash.get(id);
			if (trans.isTimeOut()) {//过期检测
				//移除
				it.remove();
				//事务过期操作
				trans.onTimeOut();
			}else if (trans.isTipTime()) {//提示检测
				trans.onTip();
			}
		}
	}


    public long getRunning() {
        return running;
    }

    public void setRunning(long running) {
        this.running = running;
    }

    /**
     * 获取下个有效的事务ID<br>
     * 事务ID从1开始
     * @return 下个有效的事务ID
     */
    private long next(){
        return ++counter;
    }
}
