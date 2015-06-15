package com.fyxridd.lib.core;

import com.fyxridd.lib.core.api.event.RealDamageEvent;
import com.fyxridd.lib.core.api.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class RealDamage implements Listener {
	public RealDamage() {
		//注册事件
		Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
	}
	
	@EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
	public void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
		//无伤害
		if (e.getEntity() instanceof LivingEntity) {
            LivingEntity le = (LivingEntity) e.getEntity();
            if (le.getNoDamageTicks() > le.getMaximumNoDamageTicks()/2 &&
                    e.getDamage() <= le.getLastDamage()) return;
        }
		//
		RealDamageEvent realDamageEvent = new RealDamageEvent(e);
		Bukkit.getPluginManager().callEvent(realDamageEvent);
	}
}
