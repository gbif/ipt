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
package org.gbif.ipt.task;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.ipt.service.manage.SourceManager;

public class GenerateDataPackageFactoryImpl implements GenerateDataPackageFactory {

  private final DataDir dataDir;
  private final SourceManager sourceManager;
  private final AppConfig cfg;
  private final MetadataReader metadataReader;

  public GenerateDataPackageFactoryImpl(DataDir dataDir, SourceManager sourceManager, AppConfig cfg, MetadataReader metadataReader) {
    this.dataDir = dataDir;
    this.sourceManager = sourceManager;
    this.cfg = cfg;
    this.metadataReader = metadataReader;
  }

  @Override
  public GenerateDataPackage create(Resource resource, ReportHandler handler) {
    return new GenerateDataPackage(resource, handler, dataDir, sourceManager, cfg, metadataReader);
  }
}
