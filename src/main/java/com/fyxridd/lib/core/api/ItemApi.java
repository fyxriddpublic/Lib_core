package com.fyxridd.lib.core.api;

import com.fyxridd.lib.core.api.nbt.Attributes;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Iterator;

public class ItemApi {
    private static ItemMeta EmptyItemMeta = new ItemStack(Material.STONE).getItemMeta();

    /**
     * 检测指定的物品是否有耐久度
     * @param is 检测的物品,不为null
     * @return 是否有耐久度
     */
    public static boolean hasDurability(ItemStack is) {
        return is.getMaxStackSize() == 1 && is.getType().getMaxDurability() > 1;
    }

    /**
     * 获取容器中空格子的数量
     * @param inv 容器,不为null
     * @return 没有返回0
     */
    public static int getEmptySlots(Inventory inv) {
        int sum = 0;
        for (int i=0;i<inv.getSize();i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType().equals(Material.AIR)) sum++;
        }
        return sum;
    }

    /**
     * 检测容器中是否有空格子
     * @param inv 容器,不为null
     */
    public static boolean hasEmptySlot(Inventory inv) {
        for (int i=0;i<inv.getSize();i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) return true;
        }
        return false;
    }

    /**
     * 检测容器中是否有指定数量的空格子
     * @param inv 容器,不为null
     * @param amount 数量
     */
    public static boolean hasEmptySlots(Inventory inv, int amount) {
        int sum = 0;
        for (int i=0;i<inv.getSize();i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType().equals(Material.AIR)) {
                sum++;
                if (sum >= amount) return true;
            }
        }
        return false;
    }

    /**
     * 检测玩家背包是否为空
     * @param p 检测的玩家
     * @return 背包为空返回true,否则返回false
     */
    public static boolean checkEmpty(Player p) {
        PlayerInventory pi = p.getInventory();
        for (int i=0;i<40;i++) {
            if (pi.getItem(i) != null && !pi.getItem(i).getType().equals(Material.AIR)) return false;
        }
        return true;
    }

    /**
     * 检测容器中是否有指定数量的'普通'物品(ItemMeta为空)
     * @param inv 容器,不为null
     * @param id 物品id
     * @param amount 物品数量
     * @return 是否含有指定数量的物品
     */
    public static boolean hasNormalItem(Inventory inv,int id,int amount) {
        //之所以不调用getNormalAmount是为了提高效率!
        int sum = 0;
        for (ItemStack is:inv.getContents()) {
            if (is != null && is.getTypeId() == id && isItemMetaEmpty(is.getItemMeta())) {
                sum += is.getAmount();
                if (sum >= amount) return true;
            }
        }
        return false;
    }

    /**
     * 不忽略物品名
     * @see #hasExactItem(org.bukkit.inventory.Inventory, org.bukkit.inventory.ItemStack, int, boolean)
     */
    public static boolean hasExactItem(Inventory inv, ItemStack is, int amount) {
        return hasExactItem(inv, is, amount, false);
    }

    /**
     * 检测容器中是否有指定数量的'精确'物品
     * @param inv 容器,不为null
     * @param is 物品
     * @param amount 物品数量
     * @param ignoreName 是否忽略物品名
     * @return 是否含有指定数量的精确物品
     */
    public static boolean hasExactItem(Inventory inv, ItemStack is, int amount, boolean ignoreName) {
        int sum = 0;
        for (int i=0;i<inv.getSize();i++) {
            ItemStack check = inv.getItem(i);
            if (check != null && isSameItem(is, check, ignoreName)) {
                sum += inv.getItem(i).getAmount();
                if (sum >= amount) return true;
            }
        }
        return false;
    }

    /**
     * 从指定容器中移除指定数量的'普通'物品(ItemMeta为空)
     * @param inv 容器,不为null
     * @param id 物品id
     * @param amount 要移除的数量
     * @param force 如果容器中物品数量不足,是否移除已经拥有的
     * @return 如果容器中没有指定数量的指定物品,返回false
     */
    @SuppressWarnings("deprecation")
    public static boolean removeNormalItem(Inventory inv, int id, int amount, boolean force) {
        if (amount <= 0) return true;
        if (hasNormalItem(inv, id, amount)) {
            for (int i=0;i<inv.getSize();i++) {
                if (inv.getItem(i) != null){
                    ItemStack is = inv.getItem(i);
                    if (is.getTypeId() == id && isItemMetaEmpty(is.getItemMeta())) {
                        if (amount >= is.getAmount()) {
                            amount -= is.getAmount();
                            inv.setItem(i, null);
                        }else {
                            is.setAmount(is.getAmount()-amount);
                            amount = 0;
                        }
                        if (amount <= 0) break;
                    }
                }
            }
            return true;
        }else if (force) {
            for (int i=0;i<inv.getSize();i++) {
                if (inv.getItem(i) != null){
                    ItemStack is = inv.getItem(i);
                    if (is.getTypeId() == id && isItemMetaEmpty(is.getItemMeta())) {
                        if (amount >= is.getAmount()) {
                            amount -= is.getAmount();
                            inv.setItem(i, null);
                        }else {
                            is.setAmount(is.getAmount()-amount);
                            amount = 0;
                        }
                        if (amount <= 0) break;
                    }
                }
            }
            return false;
        }else return false;
    }

    /**
     * 从指定容器中移除指定数量的指定物品(精确的)
     * @param inv 容器,不为null
     * @param is 物品,不为null
     * @param amount 要移除的数量
     * @param force 如果容器中物品数量不足,是否移除已经拥有的
     * @return 如果容器中没有指定数量的指定物品,返回false
     */
    public static boolean removeExactItem(Inventory inv, ItemStack is, int amount, boolean force) {
        return removeExactItem(inv, is, amount, force, false);
    }

    /**
     * 从指定容器中移除指定数量的指定物品(精确的)
     * @param inv 容器,不为null
     * @param is 物品,不为null
     * @param amount 要移除的数量
     * @param force 如果容器中物品数量不足,是否移除已经拥有的
     * @param ignoreName 是否忽略物品名
     * @return 如果容器中没有指定数量的指定物品,返回false
     */
    public static boolean removeExactItem(Inventory inv, ItemStack is, int amount, boolean force, boolean ignoreName) {
        if (amount <= 0) return true;
        if (force || hasExactItem(inv, is, amount, ignoreName)) {
            //需要减少的数量
            int need = amount;
            for (int i=0;i<inv.getSize();i++) {
                ItemStack is2 = inv.getItem(i);
                if (is2 != null && isSameItem(is, is2, ignoreName)) {//检测相同成功,减少物品
                    int has = is2.getAmount();
                    if (need <= has) {//结束
                        if (has == need) inv.setItem(i, null);
                        else is2.setAmount(has-need);
                        need = 0;
                        break;
                    }else {
                        need -= has;
                        inv.setItem(i, null);
                        continue;
                    }
                }
            }
            return need <= 0;
        }else return false;
    }

    /**
     * 获取指定容器中指定id的'普通'物品的数量(ItemMeta为空)
     * @param inv 容器,不为null
     * @param id 物品id
     * @return 数量,>=0
     */
    @SuppressWarnings("deprecation")
    public static int getNormalItemAmount(Inventory inv,int id) {
        int sum = 0;
        for (int i=0;i<inv.getSize();i++) {
            if (inv.getItem(i) != null &&
                    inv.getItem(i).getTypeId() == id &&
                    isItemMetaEmpty(inv.getItem(i).getItemMeta())) {
                sum += inv.getItem(i).getAmount();
            }
        }
        return sum;
    }

    /**
     * 不忽略物品名
     * @see #getExactItemAmount(org.bukkit.inventory.Inventory, org.bukkit.inventory.ItemStack, boolean)
     */
    public static int getExactItemAmount(Inventory inv, ItemStack is) {
        return getExactItemAmount(inv, is, false);
    }

    /**
     * 获取指定容器中指定物品的数量<br>
     * 会检测id,durability,ItemMeta,attributes
     * @param inv 容器,不为null
     * @param is 物品,不为null
     * @param ignoreName 是否忽略物品名
     * @return 数量,>=0
     */
    public static int getExactItemAmount(Inventory inv, ItemStack is, boolean ignoreName) {
        int sum = 0;
        for (int i=0;i<inv.getSize();i++) {
            ItemStack check = inv.getItem(i);
            if (check != null && isSameItem(is, check, ignoreName)) sum += inv.getItem(i).getAmount();
        }
        return sum;
    }

    /**
     * 不忽略物品名
     * @see #isSameItem(org.bukkit.inventory.ItemStack, org.bukkit.inventory.ItemStack, boolean)
     */
    public static boolean isSameItem(ItemStack is1, ItemStack is2) {
        return isSameItem(is1, is2, false);
    }

    /**
     * 检测是否是相同的物品<br>
     * 会检测id,durability,ItemMeta,attributes<br>
     * 检测不包括数量
     * @param is1 物品1,不为null
     * @param is2 物品2,不为null
     * @param ignoreName 是否忽略物品名
     * @return 是否相同
     */
    public static boolean isSameItem(ItemStack is1, ItemStack is2, boolean ignoreName) {
        //id,durability
        if (is1.getType().equals(is2.getType()) && is1.getDurability() == is2.getDurability()) {
            if (is1.getType().equals(Material.AIR)) return true;//空气,特殊情况
            //itemMeta
            ItemMeta im1 = is1.getItemMeta();
            ItemMeta im2 = is2.getItemMeta();
            if (im1 == null) {
                if (im2 != null) return false;
            }else {
                if (im2 == null) return false;
                else {
                    if (ignoreName) im2.setDisplayName(im1.getDisplayName());//把两个名字强制改成一样的
                    if (!im1.equals(im2)) return false;
                }
            }
            //attributes
            if (!hasSameAttributes(is1, is2)) return false;
            //相同
            return true;
        }
        return false;
    }

    /**
     * 检测两个物品的Attributes是否相同
     * @param is1 物品1,不为null
     * @param is2 物品2,不为null
     * @return 是否相同
     */
    public static boolean hasSameAttributes(ItemStack is1, ItemStack is2) {
        //空气,特殊情况
        if (is1.getType().equals(Material.AIR)) {
            return is2.getType().equals(Material.AIR);
        }else {
            if (is2.getType().equals(Material.AIR)) return false;
        }
        //其它物品
        Attributes a1 = new Attributes(is1);
        Attributes a2 = new Attributes(is2);
        if (a1.size() == a2.size()) {
            if (a1.size() == 0) return true;//两个Attri都为空
            //检测
            Iterator<Attributes.Attribute> it1 = a1.values().iterator();
            Iterator<Attributes.Attribute> it2 = a2.values().iterator();
            while (it1.hasNext()) {
                if (!it1.next().equals(it2.next())) return false;
            }
            return true;
        }
        //Attri数量不一样
        return false;
    }

    /**
     * 检测ItemMeta是否为空
     * @param itemMeta 可为null,null时返回true
     * @return
     */
    public static boolean isItemMetaEmpty(ItemMeta itemMeta) {
        if (itemMeta == null) return true;
        return itemMeta.equals(EmptyItemMeta);
    }
}
