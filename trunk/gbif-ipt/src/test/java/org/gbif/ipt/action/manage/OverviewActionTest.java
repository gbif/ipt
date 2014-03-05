package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.IOException;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OverviewActionTest {

  private OverviewAction action;

  @Before
  public void setup()
    throws IOException, ParserConfigurationException, SAXException, AlreadyExistingException, ImportException {

    ResourceManager mockResourceManager = mock(ResourceManager.class);
    ListMultimap<String, Date> processFailures = ArrayListMultimap.create();
    processFailures.put("res1", new Date());
    processFailures.put("res1", new Date());
    when(mockResourceManager.getProcessFailures()).thenReturn(processFailures);

    // mock action
    action = new OverviewAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
      mock(RegistrationManager.class), mockResourceManager, mock(UserAccountManager.class),
      mock(ExtensionManager.class), mock(VocabulariesManager.class));
  }

  @Test
  public void testLogProcessFailures() {
    Resource resource = new Resource();
    resource.setShortname("res1");
    resource.setTitle("Mammals");
    action.logProcessFailures(resource);
    resource.setShortname("res2");
    action.logProcessFailures(resource);
  }
}
