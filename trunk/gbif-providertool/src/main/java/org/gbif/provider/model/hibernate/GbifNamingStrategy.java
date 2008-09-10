package org.gbif.provider.model.hibernate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.util.StringHelper;
import org.apache.commons.lang.StringUtils;

public class GbifNamingStrategy extends ImprovedNamingStrategy {
	static final Pattern MULTI_UPPERCASE = Pattern.compile("([A-Z])([A-Z]+)");

	protected final Log log = LogFactory.getLog(getClass());

	@Override
	public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
//		log.debug(StringUtils.join(new String[] {propertyName, propertyEntityName, propertyTableName, referencedColumnName}, " <|> "));

		String colFk;
		String suffix = "_"+referencedColumnName;
		if (referencedColumnName.equalsIgnoreCase("id")){
			suffix = "_fk";
		}
		if (propertyName != null && propertyName.length()>1){
			colFk = propertyToColumnName(propertyName)+suffix;			
		}else{
			colFk = propertyToColumnName(propertyTableName)+suffix;			
		}
//		log.debug("foreignKeyColumnName: "+colFk);
		return colFk;
	}

	@Override
	public String propertyToColumnName(String propertyName) {
		// transform multiple upper case characters into CamelCase
		Matcher m = MULTI_UPPERCASE.matcher(propertyName);
		while(m.find()){
              // group 0 is the whole pattern matched,
			propertyName = propertyName.replaceAll(m.group(0), m.group(1)+m.group(2).toLowerCase());
		}
		// call original underscore replacement method
		String x = addUnderscores(propertyName); 
//		log.debug(String.format("propertyToColumnName=%s :: <%s>", x, propertyName));
		return x;
	}

	
	@Override
	public String logicalColumnName(String columnName, String propertyName) {
		return StringHelper.isNotEmpty( columnName ) ? columnName : propertyName;
	}

}
