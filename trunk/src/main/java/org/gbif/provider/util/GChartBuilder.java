package org.gbif.provider.util;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.text.SimpleDateFormat;

public class GChartBuilder {
	private final List<String> COLORS = Arrays.asList("CA3D05","224499","76A4FB","80C65A","ff9900","ff0000");
	private final int DATE_PRECISION = 1000000;
	private Map<String, SortedMap<Date, Long>> datasets = new HashMap<String, SortedMap<Date,Long>>();
	
	public void addDataset(Map<Date, Long> dataset, String title){
		datasets.put(title, new TreeMap<Date, Long>(dataset));
	}
	public String generateChartDataString(int width, int height){
		 // comma separated values, | separated datasets. First dataset for x values, second for y
		 String data = "";
		 String datasetColors = "";
		 int colorIndex = 0;
		 // min & max values for both axis
		 Long minYValue = 0L;
		 Long maxYValue = null;
		 Date minXValue = null;
		 Date maxXValue = null;
		 for (SortedMap<Date, Long> dataset : datasets.values()){
			 // define color for this dataset line
			 datasetColors += COLORS.get(colorIndex)+",";
			 colorIndex++;
			 if (colorIndex >= COLORS.size()){
				 // reset color index, repeating colors
				 colorIndex=0;
			 }
			 String yData = "";
			 for (Date date : dataset.keySet()){
				 Long val = dataset.get(date);
				 // update min/max counters
				 if (maxYValue== null || maxYValue < val){
					 maxYValue=val;
				 }
				 if (minXValue == null || minXValue.after(date)){
					 minXValue=date;
				 }
				 if (maxXValue == null || maxXValue.before(date)){
					 maxXValue=date;
				 }
				 // build x-axis dataset immediately into the full data string (y-axis dataset is appended later on)
				 data += date.getTime()/DATE_PRECISION + ",";
				 // build y-axis dataset
				 yData += val+",";
			 }
			 if (data.endsWith(",")){
				 data = data.substring(0, data.length()-1);
			 }
			 if (yData.endsWith(",")){
				 yData = yData.substring(0, yData.length()-1);
			 }
			 data +="|"+yData+"|";
		 }
		 // remove trailing , or |
		 if (data.endsWith("|")){
			 data = data.substring(0, data.length()-1);
		 }
		 if (datasetColors.endsWith(",")){
			 datasetColors = datasetColors.substring(0, datasetColors.length()-1);
		 }
		 
		 // calc y axis label
		 // round max up for nicer values 
		 Long step = 20L * (int) Math.ceil(maxYValue/100.0);
		 maxYValue=step*5L;
		 String yAxis="";
		 for (int i=0; i<6; i++){
			 yAxis+="|"+step*i;
		 }
		 
		 // calc x axis labels
		 SimpleDateFormat sdf =  new SimpleDateFormat("MMM''yy");
		 String xAxis1="|"+sdf.format(minXValue)+"|"+sdf.format(maxXValue);
		 sdf.applyPattern("yyyy");
		 String xAxis2="|"+sdf.format(minXValue)+"|"+sdf.format(maxXValue);
		 //TODO: chg=xaxis step, yaxis step
		 
		 // min, max scale for both axis
		 String minMax = "";
		 String minMaxPerDataset = minXValue.getTime()/DATE_PRECISION+","+maxXValue.getTime()/DATE_PRECISION+","+minYValue+","+maxYValue+",";
		 for (SortedMap<Date, Long> dataset : datasets.values()){
			 minMax += minMaxPerDataset;
		 }
		 if (minMax.endsWith(",")){
			 minMax = minMax.substring(0, minMax.length()-1);
		 }
		 return "http://chart.apis.google.com/chart?cht=lxy&chs="+width+"x"+height+"&chxt=x,y,x&chxl=0:"+xAxis1+"|1:"+yAxis+"|2:"+xAxis2+"&chds="+minMax+"&chco="+datasetColors+"&chd=t:"+data;
	}
}
