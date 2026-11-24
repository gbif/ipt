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
package org.gbif.ipt.action.manage;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.ExtensionPropertyTranslation;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("HttpUrlsUsage")
public class ExtensionActionTest {

  private static final String ROW_TYPE = "http://rs.tdwg.org/dwc/terms/ResourceRelationship";

  private Extension extension;
  private ExtensionManager extensionManager;

  @BeforeEach
  public void setup() {
    extension = new Extension();
    extension.setRowType(ROW_TYPE);
    extension.setTitle("Resource Relationship");
    extension.setSubject("Occurrence");
    // groups deliberately not in alphabetical order
    extension.addProperty(property(DwcTerm.resourceID, "Relationship",
        new ExtensionPropertyTranslation("fr", "Identifiant de la ressource", null, null, null)));
    extension.addProperty(property(DwcTerm.relatedResourceID, "Relationship"));
    extension.addProperty(property(DwcTerm.relationshipRemarks, "Remarks"));

    extensionManager = mock(ExtensionManager.class);
    when(extensionManager.get(ROW_TYPE)).thenReturn(extension);
    when(extensionManager.isSuitableFor(any(Extension.class), anyString()))
        .thenAnswer(i -> i.getArgument(1, String.class).endsWith("/Occurrence"));
  }

  @Test
  public void testNotFound() throws Exception {
    assertEquals(ExtensionAction.NOT_FOUND, action(null).execute());
    assertEquals(ExtensionAction.NOT_FOUND, action("http://example.org/Nope").execute());
  }

  @Test
  public void testPage() throws Exception {
    ExtensionAction action = action(ROW_TYPE);
    assertEquals("success", action.execute());
    assertEquals(extension, action.getExtension());

    assertEquals(List.of("Relationship", "Remarks"), new ArrayList<>(action.getPropertiesByGroup().keySet()));
    assertEquals(List.of("Occurrence"), action.getWorksWith().get(ROW_TYPE));

    assertEquals(List.of("fr"), new ArrayList<>(action.getLabelTranslations().keySet()));
    assertEquals(1, action.getLabelTranslations().get("fr"));
  }

  @Test
  public void testCoreTypeWorksWithNothing() throws Exception {
    Extension core = new Extension();
    core.setRowType("http://rs.tdwg.org/dwc/terms/Occurrence");
    core.setTitle("Darwin Core Occurrence");
    when(extensionManager.get(core.getRowType())).thenReturn(core);
    ExtensionAction action = action(core.getRowType());
    assertEquals("success", action.execute());
    assertNull(action.getWorksWith().get(core.getRowType()));
    assertTrue(action.getPropertiesByGroup().isEmpty());
  }

  private ExtensionAction action(String id) {
    ExtensionAction action = new ExtensionAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
        mock(RegistrationManager.class), extensionManager) {
      @Override
      public Locale getLocale() {
        return Locale.ENGLISH;
      }
    };
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("id")).thenReturn(id);
    action.withServletRequest(req);
    action.prepare();
    return action;
  }

  private static ExtensionProperty property(Term term, String group, ExtensionPropertyTranslation... translations) {
    ExtensionProperty p = new ExtensionProperty(term.qualifiedName());
    p.setGroup(group);
    for (ExtensionPropertyTranslation t : translations) {
      p.addTranslation(t);
    }
    return p;
  }
}
