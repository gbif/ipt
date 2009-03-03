package org.gbif.provider.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;

public class CacheMap<K,V> extends java.util.LinkedHashMap<K,V> {
	private int max;

	public CacheMap(int max){
		this.max=max;
	}
	
	@Override
	protected boolean removeEldestEntry(Entry<K, V> eldest) {
		return size() > max;
	}
	
}
