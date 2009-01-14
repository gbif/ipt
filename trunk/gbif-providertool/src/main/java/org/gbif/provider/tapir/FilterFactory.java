/**
 * 
 */
package org.gbif.provider.tapir;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;


/**
 * Builds the criteria from XML (TAPIR Filter only at present)
 * @author tim
 */
public class FilterFactory {
	public static Filter build(InputStream xml) throws IOException, SAXException {
		Digester digester = new Digester();
		Filter filter = new Filter();
		
		// root of the stack is always the filter
		digester.push(filter);
		
		digester.addObjectCreate("*/and", And.class);
		digester.addSetRoot("*/and", "setRoot");
		
		digester.addObjectCreate("*/or", Or.class);
		digester.addSetRoot("*/or", "setRoot");
		
		addSimpleBooleanOperator(digester, "*/greaterThanOrEquals", GreaterThanOrEquals.class);
		addSimpleBooleanOperator(digester, "*/lessThanOrEquals", LessThanOrEquals.class);
		addSimpleBooleanOperator(digester, "*/greaterThan", GreaterThan.class);
		addSimpleBooleanOperator(digester, "*/lessThan", LessThan.class);
		addSimpleBooleanOperator(digester, "*/equals", Equals.class);
		addSimpleBooleanOperator(digester, "*/like", Like.class);
		addSimpleBooleanOperator(digester, "*/isNull", IsNull.class);
		
		// TODO handle IN
		
		digester.addCallMethod("*/concept", "setProperty", 1);
		digester.addCallParam("*/concept", 0, "id");
		
		digester.addCallMethod("*/parameter", "setValue", 1);
		digester.addCallParam("*/parameter", 0, "name");
		
		digester.addCallMethod("*/literal", "setValue", 1);
		digester.addCallParam("*/literal", 0, "value");
		
		digester.parse(xml);
		
		return filter;
	}
	
	protected static void addSimpleBooleanOperator(Digester digester, String path, Class<?> operator) {
		digester.addObjectCreate(path, operator);
		digester.addSetRoot(path, "setRoot");
		digester.addSetNext(path, "addOperand", LogicalOperator.class.getName());
		
	}

}
