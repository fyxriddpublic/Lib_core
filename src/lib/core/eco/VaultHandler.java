package lib.core.eco;

import lib.core.api.CoreApi;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHandler implements EcoHandler {
    private static Economy economy;

    public VaultHandler() {
        //初始化经济
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        try {
            economy = rsp.getProvider();
        } catch (Exception e) {
            economy = null;
        }
    }

    @Override
    public double get(Player p) {
        return get(p.getName());
    }

    @Override
    public double get(String name) {
        return economy.getBalance(name);
    }

    @Override
    public boolean set(Player p, int amount) {
        return set(p.getName(), (double)amount);
    }

    @Override
    public boolean set(String name, int amount) {
        return set(name, (double)amount);
    }

    @Override
    public boolean set(Player p, double amount) {
        return set(p.getName(), amount);
    }

    @Override
    public boolean set(String name, double amount) {
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //数量
        if (amount < 0) amount = 0;
        else if (amount > EcoManager.max) amount = EcoManager.max;
        //
        double current = get(name);
        if (amount == current) return true;
        if (amount > current) return economy.depositPlayer(name, amount-current).transactionSuccess();
        else return economy.withdrawPlayer(name, current-amount).transactionSuccess();
    }

    @Override
    public boolean add(Player p, int amount) {
        return add(p.getName(), (double)amount);
    }

    @Override
    public boolean add(String name, int amount) {
        return add(name, (double)amount);
    }

    @Override
    public boolean add(Player p, double amount) {
        return add(p.getName(), amount);
    }

    @Override
    public boolean add(String name, double amount) {
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //
        if (amount <= 0) return true;
        double result = get(name)+amount;
        if (result < 0 || result > EcoManager.max) result = EcoManager.max;
        //
        return set(name, result);
    }

    @Override
    public boolean del(Player p, int amount) {
        return del(p.getName(), (double)amount);
    }

    @Override
    public boolean del(String name, int amount) {
        return del(name, (double)amount);
    }

    @Override
    public boolean del(Player p, double amount) {
        return del(p.getName(), amount);
    }

    @Override
    public boolean del(String name, double amount) {
        //玩家存在性检测
        name = CoreApi.getRealName(null, name);
        if (name == null) return false;
        //
        if (amount <= 0) return true;
        double current = get(name);
        double result = current - amount;
        if (result < 0 || result > EcoManager.max) return economy.withdrawPlayer(name, current).transactionSuccess();
        return economy.withdrawPlayer(name, amount).transactionSuccess();
    }
}
