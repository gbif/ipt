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
package org.gbif.provider.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.gbif.provider.model.SourceFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reads a SourceFile and provides an interator over all rows.
 * 
 */
public class SourceFileReader implements Iterator<String[]> {

  public static SourceFileReader with(SourceFile sourceFile) {
    checkNotNull(sourceFile, "SourceFile is null");
    checkNotNull(sourceFile.getResourceId(), "SourceFile resource id is null");
    File f = AppConfig.getResourceSourceFile(sourceFile.getResourceId(),
        sourceFile.getFilename());
    checkNotNull(f, "SourceFile file is null");
    checkArgument(f.exists() && f.canRead(), "SourceFile file cannot be read");
    return new SourceFileReader(sourceFile, f);
  }

  private final SourceFile sourceFile;
  private List<String> lines;
  private final File f;
  private Iterator<String> linesIterator;

  private SourceFileReader(SourceFile sourceFile, File f) {
    this.sourceFile = sourceFile;
    this.f = f;
  }

  /**
   * 
   * @see java.util.Iterator#hasNext()
   */
  public boolean hasNext() {
    if (!init()) {
      return false;
    }
    return linesIterator.hasNext();
  }

  /**
   * 
   * @see java.util.Iterator#next()
   */
  public String[] next() {
    if (!init()) {
      return null;
    }
    ArrayList<String> tokens = Lists.newArrayList(Splitter.on(
        sourceFile.getSeparator()).trimResults(CharMatcher.is('"')).split(
        linesIterator.next()));
    String[] results = new String[tokens.size()];
    tokens.toArray(results);
    return results;
  }

  /**
   * 
   * @see java.util.Iterator#remove()
   */
  public void remove() {
    if (init()) {
      linesIterator.remove();
    }
  }

  private boolean init() {
    if (lines == null) {
      try {
        lines = Files.readLines(f, Charset.forName(sourceFile.getEncoding()));
        linesIterator = lines.iterator();
        for (int i = 0; i < sourceFile.getNumLinesToSkip(); i++) {
          linesIterator.next();
        }
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    }
    return true;
  }

}
