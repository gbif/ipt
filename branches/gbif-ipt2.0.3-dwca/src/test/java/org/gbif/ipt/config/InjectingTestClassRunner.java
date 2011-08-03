package org.gbif.ipt.config;


import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.JUnit4ClassRunner;

public class InjectingTestClassRunner extends JUnit4ClassRunner {
  public InjectingTestClassRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected Object createTest() throws Exception {
    Injector injector = Guice.createInjector(new IPTTestModule());
    return injector.getInstance(this.getTestClass().getJavaClass());
  }
}
