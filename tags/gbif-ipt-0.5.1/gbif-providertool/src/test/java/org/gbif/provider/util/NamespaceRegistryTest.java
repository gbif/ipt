package org.gbif.provider.util;

import static org.junit.Assert.*;

import org.gbif.provider.model.ExtensionProperty;
import org.junit.Before;
import org.junit.Test;

public class NamespaceRegistryTest extends ResourceTestBase {

	@Test
	public void testAddExtensionProperty() {
		NamespaceRegistry nsr = new NamespaceRegistry();
		nsr.add("http://ipt.gbif.org");
		nsr.add("http://rs.tdwg.org/dwc/dwcore/");
		nsr.add("http://rs.tdwg.org/dwc/dwcore");
		nsr.add("http://ipt.gbif.org/multimedia");
		nsr.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		nsr.add("http://purl.org/dc/elements/1.1/");
		nsr.add("http://www.w3.org/2001/XMLSchema");
		nsr.add("http://www.w3.org/2000/01/rdf-schema#");
		nsr.add("http://www.w3.org/2002/07/owl#");
		nsr.add("http://www.tdwg.org/schemas/abcd/2.06");
		System.out.println(nsr);
	}

	@Test
	public void testAddResource() {
		setup();
		NamespaceRegistry nsr = new NamespaceRegistry(resource);
		System.out.println(nsr.xmlnsDef());
	}
}
