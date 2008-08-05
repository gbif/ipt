package org.gbif.provider.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.googlecode.gchartjava.Color;
import com.googlecode.gchartjava.PieChart;
import com.googlecode.gchartjava.Slice;

public class GPieBuilder {
	private static final List<Color> COLORS = Arrays.asList(new Color("76A4FB"),new Color("80C65A"),new Color("CA3D05"), new Color("ffc624"),new Color("666666"));
	
	public static String generateChartDataString(int width, int height, Map<String, Long> data, Long totalRecords){
		return generateChartDataString(width, height, null, data, totalRecords);
	}

	public static String generateChartDataString(int width, int height, String title, Map<String, Long> data, Long totalRecords){
		LinkedList<Color> colors = new LinkedList<Color>(COLORS);
		List<Slice> slices = new ArrayList<Slice>();
		for (String label : data.keySet()){
			// rotate through colors
			Color c = colors.poll();
			colors.add(c);
			// calculate percentage from total
			int perc = (int) (100 * data.get(label) / totalRecords);
			slices.add(new Slice(perc, c, label));
		}

        PieChart chart = new PieChart(slices);
        chart.setSize(width, height);
        chart.setThreeD(false);
        
        String result;
        if (title != null){
        	chart.setTitle(title,Color.BLACK,16);
        	result = chart.createURLString();
        }else{
        	chart.setTitle("",Color.BLACK,16);
        	result = chart.createURLString().split("&chtt=")[0];
        }
		return result;
	}
	
}
