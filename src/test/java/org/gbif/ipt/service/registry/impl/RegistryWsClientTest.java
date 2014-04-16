package org.gbif.ipt.service.registry.impl;

import org.gbif.api.model.registry.Dataset;
import org.gbif.api.service.registry.DatasetService;

import java.util.UUID;

import org.junit.Test;

import static org.gbif.ipt.config.RegistryTestModule.webserviceClient;
import static org.gbif.ipt.config.RegistryTestModule.webserviceClientReadOnly;

import static org.junit.Assert.assertEquals;

public class RegistryWsClientTest {

  @Test
  public void testGetDataset() {
    DatasetService ds = webserviceClientReadOnly().getInstance(DatasetService.class);
    Dataset dataset = ds.get(UUID.fromString("8575f23e-f762-11e1-a439-00145eb45e9a"));
    assertEquals("PonTaurus collection", dataset.getTitle());
  }

  @Test
  public void testUpdateDataset() {
    DatasetService ds = webserviceClient().getInstance(DatasetService.class);
    Dataset dataset = ds.get(UUID.fromString("8575f23e-f762-11e1-a439-00145eb45e9a"));
    dataset.setRights("CC0");
    ds.update(dataset);
  }

}
