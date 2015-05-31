package lib.core.api;

import lib.core.FormatManager;
import lib.core.api.inter.FancyMessage;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

public class FormatApi {
    /**
     * @see FormatManager#get(String, int)
     */
    public static FancyMessage get(String pluginName, int id) {
        return FormatManager.get(pluginName, id);
    }

    /**
     * @see FormatManager#get(String, int, Object...)
     */
    public static FancyMessage get(String pluginName, int id, Object... args) {
        return FormatManager.get(pluginName, id, args);
    }

    /**
     * @see FormatManager#load(String, org.bukkit.configuration.MemorySection)
     */
    public static FancyMessage load(String msg, MemorySection ms) {
        return FormatManager.load(msg, ms);
    }

    /**
     * @see FormatManager#save(int, org.bukkit.configuration.file.YamlConfiguration, FancyMessage)
     */
    public static void save(int num, YamlConfiguration config, FancyMessage fm) {
        FormatManager.save(num, config, fm);
    }
}
