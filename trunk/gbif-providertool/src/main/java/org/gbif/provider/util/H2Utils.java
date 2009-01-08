package org.gbif.provider.util;

public class H2Utils {
	public static int offset(int startPage, int pageSize){
		return startPage*pageSize;
	}
}
