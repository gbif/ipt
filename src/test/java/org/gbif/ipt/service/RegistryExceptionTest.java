package org.gbif.ipt.service;

import org.gbif.ipt.action.BaseAction;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegistryExceptionTest {

  @Test
  public void testLogRegistryException() {
    String answer = "An unknown error occurred!";
    BaseAction mockAction = mock(BaseAction.class);
    when(mockAction.getText("admin.registration.error.unknown")).thenReturn(answer);
    String msg = RegistryException.logRegistryException(new RegistryException(RegistryException.Type.UNKNOWN, null, null, null), mockAction);
    assertEquals(answer, msg);

    answer = "Response was empty!";
    when(mockAction.getText("admin.registration.error.badResponse")).thenReturn(answer);
    msg = RegistryException.logRegistryException(new RegistryException(RegistryException.Type.BAD_RESPONSE, null, null, null), mockAction);
    assertEquals(answer, msg);
  }
}
