package com.fyxridd.lib.core.api.model;

import java.util.HashSet;

/**
 * 权限用户
 */
public class PerUser {
    //用户名
    private String name;
    //可为空不为null
    private HashSet<String> groups;
    //可为空不为null
    private HashSet<String> pers;

    public PerUser() {}

    public PerUser(String name, HashSet<String> groups, HashSet<String> pers) {
        this.name = name;
        this.groups = groups;
        this.pers = pers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSet<String> getGroups() {
        return groups;
    }

    public void setGroups(HashSet<String> groups) {
        this.groups = groups;
    }

    public HashSet<String> getPers() {
        return pers;
    }

    public void setPers(HashSet<String> pers) {
        this.pers = pers;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return ((PerUser)obj).name.equals(name);
    }
}
