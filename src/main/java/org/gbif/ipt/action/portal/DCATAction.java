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
package org.gbif.ipt.action.portal;

import org.gbif.ipt.task.GenerateDCAT;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;

import org.apache.struts2.ActionSupport;

import lombok.Getter;

/**
 * Action to create the DCAT feed.
 */
public class DCATAction extends ActionSupport {

  @Serial
  private static final long serialVersionUID = 1261384385423019045L;

  private final GenerateDCAT generateDCAT;

  /**
   * DCAT feed
   */
  @Getter
  private InputStream dcatInfo;

  @Inject
  public DCATAction(GenerateDCAT generateDCAT) {
    this.generateDCAT = generateDCAT;
  }

  /**
   * Regenerates the DCAT feed.
   *
   * @return Struts2 result string
   */
  @Override
  public String execute() throws Exception {
    dcatInfo = new ByteArrayInputStream(generateDCAT.getFeed().getBytes(StandardCharsets.UTF_8));
    return SUCCESS;
  }

}
