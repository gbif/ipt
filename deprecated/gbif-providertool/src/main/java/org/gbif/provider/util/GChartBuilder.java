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

import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.dto.StatsCount;

import com.googlecode.gchartjava.AxisInfo;
import com.googlecode.gchartjava.AxisStyle;
import com.googlecode.gchartjava.BarChart;
import com.googlecode.gchartjava.BarChartDataSeries;
import com.googlecode.gchartjava.Color;
import com.googlecode.gchartjava.Country;
import com.googlecode.gchartjava.DataUtil;
import com.googlecode.gchartjava.Fill;
import com.googlecode.gchartjava.GeographicalArea;
import com.googlecode.gchartjava.MapChart;
import com.googlecode.gchartjava.PieChart;
import com.googlecode.gchartjava.PoliticalBoundary;
import com.googlecode.gchartjava.Slice;
import com.googlecode.gchartjava.SolidFill;
import com.googlecode.gchartjava.AxisStyle.AlignmentEnum;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class GChartBuilder {
  public static final Color OTHER_COLOR = new Color("aaaaaa");
  public static final String OTHER_LABEL = "other";
  private static final Log log = LogFactory.getLog(GChartBuilder.class);
  private static final List<Color> COLORS = Arrays.asList(new Color("76A4FB"),
      new Color("D7E9F5"), new Color("18427D"), new Color("80C65A"), new Color(
          "CA3D05"), new Color("B4C24B"), new Color("FF7C0A"), new Color(
          "6DA474"), new Color("ffc624"), new Color("666666"));
  private static final Fill MAP_BACK = new SolidFill(new Color("e0f2ff"));
  private static final Color MAP_EMPTY = new Color("ffffff");
  private static final Color MAP_LOW = new Color("EDF0D4");
  private static final Color MAP_HIGH = new Color("13390D");
  private static final int BAR_WIDTH = 3;
  private static final int[] BAR_SIZES = {
      1, 2, 5, 10, 25, 50, 100, 200, 500, 1000};

  public static String generateChronoChartUrl(int width, int height,
      List<StatsCount> data) {
    return generateChronoChartUrl(width, height, null, data);
  }

  public static String generateChronoChartUrl(int width, int height,
      String title, List<StatsCount> data) {
    final int maxBars = width / (BAR_WIDTH + 9);
    float[] gData = new float[maxBars];
    Integer minYear = null;
    Integer maxYear = null;

    // can only show 25 bars. aggregate data so that it fits
    for (StatsCount stat : data) {
      Integer year = (Integer) stat.getValue();
      if (year != null && (minYear == null || year < minYear)) {
        minYear = year;
      }
      if (year != null && (maxYear == null || year > maxYear)) {
        maxYear = year;
      }
    }

    // assert that there are some serious years existing before proceeding
    AxisInfo xAxisInfo = null;
    AxisInfo yAxisInfo = null;
    if (maxYear != null && minYear != null) {
      Integer period = maxYear - minYear;
      int barSize = 1;
      Float maxValue = 0f;

      for (int s : BAR_SIZES) {
        if (period / s <= maxBars) {
          barSize = s;
          break;
        }
      }
      // create new dataset with 1 bar = step size of years
      Integer startYear = minYear - (minYear % barSize);
      int barIdx = 0;
      for (StatsCount stat : data) {
        Integer year = (Integer) stat.getValue();
        if (year != null) {
          barIdx = (year - startYear) / barSize;
          gData[barIdx] += stat.getCount();
          if (gData[barIdx] > maxValue) {
            maxValue = gData[barIdx];
          }
        }
      }
      // define axis legend
      List<String> labels = new ArrayList<String>();
      for (int i = 0; i < maxBars; i++) {
        if (i % 2 == 0) {
          labels.add(String.valueOf(startYear + barSize * i));
        } else {
          labels.add("");
        }
      }
      xAxisInfo = new AxisInfo(labels);
      xAxisInfo.setAxisStyle(new AxisStyle(Color.GRAY, 8, AlignmentEnum.CENTER));
      yAxisInfo = new AxisInfo(0, maxValue.intValue());
    }

    BarChartDataSeries series = new BarChartDataSeries(
        DataUtil.normalize(gData), COLORS.get(1));
    BarChart chart = new BarChart(series);
    chart.setSize(width, height);
    chart.setBarWidth(BAR_WIDTH);
    // legend
    if (xAxisInfo != null) {
      chart.addXAxisInfo(xAxisInfo);
      chart.addYAxisInfo(yAxisInfo);
    }

    // String chartUrl =
    // "http://chart.apis.google.com/chart?cht=bvs&chs=320x160&chd=t:10,50,60,40,50,60,100,40,20,80,40,77,20,50,60,100,40,20,80,40,7,15,5,9,55,7850,40,50,60,100,40,20,60,100,13,56,48,13,20,10,50,78,60,80,40,50,60,100,40,20,40,50,60,0,80,40,50,60,100,40,20&chco=c6d9fd&chbh=3";

    String result;
    if (title != null) {
      chart.setTitle(title, Color.BLACK, 16);
      result = chart.createURLString();
    } else {
      chart.setTitle("", Color.BLACK, 16);
      result = chart.createURLString().split("&chtt=")[0];
    }

    return result;
  }

  public static String generateMapChartUrl(int width, int height,
      List<StatsCount> data) {
    return generateMapChartUrl(width, height, data, null);
  }

  public static String generateMapChartUrl(int width, int height,
      List<StatsCount> data, GeographicalArea area) {
    if (area == null) {
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

  public static String generatePiaChartUrl(int width, int height,
      List<StatsCount> data) {
    return generatePieChartUrl(width, height, null, data);
  }

  public static String generatePieChartUrl(int width, int height, String title,
      List<StatsCount> data) {
    // treat no data. still show a white chart
    List<Color> addColors = new ArrayList<Color>();
    if (data == null || data.isEmpty()) {
      StatsCount stat = new StatsCount("", 1L);
      data = new ArrayList<StatsCount>();
      data.add(stat);
      addColors.add(new Color("efefef"));
    }

    Long totalRecords = 0L;
    for (StatsCount stat : data) {
      totalRecords += stat.getCount();
    }

    List<Slice> slices = new ArrayList<Slice>();
    if (totalRecords > 0L) {
      LinkedList<Color> colors = new LinkedList<Color>(addColors);
      colors.addAll(COLORS);
      for (StatsCount stat : data) {
        Color c;
        if (stat.getLabel().equals(OTHER_LABEL)) {
          c = OTHER_COLOR;
        } else {
          // rotate through colors
          c = colors.poll();
        }
        colors.add(c);
        // calculate percentage from total
        int perc = (int) (100 * stat.getCount() / totalRecords);
        try {
          slices.add(new Slice(perc, c, URLEncoder.encode(stat.getLabel(),
              Constants.ENCODING)));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }
    }

    PieChart chart = new PieChart(slices);
    chart.setSize(width, height);
    chart.setThreeD(false);

    String result;
    if (title != null) {
      chart.setTitle(title, Color.BLACK, 16);
      result = chart.createURLString();
    } else {
      chart.setTitle("", Color.BLACK, 16);
      result = chart.createURLString().split("&chtt=")[0];
    }

    return result;
  }

  public static String generateUploadChartUrl(int width, int height,
      List<UploadEvent> data) {
    return generateUploadChartUrl(width, height, null, data);
  }

  public static String generateUploadChartUrl(int width, int height,
      String title, List<UploadEvent> data) {
    final int maxBars = width / (BAR_WIDTH + 9);
    float[] gData = new float[maxBars];
    Integer minYear = null;
    Integer maxYear = null;

    // for (UploadEvent event: data){

    BarChartDataSeries series = new BarChartDataSeries(
        DataUtil.normalize(gData), COLORS.get(1));
    BarChart chart = new BarChart(series);
    chart.setSize(width, height);
    chart.setBarWidth(BAR_WIDTH);

    // String chartUrl =
    // "http://chart.apis.google.com/chart?cht=bvs&chs=320x160&chd=t:10,50,60,40,50,60,100,40,20,80,40,77,20,50,60,100,40,20,80,40,7,15,5,9,55,7850,40,50,60,100,40,20,60,100,13,56,48,13,20,10,50,78,60,80,40,50,60,100,40,20,40,50,60,0,80,40,50,60,100,40,20&chco=c6d9fd&chbh=3";

    String result;
    if (title != null) {
      chart.setTitle(title, Color.BLACK, 16);
      result = chart.createURLString();
    } else {
      chart.setTitle("", Color.BLACK, 16);
      result = chart.createURLString().split("&chtt=")[0];
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  private static List<PoliticalBoundary> translateIntoPoliticalBoundaries(
      List<StatsCount> data) {
    List<PoliticalBoundary> cdata = new ArrayList<PoliticalBoundary>();
    Long maxRecords = 1L;
    if (!data.isEmpty()) {
      maxRecords = ((StatsCount) Collections.min(data)).getCount();
    }

    if (maxRecords > 0L) {
      for (StatsCount stat : data) {
        // check that ~ISO code exists, i.e. must be 2 characters only!
        if (stat.getCount() > 0L && stat.getLabel().length() == 2) {
          // calculate percentage from total
          int perc = (int) (100 * stat.getCount() / maxRecords);
          Country c = new Country(stat.getLabel(), perc);
          cdata.add(c);
        } else {
          log.debug(String.format("Country with invalid ISO code %s ignored",
              stat.getLabel()));
        }
      }
    }
    return cdata;
  }
}
