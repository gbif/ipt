package org.gbif.provider.util;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.gbif.provider.model.dto.StatsCount;
import org.junit.Before;
import org.junit.Test;

public class GPieBuilderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPie(){
		GPieBuilder gb = new GPieBuilder();
		List<StatsCount> data = new ArrayList<StatsCount>();
		data.add(new StatsCount("Harald", 1012l));
		data.add(new StatsCount("Stefan", 79l));
		data.add(new StatsCount("Freerk", 891l));
		data.add(new StatsCount("Fritz", 2122l));
		data.add(new StatsCount("Bernd", 1422l));
		Long totalRecords = 0l;
		for (StatsCount val : data){
			totalRecords += val.getCount();
		}
		String expectedString = "http://chart.apis.google.com/chart?cht=p&chs=320x160&chl=Harald|Stefan|Freerk|Fritz|Bernd&chts=000000,16&chco=76A4FB,D7E9F5,18427D,80C65A,CA3D05&chd=e:LhApKPYUQA&chtt=Phonecalls+per+Friend";
		String result = gb.generateChartDataString(320, 160, "Phonecalls per Friend", data, totalRecords);
		System.out.println(result);
        assertEquals(expectedString, result);
        
		expectedString = "http://chart.apis.google.com/chart?cht=p&chs=320x160&chl=Harald|Stefan|Freerk|Fritz|Bernd&chts=000000,16&chco=76A4FB,D7E9F5,18427D,80C65A,CA3D05&chd=e:LhApKPYUQA";
		result = gb.generateChartDataString(320, 160, data, totalRecords);
		System.out.println(result);
        assertEquals(expectedString, result);
	}
}
