package com.fyxridd.lib.core.api;

import com.fyxridd.lib.core.Speed;
import org.bukkit.entity.Player;

public class SpeedApi {
    /**
     * @see com.fyxridd.lib.core.Speed#register(String, String)
     */
    public static void register(String plugin, String type) {
        Speed.register(plugin, type);
    }

    /**
     * @see Speed#check(Player, String, String, int)
     */
    public static boolean check(Player p, String plugin, String type, int limit) {
        return Speed.check(p, plugin, type, limit);
    }

    /**
     * @see Speed#check(org.bukkit.entity.Player, String, String, int, boolean)
     */
    public static boolean check(Player p, String plugin, String type, int limit, boolean tip) {
        return Speed.check(p, plugin, type, limit, tip);
    }

    /**
     * @see Speed#checkShort(org.bukkit.entity.Player, String, String, int)
     */
    public static boolean checkShort(Player p, String plugin, String type, int level) {
        return Speed.checkShort(p, plugin, type, level);
    }
}
