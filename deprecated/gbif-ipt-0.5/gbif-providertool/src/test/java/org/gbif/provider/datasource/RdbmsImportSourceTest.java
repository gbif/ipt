package org.gbif.provider.datasource;


import org.gbif.provider.datasource.impl.ImportSourceFactory;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.util.BaseExternalDatasourceTest;
import org.junit.Test;


public class RdbmsImportSourceTest extends BaseExternalDatasourceTest{
	protected ImportSource source;
	
	protected void setUpSource() {
		// @Before triggered in superclasses doesnt work for me. Should really, but dont know why. So I use direct calls instead
		setUpExternalDatasource();
        try {
			// create import source
			source = ImportSourceFactory.newInstance(this.getTestRdbmsResource(), getTestRdbmsResource().getCoreMapping());
		} catch (ImportSourceException e) {
			e.printStackTrace();
			fail("Couldnt setup import source");
		}
	}
	
	@Test
	public void testRdbmsSourceIterator(){
		setUpSource();
		int i = 0;
		for (ImportRecord row : source){
			if (i>20){
				break;
			}
			i++;
		}
	}

}
