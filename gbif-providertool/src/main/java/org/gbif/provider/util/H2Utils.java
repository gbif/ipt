package org.gbif.provider.util;

public class H2Utils {
	public static int offset(int startPage, int pageSize){
		if (startPage<1){
			throw new IllegalArgumentException("Start page needs to be 1 or larger");
		}
		if (pageSize<0){
			throw new IllegalArgumentException("Page size cannot be negative");
		}
		return (startPage-1)*pageSize;
	}
}
