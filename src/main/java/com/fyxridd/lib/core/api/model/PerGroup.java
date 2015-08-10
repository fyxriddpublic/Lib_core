package com.fyxridd.lib.core.api.model;

import java.util.HashSet;

/**
 * 权限组
 */
public class PerGroup {
    //组名
    private String name;
    //可为空不为null
    private HashSet<String> inherits;
    //可为空不为null
    private HashSet<String> pers;

    public PerGroup() {
    }

    public PerGroup(String name, HashSet<String> inherits, HashSet<String> pers) {
        this.name = name;
        this.inherits = inherits;
        this.pers = pers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSet<String> getInherits() {
        return inherits;
    }

    public void setInherits(HashSet<String> inherits) {
        this.inherits = inherits;
    }

    public HashSet<String> getPers() {
        return pers;
    }

    public void setPers(HashSet<String> pers) {
        this.pers = pers;
    }
}
