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
package org.gbif.ipt.service.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tika.Tika;

import com.google.common.collect.ImmutableList;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class MediaTypeAndFormatDetector {

  public static final String TEXT_PLAIN = "text/plain";
  public static final String TEXT_CSV = "text/csv";
  public static final String TEXT_TSV = "text/tab-separated-values";
  // .xls
  public static final String APPLICATION_EXCEL = "application/vnd.ms-excel";
  // .xlsx
  public static final String APPLICATION_OFFICE_SPREADSHEET =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  // .ods
  public static final String APPLICATION_OPEN_DOC_SPREADSHEET =
      "application/vnd.oasis.opendocument.spreadsheet";
  // .xml
  public static final String APPLICATION_XML = "application/xml";
  public static final String APPLICATION_ZIP = "application/zip";
  public static final String APPLICATION_GZIP = "application/gzip";

  public static final List<String> COMPRESS_CONTENT_TYPE =
      ImmutableList.of(APPLICATION_ZIP, APPLICATION_GZIP);

  public static final List<String> TABULAR_CONTENT_TYPES =
      ImmutableList.of(TEXT_PLAIN, TEXT_CSV, TEXT_TSV);

  public static final List<String> SPREADSHEET_CONTENT_TYPES =
      ImmutableList.of(
          APPLICATION_EXCEL, APPLICATION_OFFICE_SPREADSHEET, APPLICATION_OPEN_DOC_SPREADSHEET);

  public static final List<String> XML_CONTENT_TYPES = ImmutableList.of(APPLICATION_XML);

  private static final Tika TIKA = new Tika();

  public static String detectMediaType(Path filePath) throws IOException {
    return TIKA.detect(filePath);
  }

  /**
   * Given a {@link Path} to a file (or folder) and a original contentType this function will check
   * to reevaluate the contentType and return the matching {@link FileFormat}. If a more specific
   * contentType can not be found the original one will be return with the matching {@link
   * FileFormat}.
   *
   * @param dataFilePath shall point to data file or folder (not a zip file)
   */
  public static Optional<MediaTypeAndFormat> evaluateMediaTypeAndFormat(
      Path dataFilePath, String detectedContentType) throws IOException {
    Objects.requireNonNull(dataFilePath, "dataFilePath must be provided");
    String contentType = detectedContentType;

    if (COMPRESS_CONTENT_TYPE.contains(detectedContentType)) {
      List<Path> content;
      try (Stream<Path> list = Files.list(dataFilePath)) {
        content = list.collect(Collectors.toList());
      }
      if (content.size() == 1) {
        contentType = MediaTypeAndFormatDetector.detectMediaType(content.get(0));
      } else {
        return Optional.of(MediaTypeAndFormat.create(contentType, FileFormat.DWCA));
      }
    }

    if (TABULAR_CONTENT_TYPES.contains(contentType)) {
      return Optional.of(MediaTypeAndFormat.create(contentType, FileFormat.TABULAR));
    }

    if (SPREADSHEET_CONTENT_TYPES.contains(contentType)) {
      return Optional.of(MediaTypeAndFormat.create(contentType, FileFormat.SPREADSHEET));
    }
    return Optional.empty();
  }

  /** Simple holder for mediaType and fileFormat */
  @Getter
  @AllArgsConstructor(staticName = "create")
  public static class MediaTypeAndFormat {
    private final String mediaType;
    private final FileFormat fileFormat;
  }
}
