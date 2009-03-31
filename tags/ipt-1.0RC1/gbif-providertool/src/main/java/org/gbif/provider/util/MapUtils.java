package org.gbif.provider.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapUtils {
	public static void createOrAppendToMappedList(Map<Object, List> map, Object key, Object listItem) {
		if (map.containsKey(key)) {
			map.get(key).add(listItem);
		} else {
			List list = new ArrayList();
			list.add(listItem);
			map.put(key, list);
		}
	}
}
