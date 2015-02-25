package org.gbif.ipt.task;

import org.gbif.ipt.model.Resource;

public interface GenerateDwcaFactory {

  GenerateDwca create(Resource resource, ReportHandler handler);
}
