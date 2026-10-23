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

import org.gbif.utils.file.ClosableReportingIterator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Source rows for tests, optionally simulating reading errors.
 */
class InMemoryRowIterator implements ClosableReportingIterator<String[]> {

  private final List<String[]> rows;
  private final Set<Integer> rowsWithError = new HashSet<>();
  private int failAt = -1;
  private RuntimeException failure;
  private Exception closeFailure;
  private int next;
  private boolean currentRowHasError;

  InMemoryRowIterator(List<String[]> rows) {
    this.rows = rows;
  }

  /**
   * The rows (0-based) are reported as having a reading error.
   */
  InMemoryRowIterator withRowErrors(Integer... rowIndexes) {
    rowsWithError.addAll(Arrays.asList(rowIndexes));
    return this;
  }

  /**
   * Throws the exception when reading the row (0-based).
   */
  InMemoryRowIterator failingAt(int rowIndex, RuntimeException failure) {
    this.failAt = rowIndex;
    this.failure = failure;
    return this;
  }

  InMemoryRowIterator failingOnClose(Exception closeFailure) {
    this.closeFailure = closeFailure;
    return this;
  }

  @Override
  public boolean hasRowError() {
    return currentRowHasError;
  }

  @Override
  public String getErrorMessage() {
    return currentRowHasError ? "row error" : null;
  }

  @Override
  public Exception getException() {
    return null;
  }

  @Override
  public void close() throws Exception {
    if (closeFailure != null) {
      throw closeFailure;
    }
  }

  @Override
  public boolean hasNext() {
    return next < rows.size();
  }

  @Override
  public String[] next() {
    if (next == failAt) {
      throw failure;
    }
    currentRowHasError = rowsWithError.contains(next);
    return rows.get(next++);
  }
}
