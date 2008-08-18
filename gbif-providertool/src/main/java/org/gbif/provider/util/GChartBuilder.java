package org.gbif.provider.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;

import com.googlecode.gchartjava.Color;
import com.googlecode.gchartjava.Country;
import com.googlecode.gchartjava.Fill;
import com.googlecode.gchartjava.GeographicalArea;
import com.googlecode.gchartjava.LinearGradientFill;
import com.googlecode.gchartjava.MapChart;
import com.googlecode.gchartjava.PieChart;
import com.googlecode.gchartjava.PoliticalBoundary;
import com.googlecode.gchartjava.Slice;
import com.googlecode.gchartjava.SolidFill;

public class GChartBuilder {
    private final static Log log = LogFactory.getLog(GChartBuilder.class);

    private static final List<Color> COLORS = Arrays.asList(new Color("76A4FB"),new Color("D7E9F5"),new Color("18427D"),new Color("80C65A"),new Color("CA3D05"),new Color("B4C24B"),new Color("FF7C0A"), new Color("6DA474"), new Color("ffc624"),new Color("666666"));
	private static final Fill MAP_BACK = new SolidFill(new Color("e0f2ff"));
	private static final Color MAP_EMPTY = new Color("ffffff");
	private static final Color MAP_LOW = new Color("EDF0D4");
	private static final Color MAP_HIGH = new Color("13390D");
	
	  
	public static String generatePiaChartUrl(int width, int height, List<StatsCount> data, Long totalRecords){
		return generatePiaChartUrl(width, height, null, data, totalRecords);
	}

	public static String generatePiaChartUrl(int width, int height, String title, List<StatsCount> data, Long totalRecords){
		LinkedList<Color> colors = new LinkedList<Color>(COLORS);
		List<Slice> slices = new ArrayList<Slice>();
		for (StatsCount stat: data){
			// rotate through colors
			Color c = colors.poll();
			colors.add(c);
			// calculate percentage from total
			int perc = (int) (100 * stat.getCount() / totalRecords);
			slices.add(new Slice(perc, c, stat.getLabel()));
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
        
        // surround | with spaces
//        result = result.replaceAll("\\|", "\\\\|");
		return result;
	}

	
	public static String generateMapChartUrl(int width, int height, List<StatsCount> data){
		return generateMapChartUrl(width, height, data, null);
	}
	public static String generateMapChartUrl(int width, int height, List<StatsCount> data, GeographicalArea area){
		if (area == null){
			// default is a world map
			area = GeographicalArea.WORLD;
		}
		List<PoliticalBoundary> cdata = translateIntoPoliticalBoundaries(data);
		MapChart map = new MapChart(area);
		map.addPoliticalBoundaries(cdata);
		map.setSize(width, height);
		map.setColorGradient(MAP_EMPTY, MAP_LOW, MAP_HIGH);
		map.setBackgroundFill(MAP_BACK);

		return map.createURLString();
	}

	private static List<PoliticalBoundary> translateIntoPoliticalBoundaries(List<StatsCount> data){
		List<PoliticalBoundary> cdata = new ArrayList<PoliticalBoundary>();
		Long maxRecords = 1l;
		if (!data.isEmpty()){
			maxRecords = Collections.max(data).getCount();
		}
		for (StatsCount stat : data){
			// check that ~ISO code exists, i.e. must be 2 characters only!
			if(stat.getLabel().length()==2){
				// calculate percentage from total
				int perc = (int) (100 * stat.getCount() / maxRecords);
				Country c = new Country(stat.getLabel(), perc);
				cdata.add(c);
			}else{
				log.debug(String.format("Country with invalid ISO code %s ignored",stat.getLabel()));
			}
		}
		return cdata;
	}
}
