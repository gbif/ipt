package org.gbif.provider.service;

import java.util.Set;

import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.tapir.ParseException;
import org.gbif.provider.tapir.TapirException;
import org.gbif.provider.tapir.filter.Filter;

public interface ExtensionPropertyManager extends GenericManager<ExtensionProperty>{
	/** Get a single ExtensionProeprty by its qualified concept name.
	 * As there might be more than one core extension using the same qualified concept name, 
	 * the ExtensionType narrows down the search to just one core type. 
	 * Extensions other than the core are not being searched.
	 * @param qName
	 * @param type
	 * @return
	 */

	public ExtensionProperty getCorePropertyByQualName(String qName);

	/** Get a single ExtensionProeprty by its simple name.
	 * As there might be more than one core extension using the same name, 
	 * the ExtensionType narrows down the search to just one core type.
	 * In case there are multiple matches a Hibernate exception will be thrown. 
	 * Extensions other than the core are not being searched.
	 * @param name
	 * @param type
	 * @return
	 */
	public ExtensionProperty getCorePropertyByName(String name);
	
	/**Iterates through all ComparisonOperators and replaces the existing ExtensionProperties
	 * with persistent properties looked up by their qualified name and the type of resource
	 * @param filter the filter to iterate through. Properties will be replaced in this object
	 * @param type the type of (core) extension to narrow down qualified name to extension property "homonyms"
	 * @return
	 * @throws ParseException 
	 */
	public Set<ExtensionProperty> lookupFilterCoreProperties(Filter filter) throws ParseException;
}
