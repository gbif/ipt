package org.gbif.provider.datasource;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.gbif.provider.datasource.impl.RdbmsImportSource;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.CoreViewMapping;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.util.BaseExternalDatasourceTest;
import org.junit.Before;
import org.junit.Test;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;


public class RdbmsImportSourceTest extends BaseExternalDatasourceTest{
	protected ImportSource source;
	
	protected void setUpSource() {
		// @Before triggered in superclasses doesnt work for me. Should really, but dont know why. So I use direct calls instead
		setUpExternalDatasource();
		String sql = "select * from observation join taxon on taxon_fk=taxon_id";
		try {
			ResultSet rs = this.datasourceInspectionDao.executeSql(sql);
	        // get resource
	        CoreViewMapping view = getTestResource().getCoreMapping();
	        // create import source
			source = RdbmsImportSource.newInstance(rs, view);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRdbmsSourceIterator(){
		setUpSource();
		int i = 0;
		for (CoreRecord row : source){
			if (i>20){
				break;
			}
			i++;
		}
	}

}
