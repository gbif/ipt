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
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * When opening an EML metadata page infers the metadata from the source data.
 */
public class MetadataActionInferringTest {

  private static final String SHORTNAME = "res";

  private ResourceManager resourceManager;
  private ResourceMetadataInferringService inferringService;
  private HttpServletRequest req;
  private MetadataAction action;
  private Resource resource;
  private InferredEmlMetadata inferred;

  @BeforeEach
  public void setUp() {
    resource = new Resource();
    resource.setShortname(SHORTNAME);
    resource.setCoreType("occurrence");

    resourceManager = mock(ResourceManager.class);
    when(resourceManager.get(SHORTNAME)).thenReturn(resource);
    inferringService = mock(ResourceMetadataInferringService.class);
    inferred = new InferredEmlMetadata();
    inferred.setLastModified(new Date());
    when(inferringService.inferMetadata(any(Resource.class))).thenReturn(inferred);

    req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/ipt/manage/metadata-geocoverage.do");
    when(req.getParameter(Constants.REQ_PARAM_RESOURCE)).thenReturn(SHORTNAME);
    when(req.getMethod()).thenReturn("GET");

    action = new MetadataAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
        resourceManager, mock(VocabulariesManager.class), inferringService, mock(ConfigWarnings.class), mock(DataDir.class));
    action.withServletRequest(req);
    Map<String, Object> session = new HashMap<>();
    session.put(Constants.SESSION_USER, new User());
    action.withSession(session);
  }

  @DisplayName("Never inferred: the empty instance of a new resource is inferred and saved")
  @Test
  public void testNeverInferred() {
    action.prepare();

    verify(inferringService).inferMetadata(resource);
    verify(resourceManager).saveInferredMetadata(resource);
    assertSame(inferred, resource.getInferredMetadata());
  }

  @DisplayName("Up to date: not inferred again")
  @Test
  public void testUpToDate() {
    InferredEmlMetadata stored = inferredAt(new Date(2_000_000L));
    resource.setInferredMetadata(stored);
    resource.setMappingsModified(new Date(1_000_000L));

    action.prepare();

    verify(inferringService, never()).inferMetadata(any());
    assertSame(stored, resource.getInferredMetadata());
  }

  @DisplayName("Mappings changed after inferring: inferred again")
  @Test
  public void testMappingsChanged() {
    resource.setInferredMetadata(inferredAt(new Date(1_000_000L)));
    resource.setMappingsModified(new Date(2_000_000L));

    action.prepare();

    verify(inferringService).inferMetadata(resource);
    assertSame(inferred, resource.getInferredMetadata());
  }

  @DisplayName("Re-inferring requested: inferred again although up to date")
  @Test
  public void testReinferRequested() {
    resource.setInferredMetadata(inferredAt(new Date(2_000_000L)));
    when(req.getParameter(Constants.REQ_PARAM_REINFER_METADATA)).thenReturn("true");

    action.prepare();

    verify(inferringService).inferMetadata(resource);
  }

  @DisplayName("Metadata-only resources: not inferred, unless requested")
  @Test
  public void testMetadataOnly() {
    resource.setCoreType(Constants.DATASET_TYPE_METADATA_IDENTIFIER);

    action.prepare();
    verify(inferringService, never()).inferMetadata(any());

    when(req.getParameter(Constants.REQ_PARAM_REINFER_METADATA)).thenReturn("true");
    action.prepare();
    verify(inferringService).inferMetadata(resource);
  }

  @DisplayName("Inferring not supported (e.g. ColDP): empty inferred metadata, no error")
  @Test
  public void testInferringNotSupported() {
    when(inferringService.inferMetadata(any(Resource.class))).thenReturn(null);

    action.prepare();

    assertInstanceOf(InferredEmlMetadata.class, resource.getInferredMetadata());
    assertNull(resource.getInferredMetadata().getLastModified());
  }

  @DisplayName("Metadata-only resource without inferred metadata: no error")
  @Test
  public void testMetadataOnlyWithoutInferredMetadata() {
    resource.setCoreType(Constants.DATASET_TYPE_METADATA_IDENTIFIER);
    resource.setInferredMetadata(null);

    action.prepare();

    verify(inferringService, never()).inferMetadata(any());
  }

  private static InferredEmlMetadata inferredAt(Date date) {
    InferredEmlMetadata metadata = new InferredEmlMetadata();
    metadata.setLastModified(date);
    return metadata;
  }
}
