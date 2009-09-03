/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * TODO: Documentation.
 * 
 */
public class UploadChartBuilder {

  private class Dataset<K, V> {
    public String title;
    public SortedMap<K, V> data;
  }

  // dark blue, light blue, green, red, dark yellow, dark grey
  private static final List<String> COLORS = Arrays.asList("224499", "76A4FB",
      "80C65A", "CA3D05", "ffc624", "333333");
  private static final int DATE_PRECISION = 1000000;

  private final List<Dataset<Date, Long>> datasets = new ArrayList<Dataset<Date, Long>>();

  public void addDataset(Map<Date, Long> dataset, String title) {
    Dataset<Date, Long> d = new Dataset<Date, Long>();
    d.title = title;
    d.data = new TreeMap<Date, Long>(dataset);
    datasets.add(d);
  }

  /**
   * Removes all datasets from the chartbuilder
   */
  public void clear() {
    datasets.clear();
  }

  public String generateChartDataString(int width, int height) {
    // comma separated values, | separated datasets. First dataset for x values,
    // second for y
    String data = "";
    // dataset properties
    String datasetTitles = "";
    String datasetColors = "";
    int colorIndex = 0;
    // min & max values for both axis
    Long minYValue = 0L;
    Long maxYValue = null;
    Date minXValue = null;
    Date maxXValue = null;
    for (Dataset<Date, Long> d : datasets) {
      // add dataset title
      datasetTitles += d.title + " |";
      // define color for this dataset line
      datasetColors += COLORS.get(colorIndex) + ",";
      colorIndex++;
      if (colorIndex >= COLORS.size()) {
        // reset color index, repeating colors
        colorIndex = 0;
      }
      String yData = "";
      for (Date date : d.data.keySet()) {
        Long val = d.data.get(date);
        // update min/max counters
        if (maxYValue == null || maxYValue < val) {
          maxYValue = val;
        }
        if (minXValue == null || minXValue.after(date)) {
          minXValue = date;
        }
        if (maxXValue == null || maxXValue.before(date)) {
          maxXValue = date;
        }
        // build x-axis dataset immediately into the full data string (y-axis
        // dataset is appended later on)
        data += date.getTime() / DATE_PRECISION + ",";
        // build y-axis dataset
        yData += val + ",";
      }
      // remove trailing , or |
      data = trimString(data);
      yData = trimString(yData);

      // append y-axis dataset
      data += "|" + yData + "|";
    }

    // check if any data was submitted at all!
    if (maxYValue == null || minXValue == null || maxXValue == null) {
      return null;
    }

    // calc y axis label
    // round max up for nicer values
    Long step = 20L * (int) Math.ceil(maxYValue / 100.0);
    maxYValue = step * 5L;
    String yAxis = "|";
    // for min/max=0 it doesnt make sense to have many xaxis labels
    if (step != 0) {
      for (int i = 1; i < 6; i++) {
        yAxis += "|" + step * i;
      }
    }

    // calc x axis labels
    SimpleDateFormat sdf = new SimpleDateFormat("MMM''yy");
    String xAxis1 = "|" + sdf.format(minXValue) + "|" + sdf.format(maxXValue);
    sdf.applyPattern("yyyy");
    String xAxis2 = "|" + sdf.format(minXValue) + "|" + sdf.format(maxXValue);
    // TODO: chg=xaxis step, yaxis step

    // min, max scale for both axis
    String minMax = "";
    String minMaxPerDataset = minXValue.getTime() / DATE_PRECISION + ","
        + maxXValue.getTime() / DATE_PRECISION + "," + minYValue + ","
        + maxYValue + ",";
    for (Dataset d : datasets) {
      minMax += minMaxPerDataset;
    }

    // remove trailing , or |
    data = trimString(data);
    datasetTitles = trimString(datasetTitles);
    datasetColors = trimString(datasetColors);
    minMax = trimString(minMax);
    return "http://chart.apis.google.com/chart?cht=lxy&chs=" + width + "x"
        + height + "&chxt=x,r&chxl=0:" + xAxis1 + "|1:" + yAxis + "&chds="
        + minMax + "&chco=" + datasetColors + "&chdlp=l&chdl=" + datasetTitles
        + "&chd=t:" + data;
  }

  private String trimString(String x) {
    if (x.endsWith(",") || x.endsWith("|")) {
      x = x.substring(0, x.length() - 1);
    }
    return x;
  }
}
