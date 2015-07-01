package com.fyxridd.lib.core.api.inter;

import org.bukkit.World;

/**
 * 方块位置
 */
public class BlockLocation {
    private World world;
    private int x,y,z;

    public BlockLocation(World world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
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
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        BlockLocation bl = (BlockLocation) obj;
        return world.equals(bl.world) && x == bl.x && y == bl.y && z == bl.z;
    }

    @Override
    public int hashCode() {
        return world.hashCode()+x+y+z;
    }
}
