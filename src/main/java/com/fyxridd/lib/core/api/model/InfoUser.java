package com.fyxridd.lib.core.api.model;

import java.io.Serializable;

/**
 * 玩家附加属性
 */
public class InfoUser implements Serializable{
    //玩家名
	private String name;
    //属性名
    private String flag;
    //属性值
    private String data;
	public InfoUser(){}

    public InfoUser(String name, String flag, String data) {
        this.name = name;
        this.flag = flag;
        if (data != null) this.data = data.substring(0, Math.min(1024, data.length()));
    }

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getData() {
        return data;
    }

    /**
     * @return 是否设置成功,如果data长度超过1024则设置失败
     */
    public boolean setData(String data) {
        if (data != null && data.length() > 1024) return false;
        this.data = data;
        return true;
    }

    @Override
	public int hashCode() {
		return (name+flag).hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		InfoUser infoUser = (InfoUser) obj;
		return infoUser.name.equals(name)  && infoUser.flag.equals(flag);
	}
}
