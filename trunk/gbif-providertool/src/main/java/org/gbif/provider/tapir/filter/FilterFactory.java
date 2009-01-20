/**
 * 
 */
package org.gbif.provider.tapir.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.commons.digester.RegexRules;
import org.apache.commons.digester.SimpleRegexMatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;


/**
 * Builds the criteria from XML (TAPIR Filter only at present)
 * @author tim
 */
public class FilterFactory {
	protected static Log log = LogFactory.getLog(FilterFactory.class);
	
	public static Filter build(InputStream xml) throws IOException, SAXException {
		return build(xml, new HashMap<String, String>());
	}
	
	
	public static Filter build(InputStream xml, Map<String, String> params) throws IOException, SAXException {
		Digester digester = new Digester();
		digester.setNamespaceAware(true);
		
		// need to use reg expressions since the wildcards in the middle
		// of the path are not supported
		digester.setRules( new RegexRules(new SimpleRegexMatcher()) );
		Filter filter = new Filter();
		
		// root of the stack is always the filter
		digester.push(filter);
		
		addOperator(digester, "*/filter*and", And.class);
		addOperator(digester, "*/filter*or", Or.class);
		addOperator(digester, "*/filter*in", In.class);
		addOperator(digester, "*/filter*greaterThanOrEquals", GreaterThanOrEquals.class);
		addOperator(digester, "*/filter*lessThanOrEquals", LessThanOrEquals.class);
		addOperator(digester, "*/filter*greaterThan", GreaterThan.class);
		addOperator(digester, "*/filter*lessThan", LessThan.class);
		addOperator(digester, "*/filter*equals", Equals.class);
		addOperator(digester, "*/filter*like", Like.class);
		addOperator(digester, "*/filter*isNull", IsNull.class);
		addOperator(digester, "*/filter*not", Not.class);
				
		digester.addCallMethod("*/filter*concept", "setProperty", 1);
		digester.addCallParam("*/filter*concept", 0, "id");
		
		digester.addCallMethod("*/filter*parameter", "setValue", 2, new Class[]{Map.class, String.class});
		digester.addObjectParam("*/filter*parameter", 0, params);
		digester.addCallParam("*/filter*parameter", 1, "name");
		
		digester.addCallMethod("*/filter*literal", "setValue", 1);
		digester.addCallParam("*/filter*literal", 0, "value");
		
		log.debug("Parsing xml");
		digester.parse(xml);
		log.debug("Parsed xml");
		
		return filter;
	}
	
	protected static void addOperator(Digester digester, String path, Class<?> operator) {
		
		digester.addObjectCreate(path, operator);
		digester.addSetRoot(path, "setRoot");
		digester.addSetNext(path, "addOperand", BooleanOperator.class.getName());
		
	}

}
