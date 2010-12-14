package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.mock.ModelMocks;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.SqlSource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.utils.IptMockBaseTest;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SourceManagerImplTest extends IptMockBaseTest {
  @Test
  public void testDelete() throws AlreadyExistingException {
    SourceManager man = new SourceManagerImpl(cfg, dataDir);
    ModelMocks mm = new ModelMocks();
    Resource cfg = new Resource();
    cfg.addSource(mm.src1, false);
    cfg.addSource(mm.src2, false);
    cfg.addSource(mm.src3, false);

    assertTrue(cfg.getSources().size() == 3);

    Source s = new SqlSource();
    s.setName("karl");

    boolean existed = false;
    try {
      cfg.addSource(s, false);
    } catch (AlreadyExistingException e) {
      existed = true;
    }
    assertTrue(existed);

    man.delete(cfg, s);

    assertTrue(cfg.getSources().size() == 2);
  }
}
