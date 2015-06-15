package com.fyxridd.lib.core.api;

import com.fyxridd.lib.core.eco.EcoManager;
import org.bukkit.entity.Player;

public class EcoApi {
    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#get(org.bukkit.entity.Player)
     */
    public static double get(Player p) {
        return EcoManager.ecoHandler.get(p);
    }

    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#get(String)
     */
    public static double get(String name) {
        return EcoManager.ecoHandler.get(name);
    }

    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#set(org.bukkit.entity.Player, int)
     */
    public static boolean set(Player p, int amount) {
        return EcoManager.ecoHandler.set(p, amount);
    }

    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#set(String, int)
     */
    public static boolean set(String name, int amount) {
        return EcoManager.ecoHandler.set(name, amount);
    }

    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#set(org.bukkit.entity.Player, double)
     */
    public static boolean set(Player p, double amount) {
        return EcoManager.ecoHandler.set(p, amount);
    }

    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#set(String, double)
     */
    public static boolean set(String name, double amount) {
        return EcoManager.ecoHandler.set(name, amount);
    }

    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#add(org.bukkit.entity.Player, int)
     */
    public static boolean add(Player p, int amount) {
        return EcoManager.ecoHandler.add(p, amount);
    }

    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#add(String, int)
     */
    public static boolean add(String name, int amount) {
        return EcoManager.ecoHandler.add(name, amount);
    }

    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#add(org.bukkit.entity.Player, double)
     */
    public static boolean add(Player p, double amount) {
        return EcoManager.ecoHandler.add(p, amount);
    }

    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#add(String, double)
     */
    public static boolean add(String name, double amount) {
        return EcoManager.ecoHandler.add(name, amount);
    }

    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#del(org.bukkit.entity.Player, int)
     */
    public static boolean del(Player p, int amount) {
        return EcoManager.ecoHandler.del(p, amount);
    }

    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#del(String, int)
     */
    public static boolean del(String name, int amount) {
        return EcoManager.ecoHandler.del(name, amount);
    }

    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#del(org.bukkit.entity.Player, double)
     */
    public static boolean del(Player p, double amount) {
        return EcoManager.ecoHandler.del(p, amount);
    }

    /**
     * @see com.fyxridd.lib.core.eco.EcoHandler#del(String, double)
     */
    public static boolean del(String name, double amount) {
        return EcoManager.ecoHandler.del(name, amount);
    }
}
