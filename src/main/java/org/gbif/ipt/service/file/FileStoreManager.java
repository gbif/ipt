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

import org.gbif.utils.file.CompressionUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

import static org.gbif.ipt.service.file.DownloadFileManager.isAvailable;
import static org.gbif.ipt.service.file.MediaTypeAndFormatDetector.COMPRESS_CONTENT_TYPE;
import static org.gbif.ipt.service.file.MediaTypeAndFormatDetector.detectMediaType;
import static org.gbif.ipt.service.file.MediaTypeAndFormatDetector.evaluateMediaTypeAndFormat;

public class FileStoreManager {

  @Data
  @Builder
  public static class AsyncDownloadResult {
    private final DataFile dataFile;
    private final CompletableFuture<File> downloadTask;
  }

  private static final Logger LOG = LogManager.getLogger(FileStoreManager.class);

  private final DownloadFileManager downloadFileManager;

  public FileStoreManager(DownloadFileManager downloadFileManager)
      throws IOException {
    this.downloadFileManager = downloadFileManager;
  }

  public CompletableFuture<DataFile> extractAndGetFileInfoAsync(
      Path dataFilePath, Path destinationFolder, String fileName) {
    return CompletableFuture.supplyAsync(
        () -> extractAndGetFileInfo(dataFilePath, destinationFolder, fileName));
  }

  @SneakyThrows
  public DataFile extractAndGetFileInfo(
      Path dataFilePath, Path destinationFolder, String fileName) {
    try {
      // check if we have something to unzip
      String detectedMediaType = detectMediaType(dataFilePath);

      if (COMPRESS_CONTENT_TYPE.contains(detectedMediaType)) {
        CompressionUtil.decompressFile(destinationFolder.toFile(), dataFilePath.toFile());
        FileUtils.deleteQuietly(dataFilePath.toFile());
      }

      // from here we can decide to change the content type (e.g. zipped excel file)
      return fromMediaTypeAndFormat(dataFilePath, fileName, detectedMediaType, destinationFolder);
    } catch (Exception ex) {
      LOG.warn("Deleting temporary content of {} after IOException.", fileName);
      FileUtils.deleteDirectory(destinationFolder.toFile());
      throw ex;
    }
  }

  private DataFile fromMediaTypeAndFormat(
      Path dataFilePath, String fileName, String detectedMediaType, Path finalPath)
      throws UnsupportedMediaTypeException, IOException {
    return evaluateMediaTypeAndFormat(finalPath, detectedMediaType)
        .map(
            mtf ->
                DataFile.create(
                    dataFilePath,
                    fileName,
                    mtf.getFileFormat(),
                    detectedMediaType,
                    mtf.getMediaType()))
        .orElseThrow(
            () -> new UnsupportedMediaTypeException("Unsupported file type: " + detectedMediaType));
  }

  @SneakyThrows
  public AsyncDownloadResult downloadDataFile(
      String url,
      String targetDirectory,
      Consumer<DataFile> resultCallback,
      Consumer<Throwable> errorCallback)
      throws IOException {

    if (!isAvailable(url)) {
      throw new IllegalArgumentException(
          "Failed to download file from "
              + url
              + ". The resource is not reachable. Please check that the URL is correct");
    }

    String fileName = getFileName(url);
    Path destinationFolder = Paths.get(targetDirectory);
    createIfNotExists(destinationFolder);
    Path dataFilePath = destinationFolder.resolve(fileName);
    return AsyncDownloadResult.builder()
        .dataFile(DataFile.builder().sourceFileName(fileName).filePath(dataFilePath).build())
        .downloadTask(
            downloadFileManager.downloadAsync(
                url,
                dataFilePath,
                file ->
                    resultCallback.accept(
                        extractAndGetFileInfo(dataFilePath, destinationFolder, fileName)),
                errorCallback))
        .build();
  }

  @SneakyThrows
  private static String getFileName(String url) {
    return Paths.get(new URI(url).getPath()).getFileName().toString();
  }

  /** Creates the directory(path) if it doesn't exist. */
  private static void createIfNotExists(Path path) throws IOException {
    if (!path.toFile().exists()) {
      Files.createDirectories(path);
    }
  }

}
