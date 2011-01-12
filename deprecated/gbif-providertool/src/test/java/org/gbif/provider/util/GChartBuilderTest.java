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

import org.gbif.provider.model.dto.StatsCount;

import com.googlecode.gchartjava.GeographicalArea;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class GChartBuilderTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testChrono() {
    GChartBuilder gb = new GChartBuilder();
    List<StatsCount> data = new ArrayList<StatsCount>();
    data.add(new StatsCount(null, "1972", 1972, 1012L));
    data.add(new StatsCount(null, "1989", 1989, 790L));
    data.add(new StatsCount(null, "1990", 1990, 1291L));
    data.add(new StatsCount(null, "1979", 1979, 8222L));
    data.add(new StatsCount(null, "1986", 1986, 12122L));
    data.add(new StatsCount(null, "1984", 1984, 522L));
    data.add(new StatsCount(null, "1977", 1977, 1342L));
    data.add(new StatsCount(null, "2001", 2001, 18842L));
    String result = gb.generateChronoChartUrl(440, 220, data);
    // System.out.println(result);
    result = gb.generateChronoChartUrl(220, 110, data);
    // System.out.println(result);

    data = new ArrayList<StatsCount>();
    data.add(new StatsCount(null, null, null, 1012L));
    data.add(new StatsCount(null, "1989", 1989, 790L));
    result = gb.generateChronoChartUrl(320, 160, data);
    // System.out.println(result);

    result = gb.generateChronoChartUrl(320, 160, new ArrayList<StatsCount>());
    // System.out.println(result);

    // String expectedString =
    // "http://chart.apis.google.com/chart?cht=t&chtm=world&chs=440x220&chld=DEUKFRARBRDARU&chco=FFFFFF,EDF0D4,13390D&chf=bg,s,E0F2FF&chd=e:DNCkD2bho9BSEf";
    // assertEquals(expectedString, result);
  }

  @Test
  public void testGetArea() {
    GeographicalArea a;
    a = GeographicalArea.valueOf("SOUTH_AMERICA");
    a = GeographicalArea.valueOf("south_america".toUpperCase());
    System.out.println(a);
  }

  @Test
  public void testMap() {
    GChartBuilder gb = new GChartBuilder();
    List<StatsCount> data = new ArrayList<StatsCount>();
    data.add(new StatsCount("DE", 1012L));
    data.add(new StatsCount("UK", 790L));
    data.add(new StatsCount("FR", 1291L));
    data.add(new StatsCount("AR", 8222L));
    data.add(new StatsCount("BR", 12122L));
    data.add(new StatsCount("DA", 522L));
    data.add(new StatsCount("RU", 1342L));
    data.add(new StatsCount("Belgium", 18842L));
    String expectedString = "http://chart.apis.google.com/chart?cht=t&chtm=world&chs=440x220&chld=DEUKFRARBRDARU&chco=FFFFFF,EDF0D4,13390D&chf=bg,s,E0F2FF&chd=e:DNCkD2bho9BSEf";
    String result = gb.generateMapChartUrl(440, 220, data);
    // System.out.println(result);
    assertEquals(expectedString, result);
  }

  @Test
  public void testPie() {
    GChartBuilder gb = new GChartBuilder();
    List<StatsCount> data = new ArrayList<StatsCount>();
    data.add(new StatsCount("Harald", 1012L));
    data.add(new StatsCount("Stefan", 79L));
    data.add(new StatsCount("Freerk", 891L));
    data.add(new StatsCount("Fritz", 2122L));
    data.add(new StatsCount("Bernd", 1422L));
    String expectedString = "http://chart.apis.google.com/chart?cht=p&chs=320x160&chl=Harald|Stefan|Freerk|Fritz|Bernd&chts=000000,16&chco=76A4FB,D7E9F5,18427D,80C65A,CA3D05&chd=e:LhApKPYUQA&chtt=Phonecalls+per+Friend";
    String result = gb.generatePieChartUrl(320, 160, "Phonecalls per Friend",
        data);
    // System.out.println(result);
    assertEquals(expectedString, result);

    expectedString = "http://chart.apis.google.com/chart?cht=p&chs=320x160&chl=Harald|Stefan|Freerk|Fritz|Bernd&chts=000000,16&chco=76A4FB,D7E9F5,18427D,80C65A,CA3D05&chd=e:LhApKPYUQA";
    result = gb.generatePiaChartUrl(320, 160, data);
    // System.out.println(result);
    assertEquals(expectedString, result);

    System.out.println(gb.generatePiaChartUrl(320, 160, null));
  }

}
