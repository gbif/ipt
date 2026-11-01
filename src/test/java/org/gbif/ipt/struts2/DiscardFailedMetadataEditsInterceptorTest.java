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
package org.gbif.ipt.struts2;

import org.gbif.ipt.action.manage.CamtrapMetadataAction;
import org.gbif.ipt.action.manage.DataPackageMetadataAction;
import org.gbif.ipt.action.manage.MetadataAction;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.ResourceMetadataLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts2.ActionContext;
import org.apache.struts2.ActionInvocation;
import org.apache.struts2.action.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DiscardFailedMetadataEditsInterceptorTest {

  @Mock
  private ActionInvocation invocation;
  @Mock
  private HttpServletRequest request;
  @Mock
  private MetadataAction action;
  @Mock
  private CamtrapMetadataAction camtrapAction;
  @Mock
  private DataPackageMetadataAction dataPackageAction;
  @Mock
  private ResourceMetadataLoader loader;
  @Mock
  private DataDir dataDir;

  @TempDir
  Path tmp;

  private final Resource resource = new Resource();
  private final DiscardFailedMetadataEditsInterceptor interceptor = new DiscardFailedMetadataEditsInterceptor();
  private File emlFile;
  private File dataPackageFile;

  @BeforeEach
  public void setUp() throws IOException {
    resource.setShortname("res");
    resource.setCoreType("camtrap-dp");
    emlFile = Files.createFile(tmp.resolve("eml.xml")).toFile();
    dataPackageFile = Files.createFile(tmp.resolve("datapackage.json")).toFile();
    interceptor.setResourceMetadataLoader(loader);
    interceptor.setDataDir(dataDir);

    when(invocation.getInvocationContext()).thenReturn(ActionContext.of().withServletRequest(request));
    when(invocation.getAction()).thenReturn(action);
    when(action.getResource()).thenReturn(resource);
    when(camtrapAction.getResource()).thenReturn(resource);
    when(dataPackageAction.getResource()).thenReturn(resource);
    when(dataDir.resourceEmlFile("res")).thenReturn(emlFile);
    when(dataDir.resourceDatapackageMetadataFile("res", "camtrap-dp")).thenReturn(dataPackageFile);
    when(request.getMethod()).thenReturn("POST");
  }

  @Test
  public void failedSaveReloadsEml() throws Exception {
    when(invocation.invoke()).thenReturn(Action.INPUT);
    when(action.hasErrors()).thenReturn(true);

    assertEquals(Action.INPUT, interceptor.intercept(invocation));
    verify(loader).loadEml(resource);
  }

  @Test
  public void successfulSaveKeepsEml() throws Exception {
    when(invocation.invoke()).thenReturn(Action.SUCCESS);

    assertEquals(Action.SUCCESS, interceptor.intercept(invocation));
    verify(loader, never()).loadEml(any());
  }

  @Test
  public void formDisplayKeepsEml() throws Exception {
    // GET renders the form with the INPUT result too
    when(request.getMethod()).thenReturn("GET");
    when(invocation.invoke()).thenReturn(Action.INPUT);
    when(action.hasErrors()).thenReturn(true);

    interceptor.intercept(invocation);
    verify(loader, never()).loadEml(any());
  }

  @Test
  public void inputWithoutErrorsKeepsEml() throws Exception {
    when(invocation.invoke()).thenReturn(Action.INPUT);
    when(action.hasErrors()).thenReturn(false);

    interceptor.intercept(invocation);
    verify(loader, never()).loadEml(any());
  }

  @Test
  public void neverSavedEmlIsKept() throws Exception {
    when(invocation.invoke()).thenReturn(Action.INPUT);
    when(action.hasErrors()).thenReturn(true);
    Files.delete(emlFile.toPath());

    interceptor.intercept(invocation);
    verify(loader, never()).loadEml(any());
  }

  @Test
  public void failedCamtrapSaveReloadsDataPackageMetadata() throws Exception {
    when(invocation.getAction()).thenReturn(camtrapAction);
    when(invocation.invoke()).thenReturn(Action.INPUT);
    when(camtrapAction.hasErrors()).thenReturn(true);

    interceptor.intercept(invocation);
    verify(loader).loadDatapackageMetadata(resource);
    verify(loader, never()).loadEml(any());
  }

  @Test
  public void failedDataPackageSaveReloadsDataPackageMetadata() throws Exception {
    when(invocation.getAction()).thenReturn(dataPackageAction);
    when(invocation.invoke()).thenReturn(Action.INPUT);
    when(dataPackageAction.hasErrors()).thenReturn(true);

    interceptor.intercept(invocation);
    verify(loader).loadDatapackageMetadata(resource);
  }

  @Test
  public void neverSavedDataPackageMetadataIsKept() throws Exception {
    when(invocation.getAction()).thenReturn(dataPackageAction);
    when(invocation.invoke()).thenReturn(Action.INPUT);
    when(dataPackageAction.hasErrors()).thenReturn(true);
    Files.delete(dataPackageFile.toPath());

    interceptor.intercept(invocation);
    verify(loader, never()).loadDatapackageMetadata(any());
  }
}
