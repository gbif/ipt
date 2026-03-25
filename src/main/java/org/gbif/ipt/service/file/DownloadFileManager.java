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

import org.gbif.ipt.model.UrlMetadata;
import org.gbif.ipt.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.SneakyThrows;

public class DownloadFileManager {

  private static final Logger LOG = LogManager.getLogger(DownloadFileManager.class);

  public static boolean isAvailable(String url) {
    try {
      UrlMetadata urlMetadata = FileUtils.fetchUrlMetadata(url);
      return urlMetadata.getStatus() == HttpURLConnection.HTTP_OK;
    } catch (Exception e) {
      LOG.warn("Error getting file information", e);
      return false;
    }
  }

  @SneakyThrows
  public File download(String url, Path targetFilePath) {
    Files.createDirectories(targetFilePath.getParent());
    File targetFile = targetFilePath.toFile();
    try (ReadableByteChannel in = Channels.newChannel(new URL(url).openStream());
         FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
      fileOutputStream.getChannel().transferFrom(in, 0, Long.MAX_VALUE);
      return targetFile;
    }
  }

  @SneakyThrows
  public CompletableFuture<File> downloadAsync(
      String url,
      Path targetFilePath,
      Consumer<File> successCallback,
      Consumer<Throwable> errorCallback) {
    return CompletableFuture.supplyAsync(() -> download(url, targetFilePath))
        .whenComplete(
            (result, error) -> {
              if (error != null) {
                LOG.error("Error downloading file from url {}", url, error);
                errorCallback.accept(error);
              } else {
                successCallback.accept(result);
              }
            });
  }
}
