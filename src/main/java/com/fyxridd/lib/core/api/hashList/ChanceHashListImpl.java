package com.fyxridd.lib.core.api.hashList;

import com.fyxridd.lib.core.api.CoreApi;

import java.util.*;

public class ChanceHashListImpl<T extends Object> extends HashListImpl<T> implements ChanceHashList<T>{
	private static final long serialVersionUID = 1L;
	private int totalChance;

    public ChanceHashListImpl(HashMap<T, Integer> hash, List<T> list, int totalChance) {
        this.hash = hash;
        this.list = list;
        this.totalChance = totalChance;
    }

    @Override
	public boolean remove(T o) {
		if (o == null) throw new NullPointerException();
		if (!hash.containsKey(o)) return false;
		totalChance -= hash.get(o);
		hash.remove(o);
		list.remove(o);
		return true;
	}

	@Override
	public T remove(int index) {
		T o = list.get(index);
		totalChance -= hash.get(o);
		hash.remove(o);
		list.remove(o);
		return o;
	}

	@Override
	public void clear() {
		super.clear();
		totalChance = 0;
	}

	@Override
	public ChanceHashList<T> clone() {
        HashMap<T, Integer> newHash = new HashMap<>();
        for (Map.Entry<T, Integer> entry:this.hash.entrySet()) newHash.put(entry.getKey(), entry.getValue());
        List<T> newList = new ArrayList<>();
        for (T t:this.list) newList.add(t);
        return new ChanceHashListImpl<>(newHash, newList, totalChance);
	}

	@Override
	public boolean add(T o) {
		return addChance(o, 1);
	}

	@Override
	public boolean add(T o, int index) {
		return addChance(o, index, 1);
	}

	@Override
	public boolean addChance(T o, int chance) {
		if (super.add(o)) {
			hash.put(o, chance);
			totalChance += chance;
			return true;
		}else return false;
	}

	@Override
	public boolean addChance(T o, int index, int chance) {
		if (super.add(o,index)) {
			hash.put(o, chance);
			totalChance += chance;
			return true;
		}else return false;
	}

	@Override
	public void setChance(int index, int chance) {
		T o = list.get(index);
		totalChance += (chance-hash.get(o));
		hash.put(o, chance);
	}

	@Override
	public boolean setChance(T o, int chance) {
		if (hash.containsKey(o)) {
			totalChance += (chance-hash.get(o));
			hash.put(o, chance);
			return true;
		}else return false;
	}

	@Override
	public int getChance(int index) {
		return hash.get(list.get(index));
	}

	@Override
	public int getChance(T o) {
		if (hash.containsKey(o)) return hash.get(o);
		else return -1;
	}

	@Override
	public int getTotalChance() {
		return totalChance;
	}

	@Override
	public void updateTotalChance(int totalChance) {
        if (this.totalChance == 0) return;

		double multi = totalChance/this.totalChance;
		this.totalChance = totalChance;
		int count = 0;
		for (T t:hash.keySet()) {
			int chance = (int) (hash.get(t)*multi);
			count += chance;
			hash.put(t, chance);
		}
		int left = totalChance-count;
		if (left > 0) {
			T t = hash.keySet().iterator().next();
			hash.put(t, hash.get(t)+left);
		}
	}

	@Override
	public T getRandom() {
        if (totalChance <= 0) return null;
		int select = CoreApi.Random.nextInt(totalChance);
		for (int i=0;i<list.size();i++) {
			select -= hash.get(list.get(i));
			if (select < 0) return list.get(i);
		}
		return list.get(list.size()-1);
	}

	@Override
	public void convert(HashList<T> hashList, boolean clear) {
		if (clear) clear();
		if (hashList instanceof ChanceHashList) {
			ChanceHashList<T> chanceHashList = (ChanceHashList<T>) hashList;
			for (T t:chanceHashList) {
				addChance(t, chanceHashList.getChance(t));
			}
		}else {
			for (T t:hashList) {
				add(t);
			}
		}
	}
}
