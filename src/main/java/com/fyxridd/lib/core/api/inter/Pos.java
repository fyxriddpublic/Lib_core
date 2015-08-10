package com.fyxridd.lib.core.api.inter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.Serializable;

public class Pos implements Serializable,Cloneable{
	private static final long serialVersionUID = 1L;
	private static final int YMAX = 256;
	private static final int YMIN = 0;
	private String world;
	private int x,y,z;

	public Pos(String world,int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * 把Location转换为Pos
	 * @param l
	 * @return
	 */
	public static Pos getPos(Location l) {
		return new Pos(l.getWorld().getName(),l.getBlockX(),l.getBlockY(),l.getBlockZ());
	}
	
	/**
	 * Pos转换为Location,x与z取中间位置
	 * @return 可能为null(如对应的世界未加载时)
	 */
	public static Location toLoc(Pos pos) {
		World w = Bukkit.getWorld(pos.getWorld());
        if (w == null) return null;
		return new Location(w, pos.getX()+0.5, pos.getY(), pos.getZ()+0.5);
	}
	
	/**
	 * 比较两个点
	 * @param p 比较的点,要求世界相同
	 * @return 如果此点每个坐标都<=目标点p的相应坐标,则返回true,否则返回false
	 */
	public boolean compare(Pos p) {
		return x <= p.getX() && y <= p.getY() && z <= p.getZ();
	}
	
	public String getWorld() {
		return world;
	}

	public void setWorld(String world){
		this.world = world;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
		if (y > YMAX) this.y = YMAX;
		if (y < YMIN) this.y = YMIN;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

    /**
     * 从保存数据中读取位置信息
     * @param data 数据,toString()方法生成
     * @return 异常返回null
     */
    public static Pos loadFromString(String data) {
        try {
            String[] args = data.split(",");
            String world = args[0];
            int x = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);
            int z = Integer.parseInt(args[3]);
            return new Pos(world, x, y, z);
        } catch (Exception e) {
            return null;
        }
    }

	@Override
	public int hashCode() {
		return world.hashCode()+x+y+z;
	}

	@Override
	public boolean equals(Object obj) {
		Pos pos = (Pos)obj;
		return pos.world.equals(world) && pos.x == x && pos.y == y && pos.z == z;
	}

    @Override
    public String toString() {
        return world+","+x+","+y+","+z;
    }

    @Override
	public Pos clone() {
		return new Pos(world,x,y,z);
	}
}
