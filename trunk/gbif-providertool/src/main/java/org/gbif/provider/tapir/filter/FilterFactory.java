/**
 * 
 */
package org.gbif.provider.tapir.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;


/**
 * Builds the criteria from XML (TAPIR Filter only at present)
 * @author tim
 */
public class FilterFactory {
	public static Filter build(InputStream xml) throws IOException, SAXException {
		return build(xml, new HashMap<String, String[]>());
	}
	
	
	public static Filter build(InputStream xml, Map<String, String[]> params) throws IOException, SAXException {
		Digester digester = new Digester();
		Filter filter = new Filter();
		
		// root of the stack is always the filter
		digester.push(filter);
		
		digester.addObjectCreate("*/and", And.class);
		digester.addSetRoot("*/and", "setRoot");
		
		digester.addObjectCreate("*/or", Or.class);
		digester.addSetRoot("*/or", "setRoot");
				
		digester.addObjectCreate("*/in", In.class);
		digester.addSetRoot("*/in", "setRoot");
				
		addSimpleOperator(digester, "*/greaterThanOrEquals", GreaterThanOrEquals.class);
		addSimpleOperator(digester, "*/lessThanOrEquals", LessThanOrEquals.class);
		addSimpleOperator(digester, "*/greaterThan", GreaterThan.class);
		addSimpleOperator(digester, "*/lessThan", LessThan.class);
		addSimpleOperator(digester, "*/equals", Equals.class);
		addSimpleOperator(digester, "*/like", Like.class);
		addSimpleOperator(digester, "*/isNull", IsNull.class);
		addSimpleOperator(digester, "*/not", Not.class);
				
		digester.addCallMethod("*/concept", "setProperty", 1);
		digester.addCallParam("*/concept", 0, "id");
		
		
		digester.addCallMethod("*/parameter", "setValue", 2, new Class[]{Map.class, String.class});
		digester.addObjectParam("*/parameter", 0, params);
		digester.addCallParam("*/parameter", 1, "name");
		
		digester.addCallMethod("*/literal", "setValue", 1);
		digester.addCallParam("*/literal", 0, "value");
		
		digester.parse(xml);
		
		return filter;
	}
	
	protected static void addSimpleOperator(Digester digester, String path, Class<?> operator) {
		digester.addObjectCreate(path, operator);
		digester.addSetRoot(path, "setRoot");
		digester.addSetNext(path, "addOperand", BooleanOperator.class.getName());
		
	}

}
