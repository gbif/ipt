package org.gbif.provider.util;


import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

public class GPieBuilderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPie(){
		GPieBuilder gb = new GPieBuilder();
		java.util.Map<String, Long> data = new TreeMap<String, Long>();
		data.put("Harald", 1012l);
		data.put("Stefan", 79l);
		data.put("Freerk", 891l);
		data.put("Fritz", 2122l);
		data.put("Bernd", 1422l);
		Long totalRecords = 0l;
		for (Long val : data.values()){
			totalRecords += val;
		}
		String expectedString = "http://chart.apis.google.com/chart?cht=p&chs=320x160&chl=Bernd|Freerk|Fritz|Harald|Stefan&chts=000000,16&chco=76A4FB,80C65A,CA3D05,FFC624,666666&chd=e:QAKPYULhAp&chtt=Phonecalls+per+Friend";
		String result = gb.generateChartDataString(320, 160, "Phonecalls per Friend", data, totalRecords);
//		System.out.println(result);
        assertEquals(expectedString, result);
        
		expectedString = "http://chart.apis.google.com/chart?cht=p&chs=320x160&chl=Bernd|Freerk|Fritz|Harald|Stefan&chts=000000,16&chco=76A4FB,80C65A,CA3D05,FFC624,666666&chd=e:QAKPYULhAp";
		result = gb.generateChartDataString(320, 160, data, totalRecords);
//		System.out.println(result);
        assertEquals(expectedString, result);
	}
}
