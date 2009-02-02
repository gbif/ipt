package org.gbif.provider.tapir;

import org.apache.commons.lang.StringUtils;

public class Utils {
	public static Boolean isTrue(String s) {
		if (Boolean.valueOf(s) || StringUtils.trimToEmpty(s).equals("1")){
			return true;
		}
		return false;
	}

}
