/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.utils;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.action.portal.OrganizedTaxonomicKeywords;
import org.gbif.ipt.model.User;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ForbiddenClassException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XStreamSecurityTest {

  private final XStream xstream = XStreamSecurity.restrict(new XStream());

  @Test
  public void rejectsClassesOutsideAllowlist() {
    // harmless stand-ins for deserialization gadgets: any class not on the allowlist must be refused
    assertThrows(ForbiddenClassException.class, () -> xstream.fromXML("<java.util.Random/>"));
    assertThrows(ForbiddenClassException.class, () -> xstream.fromXML("<java.lang.ProcessBuilder/>"));
    assertThrows(ForbiddenClassException.class,
        () -> xstream.fromXML("<list><java.util.Random/></list>"));
  }

  @Test
  public void acceptsModelAndJdkTypes() throws Exception {
    User user = new User();
    user.setEmail("a@b.org");

    Map<String, Object> map = new HashMap<>();
    map.put("user", user);
    map.put("doi", new DOI("10.5072/abc"));
    map.put("decimal", new BigDecimal("1.1"));
    map.put("date", new Date(0));
    map.put("uuid", UUID.fromString("00000000-0000-0000-0000-000000000001"));
    map.put("uri", URI.create("https://www.gbif.org"));
    map.put("url", new URL("https://www.gbif.org"));
    map.put("role", User.Role.Manager);
    List<Object> list = new ArrayList<>(map.values());
    map.put("list", list);
    // inferred metadata keeps these in lists, so XStream checks them against the allowlist
    OrganizedTaxonomicKeywords keywords = new OrganizedTaxonomicKeywords();
    keywords.setRank("species");
    map.put("keywords", new ArrayList<>(List.of(keywords)));

    Object copy = xstream.fromXML(xstream.toXML(map));
    assertEquals(map.keySet(), ((Map<?, ?>) copy).keySet());
  }
}
