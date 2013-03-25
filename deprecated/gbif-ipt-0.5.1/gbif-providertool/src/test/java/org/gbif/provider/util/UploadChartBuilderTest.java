package org.gbif.provider.util;

import static org.junit.Assert.fail;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;

public class UploadChartBuilderTest {
	private UploadChartBuilder chartBuilder;
	
	@Before
	public void setUp() throws Exception {
		chartBuilder = new UploadChartBuilder();
		Long baseTime = 839977698718L;
		Long dateTime;
		Random rnd = new Random();
		int datasetSize = rnd.nextInt(50);
		for (String t : Arrays.asList("set1","set2","set3","set4")){
			Map<Date, Long> dataset = new HashMap<Date, Long>();
			for (int i=0; i<datasetSize; i++){
				dateTime = 100000L * rnd.nextInt(2000000);
				Long val = Long.valueOf(rnd.nextInt(1000));
				if (t.equals("set4")){
					val=val*10;
				}
				dataset.put(new Date(dateTime), val);
			}
			chartBuilder.addDataset(dataset, t);
		}
	}

	@Test
	public void testGenerateChartDataString() {
		String result = chartBuilder.generateChartDataString(300,200);
//		System.out.println(result);
		chartBuilder.clear();
		result = chartBuilder.generateChartDataString(300,200);
//		System.out.println(result);
	}

}
