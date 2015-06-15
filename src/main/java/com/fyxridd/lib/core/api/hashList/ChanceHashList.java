package com.fyxridd.lib.core.api.hashList;

import java.io.Serializable;

/**
 * 继承自HashList,增加了根据机率随机选取元素的功能
 */
public interface ChanceHashList<T extends Object> extends HashList<T>,Iterable<T>,Cloneable,Serializable{
	/**
	 * 在最后面增加元素,并赋以默认机率1
	 * @param o 元素
	 * @return 成功返回true,如果已经有此元素则返回false
	 */
	public boolean add(T o);
	
	/**
	 * 在指定位置增加元素,并赋以默认机率1
	 * @param o 元素
	 * @param index 位置,[0,size()]
	 * @return 成功返回true,如果已经有此元素返回false
	 */
	public boolean add(T o, int index);
	
	/**
	 * 将HashList的内容导入进来,机率会全部默认为1<br>
	 * 但如果导入的是ChanceHashList实例,则机率会是实际情况
	 * @param hashList 要导入的HashList
	 * @param clear true表示导入前将原来的内容清空
	 */
	public void convert(HashList<T> hashList, boolean clear);

	/**
	 * 复制
	 * @return 相同内容的对象
	 */
	public ChanceHashList<T> clone();
	
	/**
	 * 在最后面增加元素,并赋以机率
	 * @param o 元素
	 * @param chance 机率,>=0
	 * @return 成功返回true,如果已经有此元素则返回false
	 */
	public boolean addChance(T o, int chance);
	
	/**
	 * 在指定位置增加元素,并赋以机率
	 * @param o 元素
	 * @param index 位置,[0,size()]
	 * @param chance 机率,>=0
	 * @return 成功返回true,如果已经有此元素返回false
	 */
	public boolean addChance(T o, int index, int chance);
	
	/**
	 * 设置指定位置元素的机率
	 * @param index 指定的位置
	 * @param chance 新的机率
	 */
	public void setChance(int index, int chance);

	/**
	 * 设置指定元素的机率
	 * @param o 指定的元素
	 * @param chance 新的机率
	 * @return 成功返回true,失败返回false
	 */
	public boolean setChance(T o, int chance);

	/**
	 * 获取指定位置元素的机率
	 * @param index 位置,从0开始
	 * @return 指定位置元素的机率
	 */
	public int getChance(int index);
	
	/**
	 * 获取指定元素的机率
	 * @param o 指定的元素
	 * @return 指定元素的机率,没有返回-1
	 */
	public int getChance(T o);
	
	/**
	 * 获取总几率
	 * @return
	 */
	public int getTotalChance();
	
	/**
	 * 按比例将总几率更新到目标值<br>
	 * 各元素间的比例可能有些许的偏差
	 * @param totalChance 更新后的总几率
	 */
	public void updateTotalChance(int totalChance);
	
	/**
	 * 根据机率随机选择一个元素
	 * @return 随机选择的元素,可能为null
	 */
	public T getRandom();
}
