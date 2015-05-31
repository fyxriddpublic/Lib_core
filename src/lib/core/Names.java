package lib.core;

import lib.core.api.ConfigApi;
import lib.core.api.CoreApi;
import lib.core.api.CorePlugin;
import lib.core.api.FormatApi;
import lib.core.api.event.ReloadConfigEvent;
import lib.core.api.inter.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.HashMap;

public class Names implements Listener{
	private static HashMap<String, String> worldHash;
	private static HashMap<Integer, String> enchantHash;
	private static HashMap<Integer, String> potionHash;
	private static HashMap<String, String> itemHash;
	private static HashMap<String, String> entityHash;
	
	public Names() {
		//读取配置文件
		loadConfig();
		//注册事件
		Bukkit.getPluginManager().registerEvents(this, CorePlugin.instance);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onReloadConfig(ReloadConfigEvent e) {
		if (e.getPlugin().equals(CorePlugin.pn)) loadConfig();
	}

    /**
     * @see lib.core.api.NamesApi#getWorldName(String)
     */
	public static String getWorldName(String world) {
        if (world == null) return "";

		if (worldHash.containsKey(world)) return worldHash.get(world);
		else return world;
	}

    /**
     * @see lib.core.api.NamesApi#getEnchantName(int)
     */
	public static String getEnchantName(int id) {
		if (enchantHash.containsKey(id)) return enchantHash.get(id);
		else {
			@SuppressWarnings("deprecation")
			Enchantment enchantment = Enchantment.getById(id);
			if (enchantment == null) return "";
			else return enchantment.getName();
		}
	}

    /**
     * @see lib.core.api.NamesApi#getItemName(org.bukkit.inventory.ItemStack)
     */
	public static String getItemName(ItemStack is) {
        if (is == null) return "";
		if (is.hasItemMeta()) {
			ItemMeta im = is.getItemMeta();
			if (im.getDisplayName() != null && !im.getDisplayName().isEmpty()) return im.getDisplayName();
		}
		return getItemName(is.getTypeId(), is.getDurability());
	}

    /**
     * @see lib.core.api.NamesApi#getItemName(int, int)
     */
	public static String getItemName(int id, int smallId) {
		String result = itemHash.get(id+":"+smallId);
		if (result == null) {
            result = itemHash.get(id + ":" + 0);
            if (result == null) result = new ItemStack(id, 1, (short) smallId).getType().name();
            if (result == null) result = "";
        }
		return result;
	}

    /**
     * @see lib.core.api.NamesApi#getEntityName(org.bukkit.entity.Entity, boolean, boolean)
     */
	public static String getEntityName(Entity entity, boolean customName, boolean playerName) {
		if (entity == null) return "";
		try {
			if (playerName && entity instanceof Player) {
				return get(300, entity.getName()).getText();
			}else if (customName && entity instanceof LivingEntity) {
				String name = entity.getCustomName();
				if (name != null && !name.trim().isEmpty()) return name;
			}
		} catch (Exception e) {
            //do nothing
		}
		return getEntityName(entity.getType().name());
	}

    /**
     * @see lib.core.api.NamesApi#getEntityName(int)
     */
	public static String getEntityName(int id) {
		try {
            return getEntityName(EntityType.fromId(id).name());
		} catch (Exception e) {
			return "";
		}
	}

    /**
     * @see lib.core.api.NamesApi#getEntityName(String)
     */
    public static String getEntityName(String name) {
        if (name == null) return "";
        String result = entityHash.get(name);
        if (result == null) result = name;
        return result;
    }

    /**
     * @see lib.core.api.NamesApi#getPotionName(int)
     */
	public static String getPotionName(int id) {
		try {
			String result = potionHash.get(id);
			if (result == null) {
                result = PotionEffectType.getById(id).getName();
                if (result == null) result = "";
            }
			return result;
		} catch (Exception e) {
			return "";
		}
	}

	public static HashMap<String, String> getItemHash() {
		return itemHash;
	}

	private static void loadConfig() {
		YamlConfiguration config = ConfigApi.getConfig(CorePlugin.pn);
		
		String namesPath = config.getString("names.path");
        if (namesPath == null) {
			ConfigApi.log(CorePlugin.pn, "names.path is null");
            return;
        }
		YamlConfiguration namesConfig = CoreApi.loadConfigByUTF8(new File(namesPath));
        if (namesConfig == null) {
			ConfigApi.log(CorePlugin.pn, "namesConfig load error");
            return;
        }

		worldHash = new HashMap<String, String>();
		enchantHash = new HashMap<Integer, String>();
		potionHash = new HashMap<Integer, String>();
		itemHash = new HashMap<String, String>();
		entityHash = new HashMap<String, String>();

        try {
            for (String s:namesConfig.getStringList("names.world")) {
                String world = s.split(" ")[0];
                String display = s.split(" ")[1];
                worldHash.put(world, display);
            }
        } catch (Exception e) {
			ConfigApi.log(CorePlugin.pn, "names.world error");
        }

        try {
            for (String s:namesConfig.getStringList("names.enchant")) {
                int id = Integer.parseInt(s.split(" ")[0]);
                String display = s.split(" ")[1];
                enchantHash.put(id, display);
            }
        } catch (Exception e) {
			ConfigApi.log(CorePlugin.pn, "names.enchant error");
        }

        try {
            for (String s:namesConfig.getStringList("names.potion")) {
                int id = Integer.parseInt(s.split(" ")[0]);
                String display = s.split(" ")[1];
                potionHash.put(id, display);
            }
        } catch (Exception e) {
			ConfigApi.log(CorePlugin.pn, "names.potion error");
        }

        try {
            for (String s:namesConfig.getStringList("names.item")) {
                String id = s.split(" ")[0];
                String name = s.split(" ")[1];
                if (!id.contains(":")) id += ":0";
                itemHash.put(id, name);
            }
        } catch (Exception e) {
			ConfigApi.log(CorePlugin.pn, "names.item error");
        }

        try {
            for (String s:namesConfig.getStringList("names.entity")) {
                String o = s.split(" ")[0];
                String name = s.split(" ")[1];
                entityHash.put(o, name);
            }
        } catch (Exception e) {
			ConfigApi.log(CorePlugin.pn, "names.entity error");
        }
    }

	private static FancyMessage get(int id, Object... args) {
		return FormatApi.get(CorePlugin.pn, id, args);
	}
}
