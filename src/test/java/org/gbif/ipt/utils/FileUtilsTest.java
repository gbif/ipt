package org.gbif.ipt.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileUtilsTest {

  private FileUtils fileUtils;

  @Test
  public void testIsExcelFileType() {
    // excel types
    String excelFileName = "resource.xls";
    assertTrue(fileUtils.isExcelFile(excelFileName));
    excelFileName = "resource.xlsx";
    assertTrue(fileUtils.isExcelFile(excelFileName));
    // non-excel types
    excelFileName = "xlsx.zip";
    assertFalse(fileUtils.isExcelFile(excelFileName));
    excelFileName = "resource_xls.xml";
    assertFalse(fileUtils.isExcelFile(excelFileName));
  }
}
