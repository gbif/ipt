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
package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.model.InferredCamtrapMetadata;
import org.gbif.ipt.model.InferredEmlMetadata;
import org.gbif.ipt.model.InferredMetadata;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.service.manage.ResourcePersister;

import java.io.InputStream;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;

public class XStreamResourcePersister implements ResourcePersister {

  private final XStream xstream;

  public XStreamResourcePersister(
      ResourceConvertersManager resourceConvertersManager,
      PasswordEncrypter passwordEncrypter) {
    this.xstream = ResourceXStreamFactory.create(resourceConvertersManager, passwordEncrypter);;
  }

  @Override
  public void save(Resource resource, Writer writer) {
    xstream.toXML(resource, writer);
  }

  @Override
  public void saveInferredMetadata(InferredMetadata metadata, Writer writer) {
    xstream.toXML(metadata, writer);
  }

  @Override
  public Resource load(InputStream input) {
    return (Resource) xstream.fromXML(input);
  }

  @Override
  public InferredCamtrapMetadata loadInferredCamtrapMetadata(InputStream input) {
    return (InferredCamtrapMetadata) xstream.fromXML(input);
  }

  @Override
  public InferredEmlMetadata loadInferredEmlMetadata(InputStream input) {
    return (InferredEmlMetadata) xstream.fromXML(input);
  }
}
