/*
 * Copyright 2010 Global Biodiversity Informatics Facility.
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
package org.gbif.provider.service.util;

import static com.google.common.base.Preconditions.checkNotNull;

import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.service.ExtensionManager;

import com.google.common.collect.ImmutableSet;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.NonUniqueResultException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class can be used for adapting an {@link Archive} to corresponding
 * classes in the IPT. For example, it adapts an {@link ArchiveFile} extension
 * to equivalent IPT {@link Extension} objects.
 * 
 */
class IptArchiveAdapter {
  @Autowired
  private static ExtensionManager extensionManager;

  private static final Log log = LogFactory.getLog(IptArchiveAdapter.class);

  /**
   * Returns the core {@link SourceFile} from an {@link Archive}.
   * 
   * @param archive the archive
   * @return SourceFile the source file
   */
  static SourceFile getCoreSourceFile(Archive archive) {
    checkNotNull(archive, "Archive is null");
    checkNotNull(archive.getCore(), "Archive core is null");
    checkNotNull(archive.getCore().getTitle(), "Archive core title is null");
    SourceFile core = new SourceFile(new File(archive.getCore().getTitle()));
    return core;
  }

  /**
   * Returns all {@link Extension}s from an {@link Archive}.
   * 
   * @param archive the archive
   * @return ImmutableSet<Extension> the archive extensions
   */
  static ImmutableSet<Extension> getExtensions(Archive archive) {
    checkNotNull(archive, "Archive is null");
    checkNotNull(archive.getExtensions(), "Archive extensions are null");
    if (archive.getExtensions().isEmpty()) {
      return ImmutableSet.of();
    }
    Extension extension;
    ImmutableSet.Builder<Extension> b = ImmutableSet.builder();
    for (ArchiveFile f : archive.getExtensions()) {
      try {
        extension = extensionManager.getExtensionByUri(f.getRowType());
        if (extension != null) {
          b.add(extension);
        } else {
          log.warn("Null extension returned for namespace: " + f.getRowType());
        }
      } catch (NonUniqueResultException e) {
        log.warn("Duplicate extension namespace was found: " + f.getRowType());
      }
    }
    return b.build();
  }
}