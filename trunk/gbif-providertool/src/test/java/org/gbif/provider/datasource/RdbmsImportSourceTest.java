package org.gbif.provider.datasource;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.gbif.provider.datasource.impl.RdbmsImportSource;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.util.BaseExternalDatasourceTest;
import org.junit.Before;
import org.junit.Test;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;


public class RdbmsImportSourceTest extends BaseExternalDatasourceTest{
	protected ImportSource source;
	
	protected void setUpSource() {
		// @Before triggered in superclasses doesnt work for me. Should really, but dont know why. So I use direct calls instead
		setUpExternalDatasource();
        // use test resource
        ViewCoreMapping view = getTestResource().getCoreMapping();
		try {
			ResultSet rs = this.datasourceInspectionDao.executeSql(view.getSourceSql());
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
		for (ImportRecord row : source){
			if (i>20){
				break;
			}
			i++;
		}
	}

}
