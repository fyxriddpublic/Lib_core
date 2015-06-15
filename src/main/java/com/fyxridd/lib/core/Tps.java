package com.fyxridd.lib.core;

import com.fyxridd.lib.core.api.CoreApi;
import com.fyxridd.lib.core.api.CorePlugin;
import org.bukkit.Bukkit;

public class Tps{
	//检测间隔,单位是tick,>=1
	private static final int CHECK_INTERVAL = 1;
	//更新间隔,单位是秒,>=1
    private static final int UPDATE_INTERVAL = 10;
    private static long start;
    private static int ticks;
    private static double tps = -1;
	
	/**
	 * 构建并开始运行同步检测tps线程
	 */
	public Tps() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(CorePlugin.instance, new Runnable() {
            public void run() {
                check();
            }
        }, CHECK_INTERVAL, CHECK_INTERVAL);
	}

    /**
     * 获取当前tps
     * @return 0-20,-1表示暂无
     */
    public static double getTps() {
        return tps;
    }

	private static void check() {
		if (start == 0)	start = System.currentTimeMillis();
		else ticks += CHECK_INTERVAL;
		if (System.currentTimeMillis() - start >= UPDATE_INTERVAL*1000) {
			start = System.currentTimeMillis();
			tps = CoreApi.getDouble((double) ticks / UPDATE_INTERVAL, 2);
			if (tps > 20) tps = 20;
			ticks = 0;
		}
	}
}
