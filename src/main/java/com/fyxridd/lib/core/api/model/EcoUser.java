package com.fyxridd.lib.core.api.model;

import java.io.Serializable;

public class EcoUser implements Serializable{
    private String name;
    private double money;

    public EcoUser() {
    }

    public EcoUser(String name, double money) {
        this.name = name;
        this.money = money;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        EcoUser ecoUser = (EcoUser) obj;
        return ecoUser.name.equals(name);
    }
}
