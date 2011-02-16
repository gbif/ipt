/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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
 ***************************************************************************/

package org.gbif.ipt.utils;

import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * @author Daniel
 * 
 */
public class CountryUtils{
  private final static BiMap<String, String> commonletterCountryCodes = HashBiMap.create();
  static {
	  commonletterCountryCodes.put("AF", "AFG");
	  commonletterCountryCodes.put("AX", "ALA");
	  commonletterCountryCodes.put("AL", "ALB");
	  commonletterCountryCodes.put("DZ", "DZA");
	  commonletterCountryCodes.put("AS", "ASM");
	  commonletterCountryCodes.put("AD", "AND");
	  commonletterCountryCodes.put("AO", "AGO");
	  commonletterCountryCodes.put("AI", "AIA");
	  commonletterCountryCodes.put("AQ", "ATA");
	  commonletterCountryCodes.put("AG", "ATG");
	  commonletterCountryCodes.put("AR", "ARG");
	  commonletterCountryCodes.put("AM", "ARM");
	  commonletterCountryCodes.put("AW", "ABW");
	  commonletterCountryCodes.put("AU", "AUS");
	  commonletterCountryCodes.put("AT", "AUT");
	  commonletterCountryCodes.put("AZ", "AZE");
	  commonletterCountryCodes.put("BS", "BHS");
	  commonletterCountryCodes.put("BH", "BHR");
	  commonletterCountryCodes.put("BD", "BGD");
	  commonletterCountryCodes.put("BB", "BRB");
	  commonletterCountryCodes.put("BY", "BLR");
	  commonletterCountryCodes.put("BE", "BEL");
	  commonletterCountryCodes.put("BZ", "BLZ");
	  commonletterCountryCodes.put("BJ", "BEN");
	  commonletterCountryCodes.put("BM", "BMU");
	  commonletterCountryCodes.put("BT", "BTN");
	  commonletterCountryCodes.put("BO", "BOL");
	  commonletterCountryCodes.put("BQ", "BES");
	  commonletterCountryCodes.put("BA", "BIH");
	  commonletterCountryCodes.put("BW", "BWA");
	  commonletterCountryCodes.put("BV", "BVT");
	  commonletterCountryCodes.put("BR", "BRA");
	  commonletterCountryCodes.put("IO", "IOT");
	  commonletterCountryCodes.put("BN", "BRN");
	  commonletterCountryCodes.put("BG", "BGR");
	  commonletterCountryCodes.put("BF", "BFA");
	  commonletterCountryCodes.put("BI", "BDI");
	  commonletterCountryCodes.put("KH", "KHM");
	  commonletterCountryCodes.put("CM", "CMR");
	  commonletterCountryCodes.put("CA", "CAN");
	  commonletterCountryCodes.put("CV", "CPV");
	  commonletterCountryCodes.put("KY", "CYM");
	  commonletterCountryCodes.put("CF", "CAF");
	  commonletterCountryCodes.put("TD", "TCD");
	  commonletterCountryCodes.put("CL", "CHL");
	  commonletterCountryCodes.put("CN", "CHN");
	  commonletterCountryCodes.put("CX", "CXR");
	  commonletterCountryCodes.put("CC", "CCK");
	  commonletterCountryCodes.put("CO", "COL");
	  commonletterCountryCodes.put("KM", "COM");
	  commonletterCountryCodes.put("CG", "COG");
	  commonletterCountryCodes.put("CD", "COD");
	  commonletterCountryCodes.put("CK", "COK");
	  commonletterCountryCodes.put("CR", "CRI");
	  commonletterCountryCodes.put("CI", "CIV");
	  commonletterCountryCodes.put("HR", "HRV");
	  commonletterCountryCodes.put("CU", "CUB");
	  commonletterCountryCodes.put("CW", "CUW");
	  commonletterCountryCodes.put("CY", "CYP");
	  commonletterCountryCodes.put("CZ", "CZE");
	  commonletterCountryCodes.put("DK", "DNK");
	  commonletterCountryCodes.put("DJ", "DJI");
	  commonletterCountryCodes.put("DM", "DMA");
	  commonletterCountryCodes.put("DO", "DOM");
	  commonletterCountryCodes.put("EC", "ECU");
	  commonletterCountryCodes.put("EG", "EGY");
	  commonletterCountryCodes.put("SV", "SLV");
	  commonletterCountryCodes.put("GQ", "GNQ");
	  commonletterCountryCodes.put("ER", "ERI");
	  commonletterCountryCodes.put("EE", "EST");
	  commonletterCountryCodes.put("ET", "ETH");
	  commonletterCountryCodes.put("FK", "FLK");
	  commonletterCountryCodes.put("FO", "FRO");
	  commonletterCountryCodes.put("FJ", "FJI");
	  commonletterCountryCodes.put("FI", "FIN");
	  commonletterCountryCodes.put("FR", "FRA");
	  commonletterCountryCodes.put("GF", "GUF");
	  commonletterCountryCodes.put("PF", "PYF");
	  commonletterCountryCodes.put("TF", "ATF");
	  commonletterCountryCodes.put("GA", "GAB");
	  commonletterCountryCodes.put("GM", "GMB");
	  commonletterCountryCodes.put("GE", "GEO");
	  commonletterCountryCodes.put("DE", "DEU");
	  commonletterCountryCodes.put("GH", "GHA");
	  commonletterCountryCodes.put("GI", "GIB");
	  commonletterCountryCodes.put("GR", "GRC");
	  commonletterCountryCodes.put("GL", "GRL");
	  commonletterCountryCodes.put("GD", "GRD");
	  commonletterCountryCodes.put("GP", "GLP");
	  commonletterCountryCodes.put("GU", "GUM");
	  commonletterCountryCodes.put("GT", "GTM");
	  commonletterCountryCodes.put("GG", "GGY");
	  commonletterCountryCodes.put("GN", "GIN");
	  commonletterCountryCodes.put("GW", "GNB");
	  commonletterCountryCodes.put("GY", "GUY");
	  commonletterCountryCodes.put("HT", "HTI");
	  commonletterCountryCodes.put("HM", "HMD");
	  commonletterCountryCodes.put("VA", "VAT");
	  commonletterCountryCodes.put("HN", "HND");
	  commonletterCountryCodes.put("HK", "HKG");
	  commonletterCountryCodes.put("HU", "HUN");
	  commonletterCountryCodes.put("IS", "ISL");
	  commonletterCountryCodes.put("IN", "IND");
	  commonletterCountryCodes.put("ID", "IDN");
	  commonletterCountryCodes.put("IR", "IRN");
	  commonletterCountryCodes.put("IQ", "IRQ");
	  commonletterCountryCodes.put("IE", "IRL");
	  commonletterCountryCodes.put("IM", "IMN");
	  commonletterCountryCodes.put("IL", "ISR");
	  commonletterCountryCodes.put("IT", "ITA");
	  commonletterCountryCodes.put("JM", "JAM");
	  commonletterCountryCodes.put("JP", "JPN");
	  commonletterCountryCodes.put("JE", "JEY");
	  commonletterCountryCodes.put("JO", "JOR");
	  commonletterCountryCodes.put("KZ", "KAZ");
	  commonletterCountryCodes.put("KE", "KEN");
	  commonletterCountryCodes.put("KI", "KIR");
	  commonletterCountryCodes.put("KP", "PRK");
	  commonletterCountryCodes.put("KR", "KOR");
	  commonletterCountryCodes.put("KW", "KWT");
	  commonletterCountryCodes.put("KG", "KGZ");
	  commonletterCountryCodes.put("LA", "LAO");
	  commonletterCountryCodes.put("LV", "LVA");
	  commonletterCountryCodes.put("LB", "LBN");
	  commonletterCountryCodes.put("LS", "LSO");
	  commonletterCountryCodes.put("LR", "LBR");
	  commonletterCountryCodes.put("LY", "LBY");
	  commonletterCountryCodes.put("LI", "LIE");
	  commonletterCountryCodes.put("LT", "LTU");
	  commonletterCountryCodes.put("LU", "LUX");
	  commonletterCountryCodes.put("MO", "MAC");
	  commonletterCountryCodes.put("MK", "MKD");
	  commonletterCountryCodes.put("MG", "MDG");
	  commonletterCountryCodes.put("MW", "MWI");
	  commonletterCountryCodes.put("MY", "MYS");
	  commonletterCountryCodes.put("MV", "MDV");
	  commonletterCountryCodes.put("ML", "MLI");
	  commonletterCountryCodes.put("MT", "MLT");
	  commonletterCountryCodes.put("MH", "MHL");
	  commonletterCountryCodes.put("MQ", "MTQ");
	  commonletterCountryCodes.put("MR", "MRT");
	  commonletterCountryCodes.put("MU", "MUS");
	  commonletterCountryCodes.put("YT", "MYT");
	  commonletterCountryCodes.put("MX", "MEX");
	  commonletterCountryCodes.put("FM", "FSM");
	  commonletterCountryCodes.put("MD", "MDA");
	  commonletterCountryCodes.put("MC", "MCO");
	  commonletterCountryCodes.put("MN", "MNG");
	  commonletterCountryCodes.put("ME", "MNE");
	  commonletterCountryCodes.put("MS", "MSR");
	  commonletterCountryCodes.put("MA", "MAR");
	  commonletterCountryCodes.put("MZ", "MOZ");
	  commonletterCountryCodes.put("MM", "MMR");
	  commonletterCountryCodes.put("NA", "NAM");
	  commonletterCountryCodes.put("NR", "NRU");
	  commonletterCountryCodes.put("NP", "NPL");
	  commonletterCountryCodes.put("NL", "NLD");
	  commonletterCountryCodes.put("NC", "NCL");
	  commonletterCountryCodes.put("NZ", "NZL");
	  commonletterCountryCodes.put("NI", "NIC");
	  commonletterCountryCodes.put("NE", "NER");
	  commonletterCountryCodes.put("NG", "NGA");
	  commonletterCountryCodes.put("NU", "NIU");
	  commonletterCountryCodes.put("NF", "NFK");
	  commonletterCountryCodes.put("MP", "MNP");
	  commonletterCountryCodes.put("NO", "NOR");
	  commonletterCountryCodes.put("OM", "OMN");
	  commonletterCountryCodes.put("PK", "PAK");
	  commonletterCountryCodes.put("PW", "PLW");
	  commonletterCountryCodes.put("PS", "PSE");
	  commonletterCountryCodes.put("PA", "PAN");
	  commonletterCountryCodes.put("PG", "PNG");
	  commonletterCountryCodes.put("PY", "PRY");
	  commonletterCountryCodes.put("PE", "PER");
	  commonletterCountryCodes.put("PH", "PHL");
	  commonletterCountryCodes.put("PN", "PCN");
	  commonletterCountryCodes.put("PL", "POL");
	  commonletterCountryCodes.put("PT", "PRT");
	  commonletterCountryCodes.put("PR", "PRI");
	  commonletterCountryCodes.put("QA", "QAT");
	  commonletterCountryCodes.put("RE", "REU");
	  commonletterCountryCodes.put("RO", "ROU");
	  commonletterCountryCodes.put("RU", "RUS");
	  commonletterCountryCodes.put("RW", "RWA");
	  commonletterCountryCodes.put("BL", "BLM");
	  commonletterCountryCodes.put("SH", "SHN");
	  commonletterCountryCodes.put("KN", "KNA");
	  commonletterCountryCodes.put("LC", "LCA");
	  commonletterCountryCodes.put("MF", "MAF");
	  commonletterCountryCodes.put("PM", "SPM");
	  commonletterCountryCodes.put("VC", "VCT");
	  commonletterCountryCodes.put("WS", "WSM");
	  commonletterCountryCodes.put("SM", "SMR");
	  commonletterCountryCodes.put("ST", "STP");
	  commonletterCountryCodes.put("SA", "SAU");
	  commonletterCountryCodes.put("SN", "SEN");
	  commonletterCountryCodes.put("RS", "SRB");
	  commonletterCountryCodes.put("SC", "SYC");
	  commonletterCountryCodes.put("SL", "SLE");
	  commonletterCountryCodes.put("SG", "SGP");
	  commonletterCountryCodes.put("SX", "SXM");
	  commonletterCountryCodes.put("SK", "SVK");
	  commonletterCountryCodes.put("SI", "SVN");
	  commonletterCountryCodes.put("SB", "SLB");
	  commonletterCountryCodes.put("SO", "SOM");
	  commonletterCountryCodes.put("ZA", "ZAF");
	  commonletterCountryCodes.put("GS", "SGS");
	  commonletterCountryCodes.put("ES", "ESP");
	  commonletterCountryCodes.put("LK", "LKA");
	  commonletterCountryCodes.put("SD", "SDN");
	  commonletterCountryCodes.put("SR", "SUR");
	  commonletterCountryCodes.put("SJ", "SJM");
	  commonletterCountryCodes.put("SZ", "SWZ");
	  commonletterCountryCodes.put("SE", "SWE");
	  commonletterCountryCodes.put("CH", "CHE");
	  commonletterCountryCodes.put("SY", "SYR");
	  commonletterCountryCodes.put("TW", "TWN");
	  commonletterCountryCodes.put("TJ", "TJK");
	  commonletterCountryCodes.put("TZ", "TZA");
	  commonletterCountryCodes.put("TH", "THA");
	  commonletterCountryCodes.put("TL", "TLS");
	  commonletterCountryCodes.put("TG", "TGO");
	  commonletterCountryCodes.put("TK", "TKL");
	  commonletterCountryCodes.put("TO", "TON");
	  commonletterCountryCodes.put("TT", "TTO");
	  commonletterCountryCodes.put("TN", "TUN");
	  commonletterCountryCodes.put("TR", "TUR");
	  commonletterCountryCodes.put("TM", "TKM");
	  commonletterCountryCodes.put("TC", "TCA");
	  commonletterCountryCodes.put("TV", "TUV");
	  commonletterCountryCodes.put("UG", "UGA");
	  commonletterCountryCodes.put("UA", "UKR");
	  commonletterCountryCodes.put("AE", "ARE");
	  commonletterCountryCodes.put("GB", "GBR");
	  commonletterCountryCodes.put("US", "USA");
	  commonletterCountryCodes.put("UM", "UMI");
	  commonletterCountryCodes.put("UY", "URY");
	  commonletterCountryCodes.put("UZ", "UZB");
	  commonletterCountryCodes.put("VU", "VUT");
	  commonletterCountryCodes.put("VE", "VEN");
	  commonletterCountryCodes.put("VN", "VNM");
	  commonletterCountryCodes.put("VG", "VGB");
	  commonletterCountryCodes.put("VI", "VIR");
	  commonletterCountryCodes.put("WF", "WLF");
	  commonletterCountryCodes.put("EH", "ESH");
	  commonletterCountryCodes.put("YE", "YEM");
	  commonletterCountryCodes.put("ZM", "ZMB");
	  commonletterCountryCodes.put("ZW", "ZWE");
  }
  
  public static String iso2(String country) {
    if (country != null && country.length() == 2) {
      return country;
    } else if (country != null && country.length() == 3) {
      return commonletterCountryCodes.inverse().get(country.toUpperCase());
    }
    return null;
  }
  
  public static String iso3(String country) {
	    if (country != null && country.length() == 3) {
	      return country;
	    } else if (country != null && country.length() == 2) {
	      return commonletterCountryCodes.get(country.toUpperCase());
	    }
	    return null;
	  }

}


