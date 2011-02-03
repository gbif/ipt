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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;

/**
 * @author markus
 * 
 */
public class HttpUtilTest {
  @Test
  public void testConditionalGet() throws ParseException, IOException {
    DefaultHttpClient client = new DefaultHttpClient();
    HttpUtil util = new HttpUtil(client);
    Date last = HttpUtil.DATE_FORMAT_RFC2616.parse("Wed, 03 Aug 2009 22:37:31 GMT");
    Date current = HttpUtil.DATE_FORMAT_RFC2616.parse("Wed, 04 Aug 2010 8:14:57 GMT");

    File tmp = File.createTempFile("vocab", ".xml");
    URL url = new URL("http://rs.gbif.org/vocabulary/gbif/resource_type.xml");
    boolean downloaded = util.downloadIfChanged(url, last, tmp);
    assertTrue(downloaded);

    downloaded = util.downloadIfChanged(url, current, tmp);
    assertFalse(downloaded);
  }
}
