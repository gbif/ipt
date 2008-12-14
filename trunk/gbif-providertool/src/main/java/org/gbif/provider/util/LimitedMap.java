package org.gbif.provider.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class LimitedMap<K,V> extends HashMap<K,V> {
	private int max;
	private LinkedList<K> queue = new LinkedList<K>();

	public LimitedMap(int max){
		this.max=max;
	}
	
	@Override
	public void clear() {
		queue.clear();
		super.clear();
	}

	@Override
	public V put(K key, V value) {
		if (!super.containsKey(key)){
			if (super.size()+1 > max){
				K byebye = queue.poll();
				super.remove(byebye);
			}
			queue.add(key);
		}
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (K key : m.keySet()){
			this.put(key, m.get(key));
		}
	}

	@Override
	public V remove(Object key) {
		queue.remove(key);
		return super.remove(key);
	}
	
	
}
