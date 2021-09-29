package org.gbif.ipt.action;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.LocaleProvider;
import com.opensymphony.xwork2.LocaleProviderFactory;
import com.opensymphony.xwork2.inject.Container;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseActionTest {

  /**
   * Test getLocale on none Struts action context.
   */
  @Test
  public void testGetLocaleOnNoneActionContext() {
    BaseAction action = new BaseAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class));
    assertEquals(null, action.getLocale());
  }

  /**
   * Test getLocale on Struts action context.
   */
  @Test
  public void testGetLocaleOnActionContext() {

    //Simple mocked ActionContext
    ActionContext mockActionContext = mock(ActionContext.class);
    Container mockContainer = mock(Container.class);
    LocaleProviderFactory mockLocaleProviderFactory = mock(LocaleProviderFactory.class);
    LocaleProvider mockLocaleProvider = mock(LocaleProvider.class);

    when(mockLocaleProvider.getLocale()).thenReturn(Locale.JAPANESE);
    when(mockLocaleProviderFactory.createLocaleProvider()).thenReturn(mockLocaleProvider);
    when(mockContainer.getInstance(LocaleProviderFactory.class)).thenReturn(mockLocaleProviderFactory);
    when(mockActionContext.getContainer()).thenReturn(mockContainer);

    //Set threadLocal ActionContext
    ActionContext.setContext(mockActionContext);

    //TEST
    BaseAction action = new BaseAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class));
    assertEquals(Locale.JAPANESE, action.getLocale());
  }
}
