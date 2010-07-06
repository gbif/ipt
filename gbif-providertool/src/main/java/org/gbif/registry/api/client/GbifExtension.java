/*
 * Copyright 2010 Regents of the University of California, University of Kansas.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.registry.api.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * This class encapsulates a GBIF Extension returned from the GBRDS.
 * 
 * @see http://gbrds.gbif.org/registry/ipt/extensions.json
 * 
 */
@SuppressWarnings("serial")
public class GbifExtension implements Serializable {

  public static GbifExtension create(int id, String title, String url) {
    checkNotNull(id);
    checkNotNull(title);
    checkNotNull(url);
    return new GbifExtension(id, title, url);
  }

  private final int id;
  private final String title;
  private final String url;

  GbifExtension() {
    this(-1, null, null);
  }

  private GbifExtension(int id, String title, String url) {
    this.id = id;
    this.title = title;
    this.url = url;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GbifExtension)) {
      return false;
    }
    GbifExtension o = (GbifExtension) other;
    return Objects.equal(id, o.id) && Objects.equal(title, o.title)
        && Objects.equal(url, o.url);
  }

  public int getId() {
    return id;
  };

  public String getTitle() {
    return title;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, title, url);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("Id", id).add("Title", title).add(
        "URL", url).toString();
  }
}