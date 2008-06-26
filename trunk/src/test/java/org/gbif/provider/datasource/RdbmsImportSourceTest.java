package org.gbif.provider.datasource;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.gbif.provider.datasource.impl.RdbmsImportSource;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.util.BaseExternalDatasourceTest;
import org.junit.Before;
import org.junit.Test;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;


public class RdbmsImportSourceTest extends BaseExternalDatasourceTest{
	private ImportSource source;
	
	private void setUpSource() {
		// @Before triggered in superclasses doesnt work for me. Should really, but dont know why. So I use direct calls instead
		setUpExternalDatasource();
		String sql = "select * from observation join taxon on taxon_fk=taxon_id";
		try {
			ResultSet rs = this.datasourceInspectionDao.executeSql(sql);
	        // get resource
	        ViewMapping view = getTestResource().getCoreMapping();
	        // create import source
			source = RdbmsImportSource.getInstance(rs, view);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRdbmsSourceIterator(){
		setUpSource();
		int i = 0;
		for (SourceRow row : source){
			System.out.println(row);
			if (i>20){
				break;
			}
			i++;
		}
	}

}
