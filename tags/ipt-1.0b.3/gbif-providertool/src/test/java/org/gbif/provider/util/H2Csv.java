package org.gbif.provider.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.h2.tools.Csv; 
import org.junit.Test;

public class H2Csv {

	@Test
	public void testH2Csv() throws SQLException{
		ResultSet rs = Csv.getInstance().read("/Users/markus/Desktop/rank.csv", null, null); 
		ResultSetMetaData meta = rs.getMetaData(); 
		while (rs.next()) { 
		    for (int i = 0; i < meta.getColumnCount(); i++) { 
		        System.out.println(meta.getColumnLabel(i + 1) +": " + rs.getString(i + 1)); 
		    } 
		    System.out.println(); 
		} 
		rs.close(); 
	}
}
