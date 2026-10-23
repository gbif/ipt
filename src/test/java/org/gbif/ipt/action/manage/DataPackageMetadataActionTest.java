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

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessContributor;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessLicense;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessSource;
import org.gbif.ipt.model.datapackage.metadata.col.FrictionlessColMetadata;
import org.gbif.ipt.model.voc.FrictionlessMetadataSection;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.DataPackageMetadataValidator;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts2.ActionSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Frictionless data package metadata (e.g. ColDP).
 */
public class DataPackageMetadataActionTest {

  private static final String SHORTNAME = "coldp";

  private ResourceManager resourceManager;
  private DataPackageMetadataValidator validator;
  private HttpServletRequest req;
  private DataPackageMetadataAction action;
  private Resource resource;
  private FrictionlessColMetadata metadata;

  @BeforeEach
  public void setUp() {
    metadata = new FrictionlessColMetadata();
    metadata.getContributors().add(new FrictionlessContributor());
    metadata.getLicenses().add(new FrictionlessLicense());
    metadata.getSources().add(new FrictionlessSource());
    resource = new Resource();
    resource.setShortname(SHORTNAME);
    resource.setCoreType(Constants.COL_DP);
    resource.setDataPackageIdentifier(Constants.DATA_PACKAGE_COLDP_IDENTIFIER);
    resource.setDataPackageMetadata(metadata);

    resourceManager = mock(ResourceManager.class);
    when(resourceManager.get(SHORTNAME)).thenReturn(resource);
    validator = mock(DataPackageMetadataValidator.class);

    req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/ipt/manage/datapackage-metadata-basic.do");
    when(req.getParameter(Constants.REQ_PARAM_RESOURCE)).thenReturn(SHORTNAME);

    action = new DataPackageMetadataAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
        mock(RegistrationManager.class), resourceManager, validator);
    action.withServletRequest(req);
    Map<String, Object> session = new HashMap<>();
    session.put(Constants.SESSION_USER, new User());
    action.withSession(session);
  }

  @DisplayName("Posting the form replaces contributors, licenses and sources")
  @Test
  public void testPrepareOnPost() {
    when(req.getMethod()).thenReturn("POST");

    action.prepare();

    assertAll(
        () -> assertSame(metadata, action.getMetadata()),
        () -> assertEquals(FrictionlessMetadataSection.BASIC_SECTION.getName(), action.getSection()),
        () -> assertTrue(metadata.getContributors().isEmpty()),
        () -> assertTrue(metadata.getLicenses().isEmpty()),
        () -> assertTrue(metadata.getSources().isEmpty()));
  }

  @DisplayName("Opening the page keeps contributors, licenses and sources")
  @Test
  public void testPrepareOnGet() {
    when(req.getMethod()).thenReturn("GET");

    action.prepare();

    assertAll(
        () -> assertEquals(1, metadata.getContributors().size()),
        () -> assertEquals(1, metadata.getLicenses().size()),
        () -> assertEquals(1, metadata.getSources().size()));
  }

  @DisplayName("Valid metadata is saved")
  @Test
  public void testSaveValid() throws Exception {
    when(req.getMethod()).thenReturn("POST");
    when(validator.isSectionValid(eq(action), eq(resource), eq(FrictionlessMetadataSection.BASIC_SECTION))).thenReturn(true);
    action.prepare();

    assertEquals(ActionSupport.SUCCESS, action.save());

    verify(resourceManager).saveDatapackageMetadata(resource);
    verify(resourceManager).save(resource);
    assertNotNull(resource.getMetadataModified());
  }

  @DisplayName("Invalid metadata is not saved")
  @Test
  public void testSaveInvalid() throws Exception {
    when(req.getMethod()).thenReturn("POST");
    when(validator.isSectionValid(any(), any(), any())).thenReturn(false);
    action.prepare();

    assertEquals(ActionSupport.INPUT, action.save());

    verify(resourceManager, never()).saveDatapackageMetadata(any());
    verify(resourceManager, never()).save(any());
  }
}
