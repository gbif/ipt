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

import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.impl.ResourceArchive;

import java.io.File;
import java.io.IOException;

/**
 * This class provides a service interface for working with
 * {@link ResourceArchive}s.
 * 
 */
public interface ResourceArchiveManager {

  /**
   * Binds an existing resource with an existing archive by replacing the
   * resource core source file and extension mappings with those in the archive.
   * 
   * @param <R> the resource type
   * @param <A> the archive type
   * @param resource the resource
   * @param archive the archive
   * @return R the resource bound with the archive
   */
  <R extends Resource, A extends ResourceArchive> R bind(R resource, A archive);

  /**
   * 
   * @deprecated
   * @param resource
   * @return
   * @throws IOException File
   */
  @Deprecated
  File createArchive(DataResource resource) throws IOException;

  /**
   * Creates a {@link ResourceArchive} for a {@link Resource}.
   * 
   * TODO: Should a File location be passed in here?
   * 
   * @param <R> the resource type
   * @param resource the resource to archive
   * @return the archive created for the resource
   * @throws IOException File
   */
  <R extends Resource, A extends ResourceArchive> A createArchive(R resource)
      throws IOException;

  /**
   * Creates a {@link Resource} from an {@link ResourceArchive}.
   * 
   * @param <R> the resource type
   * @param <A> the archive type
   * @param archive the archive from which a resource will be created
   * @return the resource
   * @throws IOException R
   */
  <R extends Resource, A extends ResourceArchive> R createResource(A archive)
      throws IOException;

  /**
   * Opens and returns a {@link ResourceArchive}.
   * 
   * @param <A> the archive type
   * @param location the archive location on disk
   * @param resource TODO
   * @param normalise normalise extension names
   * @return the archive
   * @throws IOException
   * @throws UnsupportedArchiveException A
   */
  <A extends ResourceArchive> A openArchive(File location, Resource resource, boolean normalise)
      throws IOException, UnsupportedArchiveException;
}