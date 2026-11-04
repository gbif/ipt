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
package org.gbif.ipt.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Something the IPT worked around while loading a resource, e.g. it couldn't read eml.xml and used the last published
 * version instead. Kept in memory only: the next load finds out again.
 */
public class ResourceLoadNotice implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  public enum Type {
    /** A mapped extension wasn't installed, so the IPT installed it from the registry. Detail: the row type. */
    EXTENSION_INSTALLED(false),
    /** The data package type wasn't installed, so the IPT installed it from the registry. Detail: its identifier. */
    DATA_PACKAGE_TYPE_INSTALLED(false),
    /** eml.xml couldn't be read, the last published EML is used. Detail: the version. */
    EML_FROM_PUBLISHED(true),
    /** eml.xml couldn't be read and there's no published EML, the metadata is empty. */
    EML_UNREADABLE(true),
    /** The data package metadata file couldn't be read, the last published one is used. Detail: the version. */
    METADATA_FROM_PUBLISHED(true),
    /** The data package metadata file couldn't be read and there's no published one, the metadata is empty. */
    METADATA_UNREADABLE(true);

    private final boolean needsAttention;

    Type(boolean needsAttention) {
      this.needsAttention = needsAttention;
    }

    /**
     * @return true if a manager has to do something, e.g. check and save the metadata; false if it's only for
     * information
     */
    public boolean isNeedsAttention() {
      return needsAttention;
    }

    public boolean isEml() {
      return this == EML_FROM_PUBLISHED || this == EML_UNREADABLE;
    }

    public boolean isDataPackageMetadata() {
      return this == METADATA_FROM_PUBLISHED || this == METADATA_UNREADABLE;
    }
  }

  private final Type type;
  private final String detail;

  public ResourceLoadNotice(Type type, String detail) {
    this.type = Objects.requireNonNull(type);
    this.detail = detail == null ? "" : detail;
  }

  public Type getType() {
    return type;
  }

  /**
   * @return the parameter of the notice's text, e.g. a version or row type; empty if it has none
   */
  public String getDetail() {
    return detail;
  }

  /**
   * @return key of the notice's text, it takes the detail as parameter
   */
  public String getTextKey() {
    return "manage.resource.loadNotice." + type.name();
  }

  public boolean isNeedsAttention() {
    return type.isNeedsAttention();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ResourceLoadNotice that)) {
      return false;
    }
    return type == that.type && detail.equals(that.detail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, detail);
  }
}
