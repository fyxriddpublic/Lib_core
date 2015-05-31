package lib.core.per;

import lib.core.api.hashList.HashList;
import lib.core.api.hashList.HashListImpl;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class Group {
    //继承列表,可为空不为null
    private HashList<String> inherits;

    //权限列表,可为空不为null
    private HashList<String> pers;

    public Group(HashList<String> inherits, HashList<String> pers) {
        this.inherits = inherits;
        this.pers = pers;
    }

    public HashList<String> getInherits() {
        return inherits;
    }

    public void setInherits(HashList<String> inherits) {
        this.inherits = inherits;
    }

    public HashList<String> getPers() {
        return pers;
    }

    public void setPers(HashList<String> pers) {
        this.pers = pers;
    }

    /**
     * 从配置文件中读取权限组信息
     * @param config 配置文件,不为null
     * @return 异常返回null
     */
    public static Group load(YamlConfiguration config) {
        if (config == null) return null;
        try {
            HashList<String> inherits = new HashListImpl<String>();
            HashList<String> pers = new HashListImpl<String>();
            Group group = new Group(inherits, pers);
            //inherits
            List<String> list = config.getStringList("inherits");
            if (list != null) {
                for (String s:list) inherits.add(s);
            }
            //pers
            list = config.getStringList("pers");
            if (list != null) {
                for (String s:list) pers.add(s);
            }
            //返回
            return group;
        } catch (Exception e) {
            return null;
        }
    }
}
