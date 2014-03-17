/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class CountryUtils {

  private static final BiMap<String, String> COMMON_COUNTRY_CODES = HashBiMap.create();

  static {
    COMMON_COUNTRY_CODES.put("AF", "AFG");
    COMMON_COUNTRY_CODES.put("AX", "ALA");
    COMMON_COUNTRY_CODES.put("AL", "ALB");
    COMMON_COUNTRY_CODES.put("DZ", "DZA");
    COMMON_COUNTRY_CODES.put("AS", "ASM");
    COMMON_COUNTRY_CODES.put("AD", "AND");
    COMMON_COUNTRY_CODES.put("AO", "AGO");
    COMMON_COUNTRY_CODES.put("AI", "AIA");
    COMMON_COUNTRY_CODES.put("AQ", "ATA");
    COMMON_COUNTRY_CODES.put("AG", "ATG");
    COMMON_COUNTRY_CODES.put("AR", "ARG");
    COMMON_COUNTRY_CODES.put("AM", "ARM");
    COMMON_COUNTRY_CODES.put("AW", "ABW");
    COMMON_COUNTRY_CODES.put("AU", "AUS");
    COMMON_COUNTRY_CODES.put("AT", "AUT");
    COMMON_COUNTRY_CODES.put("AZ", "AZE");
    COMMON_COUNTRY_CODES.put("BS", "BHS");
    COMMON_COUNTRY_CODES.put("BH", "BHR");
    COMMON_COUNTRY_CODES.put("BD", "BGD");
    COMMON_COUNTRY_CODES.put("BB", "BRB");
    COMMON_COUNTRY_CODES.put("BY", "BLR");
    COMMON_COUNTRY_CODES.put("BE", "BEL");
    COMMON_COUNTRY_CODES.put("BZ", "BLZ");
    COMMON_COUNTRY_CODES.put("BJ", "BEN");
    COMMON_COUNTRY_CODES.put("BM", "BMU");
    COMMON_COUNTRY_CODES.put("BT", "BTN");
    COMMON_COUNTRY_CODES.put("BO", "BOL");
    COMMON_COUNTRY_CODES.put("BQ", "BES");
    COMMON_COUNTRY_CODES.put("BA", "BIH");
    COMMON_COUNTRY_CODES.put("BW", "BWA");
    COMMON_COUNTRY_CODES.put("BV", "BVT");
    COMMON_COUNTRY_CODES.put("BR", "BRA");
    COMMON_COUNTRY_CODES.put("IO", "IOT");
    COMMON_COUNTRY_CODES.put("BN", "BRN");
    COMMON_COUNTRY_CODES.put("BG", "BGR");
    COMMON_COUNTRY_CODES.put("BF", "BFA");
    COMMON_COUNTRY_CODES.put("BI", "BDI");
    COMMON_COUNTRY_CODES.put("KH", "KHM");
    COMMON_COUNTRY_CODES.put("CM", "CMR");
    COMMON_COUNTRY_CODES.put("CA", "CAN");
    COMMON_COUNTRY_CODES.put("CV", "CPV");
    COMMON_COUNTRY_CODES.put("KY", "CYM");
    COMMON_COUNTRY_CODES.put("CF", "CAF");
    COMMON_COUNTRY_CODES.put("TD", "TCD");
    COMMON_COUNTRY_CODES.put("CL", "CHL");
    COMMON_COUNTRY_CODES.put("CN", "CHN");
    COMMON_COUNTRY_CODES.put("CX", "CXR");
    COMMON_COUNTRY_CODES.put("CC", "CCK");
    COMMON_COUNTRY_CODES.put("CO", "COL");
    COMMON_COUNTRY_CODES.put("KM", "COM");
    COMMON_COUNTRY_CODES.put("CG", "COG");
    COMMON_COUNTRY_CODES.put("CD", "COD");
    COMMON_COUNTRY_CODES.put("CK", "COK");
    COMMON_COUNTRY_CODES.put("CR", "CRI");
    COMMON_COUNTRY_CODES.put("CI", "CIV");
    COMMON_COUNTRY_CODES.put("HR", "HRV");
    COMMON_COUNTRY_CODES.put("CU", "CUB");
    COMMON_COUNTRY_CODES.put("CW", "CUW");
    COMMON_COUNTRY_CODES.put("CY", "CYP");
    COMMON_COUNTRY_CODES.put("CZ", "CZE");
    COMMON_COUNTRY_CODES.put("DK", "DNK");
    COMMON_COUNTRY_CODES.put("DJ", "DJI");
    COMMON_COUNTRY_CODES.put("DM", "DMA");
    COMMON_COUNTRY_CODES.put("DO", "DOM");
    COMMON_COUNTRY_CODES.put("EC", "ECU");
    COMMON_COUNTRY_CODES.put("EG", "EGY");
    COMMON_COUNTRY_CODES.put("SV", "SLV");
    COMMON_COUNTRY_CODES.put("GQ", "GNQ");
    COMMON_COUNTRY_CODES.put("ER", "ERI");
    COMMON_COUNTRY_CODES.put("EE", "EST");
    COMMON_COUNTRY_CODES.put("ET", "ETH");
    COMMON_COUNTRY_CODES.put("FK", "FLK");
    COMMON_COUNTRY_CODES.put("FO", "FRO");
    COMMON_COUNTRY_CODES.put("FJ", "FJI");
    COMMON_COUNTRY_CODES.put("FI", "FIN");
    COMMON_COUNTRY_CODES.put("FR", "FRA");
    COMMON_COUNTRY_CODES.put("GF", "GUF");
    COMMON_COUNTRY_CODES.put("PF", "PYF");
    COMMON_COUNTRY_CODES.put("TF", "ATF");
    COMMON_COUNTRY_CODES.put("GA", "GAB");
    COMMON_COUNTRY_CODES.put("GM", "GMB");
    COMMON_COUNTRY_CODES.put("GE", "GEO");
    COMMON_COUNTRY_CODES.put("DE", "DEU");
    COMMON_COUNTRY_CODES.put("GH", "GHA");
    COMMON_COUNTRY_CODES.put("GI", "GIB");
    COMMON_COUNTRY_CODES.put("GR", "GRC");
    COMMON_COUNTRY_CODES.put("GL", "GRL");
    COMMON_COUNTRY_CODES.put("GD", "GRD");
    COMMON_COUNTRY_CODES.put("GP", "GLP");
    COMMON_COUNTRY_CODES.put("GU", "GUM");
    COMMON_COUNTRY_CODES.put("GT", "GTM");
    COMMON_COUNTRY_CODES.put("GG", "GGY");
    COMMON_COUNTRY_CODES.put("GN", "GIN");
    COMMON_COUNTRY_CODES.put("GW", "GNB");
    COMMON_COUNTRY_CODES.put("GY", "GUY");
    COMMON_COUNTRY_CODES.put("HT", "HTI");
    COMMON_COUNTRY_CODES.put("HM", "HMD");
    COMMON_COUNTRY_CODES.put("VA", "VAT");
    COMMON_COUNTRY_CODES.put("HN", "HND");
    COMMON_COUNTRY_CODES.put("HK", "HKG");
    COMMON_COUNTRY_CODES.put("HU", "HUN");
    COMMON_COUNTRY_CODES.put("IS", "ISL");
    COMMON_COUNTRY_CODES.put("IN", "IND");
    COMMON_COUNTRY_CODES.put("ID", "IDN");
    COMMON_COUNTRY_CODES.put("IR", "IRN");
    COMMON_COUNTRY_CODES.put("IQ", "IRQ");
    COMMON_COUNTRY_CODES.put("IE", "IRL");
    COMMON_COUNTRY_CODES.put("IM", "IMN");
    COMMON_COUNTRY_CODES.put("IL", "ISR");
    COMMON_COUNTRY_CODES.put("IT", "ITA");
    COMMON_COUNTRY_CODES.put("JM", "JAM");
    COMMON_COUNTRY_CODES.put("JP", "JPN");
    COMMON_COUNTRY_CODES.put("JE", "JEY");
    COMMON_COUNTRY_CODES.put("JO", "JOR");
    COMMON_COUNTRY_CODES.put("KZ", "KAZ");
    COMMON_COUNTRY_CODES.put("KE", "KEN");
    COMMON_COUNTRY_CODES.put("KI", "KIR");
    COMMON_COUNTRY_CODES.put("KP", "PRK");
    COMMON_COUNTRY_CODES.put("KR", "KOR");
    COMMON_COUNTRY_CODES.put("KW", "KWT");
    COMMON_COUNTRY_CODES.put("KG", "KGZ");
    COMMON_COUNTRY_CODES.put("LA", "LAO");
    COMMON_COUNTRY_CODES.put("LV", "LVA");
    COMMON_COUNTRY_CODES.put("LB", "LBN");
    COMMON_COUNTRY_CODES.put("LS", "LSO");
    COMMON_COUNTRY_CODES.put("LR", "LBR");
    COMMON_COUNTRY_CODES.put("LY", "LBY");
    COMMON_COUNTRY_CODES.put("LI", "LIE");
    COMMON_COUNTRY_CODES.put("LT", "LTU");
    COMMON_COUNTRY_CODES.put("LU", "LUX");
    COMMON_COUNTRY_CODES.put("MO", "MAC");
    COMMON_COUNTRY_CODES.put("MK", "MKD");
    COMMON_COUNTRY_CODES.put("MG", "MDG");
    COMMON_COUNTRY_CODES.put("MW", "MWI");
    COMMON_COUNTRY_CODES.put("MY", "MYS");
    COMMON_COUNTRY_CODES.put("MV", "MDV");
    COMMON_COUNTRY_CODES.put("ML", "MLI");
    COMMON_COUNTRY_CODES.put("MT", "MLT");
    COMMON_COUNTRY_CODES.put("MH", "MHL");
    COMMON_COUNTRY_CODES.put("MQ", "MTQ");
    COMMON_COUNTRY_CODES.put("MR", "MRT");
    COMMON_COUNTRY_CODES.put("MU", "MUS");
    COMMON_COUNTRY_CODES.put("YT", "MYT");
    COMMON_COUNTRY_CODES.put("MX", "MEX");
    COMMON_COUNTRY_CODES.put("FM", "FSM");
    COMMON_COUNTRY_CODES.put("MD", "MDA");
    COMMON_COUNTRY_CODES.put("MC", "MCO");
    COMMON_COUNTRY_CODES.put("MN", "MNG");
    COMMON_COUNTRY_CODES.put("ME", "MNE");
    COMMON_COUNTRY_CODES.put("MS", "MSR");
    COMMON_COUNTRY_CODES.put("MA", "MAR");
    COMMON_COUNTRY_CODES.put("MZ", "MOZ");
    COMMON_COUNTRY_CODES.put("MM", "MMR");
    COMMON_COUNTRY_CODES.put("NA", "NAM");
    COMMON_COUNTRY_CODES.put("NR", "NRU");
    COMMON_COUNTRY_CODES.put("NP", "NPL");
    COMMON_COUNTRY_CODES.put("NL", "NLD");
    COMMON_COUNTRY_CODES.put("NC", "NCL");
    COMMON_COUNTRY_CODES.put("NZ", "NZL");
    COMMON_COUNTRY_CODES.put("NI", "NIC");
    COMMON_COUNTRY_CODES.put("NE", "NER");
    COMMON_COUNTRY_CODES.put("NG", "NGA");
    COMMON_COUNTRY_CODES.put("NU", "NIU");
    COMMON_COUNTRY_CODES.put("NF", "NFK");
    COMMON_COUNTRY_CODES.put("MP", "MNP");
    COMMON_COUNTRY_CODES.put("NO", "NOR");
    COMMON_COUNTRY_CODES.put("OM", "OMN");
    COMMON_COUNTRY_CODES.put("PK", "PAK");
    COMMON_COUNTRY_CODES.put("PW", "PLW");
    COMMON_COUNTRY_CODES.put("PS", "PSE");
    COMMON_COUNTRY_CODES.put("PA", "PAN");
    COMMON_COUNTRY_CODES.put("PG", "PNG");
    COMMON_COUNTRY_CODES.put("PY", "PRY");
    COMMON_COUNTRY_CODES.put("PE", "PER");
    COMMON_COUNTRY_CODES.put("PH", "PHL");
    COMMON_COUNTRY_CODES.put("PN", "PCN");
    COMMON_COUNTRY_CODES.put("PL", "POL");
    COMMON_COUNTRY_CODES.put("PT", "PRT");
    COMMON_COUNTRY_CODES.put("PR", "PRI");
    COMMON_COUNTRY_CODES.put("QA", "QAT");
    COMMON_COUNTRY_CODES.put("RE", "REU");
    COMMON_COUNTRY_CODES.put("RO", "ROU");
    COMMON_COUNTRY_CODES.put("RU", "RUS");
    COMMON_COUNTRY_CODES.put("RW", "RWA");
    COMMON_COUNTRY_CODES.put("BL", "BLM");
    COMMON_COUNTRY_CODES.put("SH", "SHN");
    COMMON_COUNTRY_CODES.put("KN", "KNA");
    COMMON_COUNTRY_CODES.put("LC", "LCA");
    COMMON_COUNTRY_CODES.put("MF", "MAF");
    COMMON_COUNTRY_CODES.put("PM", "SPM");
    COMMON_COUNTRY_CODES.put("VC", "VCT");
    COMMON_COUNTRY_CODES.put("WS", "WSM");
    COMMON_COUNTRY_CODES.put("SM", "SMR");
    COMMON_COUNTRY_CODES.put("ST", "STP");
    COMMON_COUNTRY_CODES.put("SA", "SAU");
    COMMON_COUNTRY_CODES.put("SN", "SEN");
    COMMON_COUNTRY_CODES.put("RS", "SRB");
    COMMON_COUNTRY_CODES.put("SC", "SYC");
    COMMON_COUNTRY_CODES.put("SL", "SLE");
    COMMON_COUNTRY_CODES.put("SG", "SGP");
    COMMON_COUNTRY_CODES.put("SX", "SXM");
    COMMON_COUNTRY_CODES.put("SK", "SVK");
    COMMON_COUNTRY_CODES.put("SI", "SVN");
    COMMON_COUNTRY_CODES.put("SB", "SLB");
    COMMON_COUNTRY_CODES.put("SO", "SOM");
    COMMON_COUNTRY_CODES.put("ZA", "ZAF");
    COMMON_COUNTRY_CODES.put("GS", "SGS");
    COMMON_COUNTRY_CODES.put("ES", "ESP");
    COMMON_COUNTRY_CODES.put("LK", "LKA");
    COMMON_COUNTRY_CODES.put("SD", "SDN");
    COMMON_COUNTRY_CODES.put("SR", "SUR");
    COMMON_COUNTRY_CODES.put("SJ", "SJM");
    COMMON_COUNTRY_CODES.put("SZ", "SWZ");
    COMMON_COUNTRY_CODES.put("SE", "SWE");
    COMMON_COUNTRY_CODES.put("CH", "CHE");
    COMMON_COUNTRY_CODES.put("SY", "SYR");
    COMMON_COUNTRY_CODES.put("TW", "TWN");
    COMMON_COUNTRY_CODES.put("TJ", "TJK");
    COMMON_COUNTRY_CODES.put("TZ", "TZA");
    COMMON_COUNTRY_CODES.put("TH", "THA");
    COMMON_COUNTRY_CODES.put("TL", "TLS");
    COMMON_COUNTRY_CODES.put("TG", "TGO");
    COMMON_COUNTRY_CODES.put("TK", "TKL");
    COMMON_COUNTRY_CODES.put("TO", "TON");
    COMMON_COUNTRY_CODES.put("TT", "TTO");
    COMMON_COUNTRY_CODES.put("TN", "TUN");
    COMMON_COUNTRY_CODES.put("TR", "TUR");
    COMMON_COUNTRY_CODES.put("TM", "TKM");
    COMMON_COUNTRY_CODES.put("TC", "TCA");
    COMMON_COUNTRY_CODES.put("TV", "TUV");
    COMMON_COUNTRY_CODES.put("UG", "UGA");
    COMMON_COUNTRY_CODES.put("UA", "UKR");
    COMMON_COUNTRY_CODES.put("AE", "ARE");
    COMMON_COUNTRY_CODES.put("GB", "GBR");
    COMMON_COUNTRY_CODES.put("US", "USA");
    COMMON_COUNTRY_CODES.put("UM", "UMI");
    COMMON_COUNTRY_CODES.put("UY", "URY");
    COMMON_COUNTRY_CODES.put("UZ", "UZB");
    COMMON_COUNTRY_CODES.put("VU", "VUT");
    COMMON_COUNTRY_CODES.put("VE", "VEN");
    COMMON_COUNTRY_CODES.put("VN", "VNM");
    COMMON_COUNTRY_CODES.put("VG", "VGB");
    COMMON_COUNTRY_CODES.put("VI", "VIR");
    COMMON_COUNTRY_CODES.put("WF", "WLF");
    COMMON_COUNTRY_CODES.put("EH", "ESH");
    COMMON_COUNTRY_CODES.put("YE", "YEM");
    COMMON_COUNTRY_CODES.put("ZM", "ZMB");
    COMMON_COUNTRY_CODES.put("ZW", "ZWE");
  }

  public static String iso2(String country) {
    if (country != null && country.length() == 2) {
      return country;
    } else if (country != null && country.length() == 3) {
      return COMMON_COUNTRY_CODES.inverse().get(country.toUpperCase());
    }
    return null;
  }

  public static String iso3(String country) {
    if (country != null && country.length() == 3) {
      return country;
    } else if (country != null && country.length() == 2) {
      return COMMON_COUNTRY_CODES.get(country.toUpperCase());
    }
    return null;
  }

}
