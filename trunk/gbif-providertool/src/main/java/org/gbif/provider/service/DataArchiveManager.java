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
package org.gbif.provider.service;

import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.SourceFile;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.IOException;

/**
 * This class provides a service interface for working with data archives in the
 * Darwin Core Archive format.
 * 
 * @see http://goo.gl/Xbnr
 * 
 */
public interface DataArchiveManager {

  /**
   * Creates a Darwin Core Archive on disk for a {@link DataResource}.
   * 
   * @see http://goo.gl/Xbnr
   * 
   * @param resource the resource to archive
   * @return the archived file
   * @throws IOException
   */
  File createArchive(DataResource resource) throws IOException;

  /**
   * Returns the core {@link SourceFile} of the archive.
   * 
   * @param archive the archive
   * @return the core source file of the archive
   * @throws IOException SourceFile
   */
  SourceFile getCoreSourceFile(Archive archive) throws IOException;

  /**
   * Returns an immutable set of {@link Extension}s associated with an
   * {@link Archive}.
   * 
   * @param archive the archive
   * @return SourceFile an immutable set of core source files
   */
  ImmutableSet<Extension> getExtensions(Archive archive) throws IOException;

  /**
   * Opens an existing archive file that is stored on disk using the Darwin Core
   * Archive format and returns it as an {@link Archive}.
   * 
   * @see http://goo.gl/Xbnr
   * 
   * @param location the file location of the archive
   * @param normaliseExtension
   * @return {@link Archive}
   * @throws IOException
   * @throws UnsupportedArchiveException
   */
  Archive openArchive(File location, boolean normaliseExtension)
      throws IOException, UnsupportedArchiveException;
}
