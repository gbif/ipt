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
package org.gbif.provider.service.impl;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.model.eml.Eml;

import com.google.common.collect.ImmutableSet;

import java.io.File;

/**
 * This class provides an interface for accessing information about archives
 * that represent resources containing metadata only (like a {@link Resource})
 * or containing both metadata and data (like resources that extend
 * {@link DataResource}).
 * 
 */
public interface ResourceArchive {

  /**
   * Returns the archive file.
   * 
   * @return File the archive file
   */
  File getArchiveFile();

  /**
   * Returns the {@link Eml}.
   * 
   * @return Eml the eml
   */
  Eml getEml();

  /**
   * Returns an {@link ImmutableSet} of all {@link Extension}s.
   * 
   * @return ImmutableSet<Extension> all extensions
   */
  ImmutableSet<Extension> getExtensions();

  /**
   * Returns an {@link ImmutableSet} of all {@link Extension}s for a given
   * {@link SourceFile}.
   * 
   * @param <T> the type of source file
   * @param source the source file
   * @return ImmutableSet<Extension> source file extensions
   */
  <T extends SourceFile> ImmutableSet<Extension> getExtensions(T source);

  /**
   * Returns an {@link ImmutableSet} of all {@link SourceFile}s.
   * 
   * @param <T> the type of source file
   * @return ImmutableSet<T> source files
   */
  <T extends SourceFile> ImmutableSet<T> getSourceFiles();
}
