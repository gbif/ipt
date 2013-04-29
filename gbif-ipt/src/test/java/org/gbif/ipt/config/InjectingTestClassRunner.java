package org.gbif.ipt.config;


import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.runners.BlockJUnit4ClassRunner;

public class InjectingTestClassRunner extends BlockJUnit4ClassRunner {

  public InjectingTestClassRunner(Class<?> klass) throws org.junit.runners.model.InitializationError {
    super(klass);
  }

  @Override
  protected Object createTest() throws Exception {
    Injector injector = Guice.createInjector(new IPTTestModule());
    return injector.getInstance(this.getTestClass().getJavaClass());
  }
}
