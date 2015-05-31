package lib.core;

import lib.core.api.CorePlugin;
import lib.core.api.event.TimeEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class Time implements Listener{
	private static class Check implements Runnable {
		private long pre;
		private int sum;//毫秒
		@Override
		public void run() {
			long now = System.currentTimeMillis();
			if (pre == 0) pre = now;
			int past = (int) (now - pre);
			pre = now;
			sum += past;
			if (sum >= 1000) {
				sum = 0;
				TimeEvent timeEvent = new TimeEvent();
				Bukkit.getPluginManager().callEvent(timeEvent);
			}
		}
	}

	private static Check check = new Check();
	
	public Time() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(CorePlugin.instance, check, 1, 1);
	}
}
