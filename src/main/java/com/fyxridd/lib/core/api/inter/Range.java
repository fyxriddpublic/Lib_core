package com.fyxridd.lib.core.api.inter;

import java.io.Serializable;
import java.util.Random;

/**
 * 3D空间内的范围
 */
public class Range implements Serializable,Cloneable{
	private static final long serialVersionUID = 0;
	private static final Random r = new Random();
	/**
	 * 范围的两个对角
	 */
	private Pos p1,p2;
	
	public Range(Pos p1, Pos p2){
		this.p1 = p1;
		this.p2 = p2;
	}
	
	/**
	 * 检测两点坐标是否合适
	 * @return 如果p1不大于p2,则返回true
	 */
	public boolean isFit() {
		return p1.compare(p2);
	}
	
	/**
	 * 表示调整两点位置,使p1<=p2
	 */
	public void fit() {
		int x1 = p1.getX();
		int y1 = p1.getY();
		int z1 = p1.getZ();
		int x2 = p2.getX();
		int y2 = p2.getY();
		int z2 = p2.getZ();
		if (x1 > x2) {
			p1.setX(x2);
			p2.setX(x1);
		}
		if (y1 > y2) {
			p1.setY(y2);
			p2.setY(y1);
		}
		if (z1 > z2) {
			p1.setZ(z2);
			p2.setZ(z1);
		}
	}

	public Pos getP1() {
		return p1.clone();
	}

	public void setP1(Pos p1) {
		this.p1 = p1;
	}

	public Pos getP2() {
		return p2.clone();
	}

	public void setP2(Pos p2) {
		this.p2 = p2;
	}
	
	/**
	 * 获取x轴的长度
	 */
	public int getXLength() {
		return Math.abs(p2.getX() - p1.getX())+1;
	}

	/**
	 * 获取y轴的长度
	 */
	public int getYLength() {
		return Math.abs(p2.getY() - p1.getY())+1;
	}

	/**
	 * 获取z轴的长度
	 */
	public int getZLength() {
		return Math.abs(p2.getZ() - p1.getZ())+1;
	}
	
	/**
	 * 获取总边长
	 * @param repeatCorner 是否重复计算角落的方块
	 * @return 如果重复计算角落的方块,返回的值会更大
	 */
	public int getTotalLength(boolean repeatCorner) {
		if (repeatCorner) return getXLength()*4+getYLength()*4+getZLength()*4;
		int result = 0;
		//除角落外
		int xLength = getXLength();
		if (xLength > 2) result += 4*(xLength-2);
		int yLength = getYLength();
		if (yLength > 2) result += 4*(yLength-2);
		int zLength = getZLength();
		if (zLength > 2) result += 4*(zLength-2);
		//角落
		result += 8;
		if (xLength == 1) {
			if (yLength == 1 && zLength == 1) result -= 7;
			else if (yLength == 1 || zLength == 1) result -= 6;
			else result -= 4;
		}else {
			if (yLength == 1 && zLength == 1) result -= 6;
			else if (yLength == 1 || zLength == 1) result -= 4;
		}
		return result;
	}
	
	/**
	 * 获取范围x的中点位置
	 * @return x的中点位置
	 */
	public int getXCenter() {
		return (p1.getX()+p2.getX())/2;
	}

	/**
	 * 获取范围x的中点位置
	 * @return x的中点位置
	 */
	public int getYCenter() {
		return (p1.getY()+p2.getY())/2;
	}

	/**
	 * 获取范围x的中点位置
	 * @return x的中点位置
	 */
	public int getZCenter() {
		return (p1.getZ()+p2.getZ())/2;
	}
	
	/**
	 * 检测点是否在范围内
	 * @param pos 点
	 * @return 是否在范围内
	 */
	public boolean checkPos(Pos pos) {
		Range r = clone();
		r.fit();
		return r.getP1().compare(pos) && pos.compare(r.getP2());
	}
	
	/**
	 * 获取范围的大小
	 */
	public long getSize() {
		int xLength = Math.abs(p2.getX() - p1.getX()) + 1;
		int yLength = Math.abs(p2.getY() - p1.getY()) + 1;
		int zLength = Math.abs(p2.getZ() - p1.getZ()) + 1;
		return (long)xLength*yLength*zLength;
	}

	/**
	 * 获取此范围内的中央位置
	 */
	public Pos getCenter() {
		int x1 = p1.getX();
		int y1 = p1.getY();
		int z1 = p1.getZ();
		int x2 = p2.getX();
		int y2 = p2.getY();
		int z2 = p2.getZ();
		Pos result = new Pos(p1.getWorld(), (x1+x2)/2, (y1+y2)/2, (z1+z2)/2);
		return result;
	}

	/**
	 * 获取此范围内的随机位置
	 */
	public Pos getRandomPos() {
		int temp;
		int x1 = p1.getX();
		int y1 = p1.getY();
		int z1 = p1.getZ();
		int x2 = p2.getX();
		int y2 = p2.getY();
		int z2 = p2.getZ();
		if (x1 > x2) {
			temp = x1;
			x1 = x2;
			x2 = temp;
		}
		if (y1 > y2) {
			temp = y1;
			y1 = y2;
			y2 = temp;
		}
		if (z1 > z2) {
			temp = z1;
			z1 = z2;
			z2 = temp;
		}
		Pos result = new Pos(p1.getWorld(), r.nextInt(x2-x1+1)+x1, r.nextInt(y2-y1+1)+y1, r.nextInt(z2-z1+1)+z1);
		return result;
	}
	
	/**
	 * 扩大范围
	 * @param xDir x方向扩展
	 * @param yDir y方向扩展
	 * @param zDir z方向扩展
	 */
	public void expand(int xDir, int yDir, int zDir) {
		if (xDir > 0) {
			if (p1.getX() <= p2.getX()) p2.setX(p2.getX()+xDir);
			else p1.setX(p1.getX()+xDir);
		}else if (xDir < 0) {
			if (p1.getX() <= p2.getX()) p1.setX(p1.getX()+xDir);
			else p2.setX(p2.getX()+xDir);
		}
		if (yDir > 0) {
			if (p1.getY() <= p2.getY()) p2.setY(p2.getY()+yDir);
			else p1.setY(p1.getY()+yDir);
		}else if (yDir < 0) {
			if (p1.getY() <= p2.getY()) p1.setY(p1.getY()+yDir);
			else p2.setY(p2.getY()+yDir);
		}
		if (zDir > 0) {
			if (p1.getZ() <= p2.getZ()) p2.setZ(p2.getZ()+zDir);
			else p1.setZ(p1.getZ()+zDir);
		}else if (zDir < 0) {
			if (p1.getZ() <= p2.getZ()) p1.setZ(p1.getZ()+zDir);
			else p2.setZ(p2.getZ()+zDir);
		}
	}
	
	/**
	 * 缩小范围,最小会缩到长度为1
	 * @param xDir x方向缩小
	 * @param yDir y方向缩小
	 * @param zDir z方向缩小
	 */
	public void contract(int xDir, int yDir, int zDir) {
		if (xDir > 0) {
			if (p1.getX() <= p2.getX()) p1.setX(Math.min(p2.getX(),p1.getX()+xDir));
			else p2.setX(Math.min(p1.getX(),p2.getX()+xDir));
		}else if (xDir < 0) {
			if (p1.getX() <= p2.getX()) p2.setX(Math.max(p1.getX(),p2.getX()+xDir));
			else p1.setX(Math.max(p2.getX(),p1.getX()+xDir));
		}
		if (yDir > 0) {
			if (p1.getY() <= p2.getY()) p1.setY(Math.min(p2.getY(),p1.getY()+yDir));
			else p2.setY(Math.min(p1.getY(),p2.getY()+yDir));
		}else if (yDir < 0) {
			if (p1.getY() <= p2.getY()) p2.setY(Math.max(p1.getY(),p2.getY()+yDir));
			else p1.setY(Math.max(p2.getY(),p1.getY()+yDir));
		}
		if (zDir > 0) {
			if (p1.getZ() <= p2.getZ()) p1.setZ(Math.min(p2.getZ(),p1.getZ()+zDir));
			else p2.setZ(Math.min(p1.getZ(),p2.getZ()+zDir));
		}else if (zDir < 0) {
			if (p1.getZ() <= p2.getZ()) p2.setZ(Math.max(p1.getZ(),p2.getZ()+zDir));
			else p1.setZ(Math.max(p2.getZ(),p1.getZ()+zDir));
		}
	}

	/**
	 * 移动范围
	 * @param xDir x方向移动量
	 * @param yDir y方向移动量
	 * @param zDir z方向移动量
	 */
	public void move(int xDir, int yDir, int zDir) {
		p1.setX(p1.getX()+xDir);
		p2.setX(p2.getX()+xDir);
		p1.setY(p1.getY()+yDir);
		p2.setY(p2.getY()+yDir);
		p1.setZ(p1.getZ()+zDir);
		p2.setZ(p2.getZ()+zDir);
	}

	/**
	 * 检测此范围是否在目标范围内<br>
	 * 相同(边框重合)也算
	 * @param range 目标范围
	 */
	public boolean isIn(Range range) {
		Range r1 = this.clone();
		Range r2 = range.clone();
		r1.fit();
		r2.fit();
		//世界不同
		if (!r1.getP1().getWorld().equals(r2.getP1().getWorld())) return false;
		//坐标检测
		return r2.getP1().compare(r1.getP1()) && r1.getP2().compare(r2.getP2());
	}
	
	@Override
	public Range clone() {
		Range range = new Range(p1.clone(),p2.clone());
		return range;
	}

	@Override
	public int hashCode() {
		return p1.hashCode()+p2.hashCode();
	}

	/**
	 * 此方法较消耗资源,请勿大量调用
	 */
	@Override
	public boolean equals(Object obj) {
		Range r1 = clone();
		Range r2 = ((Range) obj).clone();
		r1.fit();
		r2.fit();
		return r1.p1.equals(p1) && r2.p2.equals(p2);
	}
}
