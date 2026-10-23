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
import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.service.manage.impl.InferredMetadataManagerImpl;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.DataPackageMetadataValidator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * When opening a Camtrap DP metadata page infers the metadata from the source data.
 */
public class CamtrapMetadataActionInferringTest {

  private static final String SHORTNAME = "camtrap";

  private ResourceManager resourceManager;
  private ResourceMetadataInferringService inferringService;
  private CamtrapMetadataAction action;
  private Resource resource;
  private InferredCamtrapMetadata inferred;

  @BeforeEach
  public void setUp() {
    resource = new Resource();
    resource.setShortname(SHORTNAME);
    resource.setCoreType(Constants.CAMTRAP_DP);
    resource.setDataPackageIdentifier("http://rs.gbif.org/data-packages/camtrap-dp");
    resource.setDataPackageMetadata(new CamtrapMetadata());

    resourceManager = mock(ResourceManager.class);
    when(resourceManager.get(SHORTNAME)).thenReturn(resource);
    inferringService = mock(ResourceMetadataInferringService.class);
    inferred = new InferredCamtrapMetadata();
    inferred.setLastModified(new Date());
    when(inferringService.inferMetadata(any(Resource.class))).thenReturn(inferred);
    // a small dataset: inferred right away
    when(inferringService.countRowsToRead(any(Resource.class))).thenReturn(OptionalLong.of(0));

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/ipt/manage/camtrap-metadata-geographic.do");
    when(req.getParameter(Constants.REQ_PARAM_RESOURCE)).thenReturn(SHORTNAME);
    when(req.getMethod()).thenReturn("GET");

    action = new CamtrapMetadataAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
        mock(RegistrationManager.class), resourceManager, mock(DataPackageMetadataValidator.class),
        new InferredMetadataManagerImpl(inferringService, resourceManager, Runnable::run), mock(DataPackageSchemaManager.class));
    action.withServletRequest(req);
    Map<String, Object> session = new HashMap<>();
    session.put(Constants.SESSION_USER, new User());
    action.withSession(session);
  }

  @DisplayName("EML default of a new resource: inferred and stored as Camtrap DP metadata")
  @Test
  public void testWrongType() {
    InferredEmlMetadata eml = new InferredEmlMetadata();
    eml.setLastModified(new Date());
    resource.setInferredMetadata(eml);

    action.prepare();

    verify(inferringService).inferMetadata(resource);
    verify(resourceManager).saveInferredMetadata(resource);
    assertSame(inferred, resource.getInferredMetadata());
  }

  @DisplayName("Never inferred: inferred and saved")
  @Test
  public void testNeverInferred() {
    resource.setInferredMetadata(new InferredCamtrapMetadata());

    action.prepare();

    verify(inferringService).inferMetadata(resource);
    assertSame(inferred, resource.getInferredMetadata());
  }

  @DisplayName("Inferring returns nothing: empty Camtrap DP inferred metadata, no error")
  @Test
  public void testInferringReturnsNothing() {
    when(inferringService.inferMetadata(any(Resource.class))).thenReturn(null);

    action.prepare();

    assertInstanceOf(InferredCamtrapMetadata.class, resource.getInferredMetadata());
  }

  @DisplayName("Up to date: not inferred again")
  @Test
  public void testUpToDate() {
    InferredCamtrapMetadata stored = new InferredCamtrapMetadata();
    stored.setLastModified(new Date(2_000_000L));
    resource.setInferredMetadata(stored);
    resource.setMappingsModified(new Date(1_000_000L));

    action.prepare();

    verify(inferringService, never()).inferMetadata(any());
    assertSame(stored, resource.getInferredMetadata());
  }
}
