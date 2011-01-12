/*
 * Copyright 2009 GBIF.
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
package org.gbif.provider.datasource;

/**
 * Exception thrown when import source cant be created or read. Abstracts
 * IOException or SQLException or any other upcoming source problems
 * 
 */
public class ImportSourceException extends Exception {

  public ImportSourceException() {
    super();
  }

  public ImportSourceException(Exception e) {
    super(e);
  }

  public ImportSourceException(String message) {
    super(message);
  }

  public ImportSourceException(String message, Exception e) {
    super(message, e);
  }
}
