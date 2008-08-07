package org.gbif.provider.util;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.gbif.provider.model.dto.StatsCount;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.gchartjava.GeographicalArea;

public class GPieBuilderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPie(){
		GChartBuilder gb = new GChartBuilder();
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
		String result = gb.generatePiaChartUrl(320, 160, "Phonecalls per Friend", data, totalRecords);
//		System.out.println(result);
        assertEquals(expectedString, result);
        
		expectedString = "http://chart.apis.google.com/chart?cht=p&chs=320x160&chl=Harald|Stefan|Freerk|Fritz|Bernd&chts=000000,16&chco=76A4FB,D7E9F5,18427D,80C65A,CA3D05&chd=e:LhApKPYUQA";
		result = gb.generatePiaChartUrl(320, 160, data, totalRecords);
//		System.out.println(result);
        assertEquals(expectedString, result);
	}
	
	@Test
	public void testMap(){
		GChartBuilder gb = new GChartBuilder();
		List<StatsCount> data = new ArrayList<StatsCount>();
		data.add(new StatsCount("DE", 1012l));
		data.add(new StatsCount("UK", 790l));
		data.add(new StatsCount("FR", 1291l));
		data.add(new StatsCount("AR", 8222l));
		data.add(new StatsCount("BR", 12122l));
		data.add(new StatsCount("DA", 522l));
		data.add(new StatsCount("RU", 1342l));
		data.add(new StatsCount("Belgium", 18842l));
		String expectedString = "http://chart.apis.google.com/chart?cht=t&chtm=world&chs=440x220&chld=DEUKFRARBRDARU&chco=FFFFFF,EDF0D4,13390D&chf=bg,s,E0F2FF&chd=e:DNCkD2bho9BSEf";
		String result = gb.generateMapChartUrl(440, 220, data);
//		System.out.println(result);
        assertEquals(expectedString, result);
	}	

	@Test
	public void testGetArea(){
		GeographicalArea a;
		a = GeographicalArea.valueOf("SOUTH_AMERICA");
		a = GeographicalArea.valueOf("south_america".toUpperCase());
		System.out.println(a);
	}
}
