package com.fyxridd.lib.core.api.model;

import java.io.Serializable;

public class User implements Serializable{
    private String lowerName;
	private String name;
	public User(){}
	public User(String name) {
        this.lowerName = name.toLowerCase();
		this.name = name;
	}

    public String getLowerName() {
        return lowerName;
    }

    public void setLowerName(String lowerName) {
        this.lowerName = lowerName;
    }

    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public int hashCode() {
		return lowerName.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		User user = (User) obj;
		return user.name.equals(name);
	}
}
