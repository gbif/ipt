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
package org.gbif.datapackage;

import org.gbif.dp.analysis.DefaultDataPackageAnalysisOrchestrator;
import org.gbif.dp.analysis.api.AnalysisExecution;
import org.gbif.dp.analysis.api.AnalysisFeature;
import org.gbif.dp.analysis.api.DataAnalyser;
import org.gbif.dp.analysis.api.DataPackageAnalysisOrchestrator;
import org.gbif.dp.analysis.api.DatapackageAnalysisResult;
import org.gbif.dp.analysis.api.ValidationOptions;
import org.gbif.dp.analysis.duckdb.DuckDbDataPackageAnalyser;
import org.gbif.dp.analysis.duckdb.DuckDbDialectRenderer;
import org.gbif.dp.analysis.duckdb.DuckDbResourceLoader;
import org.gbif.dp.common.descriptor.JacksonDataPackageParser;
import org.gbif.dp.descriptor.DataPackageParser;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

/**
 * Equivalent to io.frictionlessdata.datapackage.Package.
 * <p>
 * This covers just the assembly/writing surface.
 * <p>
 * Deliberately NOT covered here (by design, since it lives elsewhere):
 * profile/JSON-Schema validation, field type validation, primary/foreign key checking.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataPackage {

  @Setter
  @Getter
  private String name;
  @Setter
  @Getter
  private String id;
  @Setter
  @Getter
  private String profile;
  @Setter
  @Getter
  private String title;
  @Setter
  @Getter
  private String description;
  @Setter
  @Getter
  private List<Object> licenses = new ArrayList<>();

  @Getter
  private final List<DataPackageResource> resources = new ArrayList<>();

  // title, description, version, created, contributors, keywords, homepage, licenses,
  // and any COL/Camtrap-specific extras all pass through here untyped.
  private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_NULL);

  private final DataPackageParser parser = new JacksonDataPackageParser();
  private final DuckDbDialectRenderer dialectRenderer = new DuckDbDialectRenderer();
  private final DuckDbResourceLoader loader = new DuckDbResourceLoader(dialectRenderer);
  private final DataAnalyser analyser = new DuckDbDataPackageAnalyser(parser, loader);
  private final DataPackageAnalysisOrchestrator dpAnalysisOrchestrator = new DefaultDataPackageAnalysisOrchestrator(analyser);

  public DataPackage() {
  }

  /**
   * Equivalent to `new Package(metadataFile.toPath(), false)` -- loads a user-authored
   * descriptor (e.g. Camtrap DP's datapackage.json) as the starting point, before resources
   * and computed properties get layered on top.
   */
  public static DataPackage load(File descriptorFile) throws IOException {
    return MAPPER.readValue(descriptorFile, DataPackage.class);
  }

  public void addResource(DataPackageResource resource) {
    resources.add(resource);
  }

  /** Equivalent to dataPackage.setProperty(name, value) for any spec or custom field. */
  public void setProperty(String key, Object value) {
    if (value == null) {
      return;
    }
    switch (key) {
      case "name":
        setName((String) value);
        break;
      case "id":
        setId((String) value);
        break;
      case "profile":
        setProfile((String) value);
        break;
      case "title":
        setTitle((String) value);
        break;
      case "description":
        setDescription((String) value);
        break;
      case "licenses":
        setLicenses((List<Object>) value);
        break;
      default:
        additionalProperties.put(key, value);
    }
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String key, Object value) {
    additionalProperties.put(key, value);
  }

  /**
   * Writes the package as a zip archive: datapackage.json at the root, plus each resource's
   * data file(s) resolved against baseDir, plus whatever `extra` writes directly into the
   * archive. This replaces `Package.write(zip, callback, boolean)` -- callers using a method
   * reference like `this::writeEMLMetadata` need no changes, since that method already just
   * does `Files.copy(source, outputDir.getFileSystem().getPath("eml.xml"))`, which works
   * identically against the zip filesystem's root Path handed to it here.
   */
  public void write(File zipFile, File baseDir, ExtraFilesWriter extra) throws IOException {
    if (zipFile.exists() && !zipFile.delete()) {
      throw new IOException("Could not overwrite existing file: " + zipFile);
    }

    URI uri = URI.create("jar:" + zipFile.toURI());
    Map<String, String> env = Map.of("create", "true");

    try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
      Path root = zipfs.getPath("/");

      Files.write(root.resolve("datapackage.json"), MAPPER.writeValueAsBytes(this));

      for (DataPackageResource resource : resources) {
        for (String relativePath : resource.getPaths()) {
          Path source = baseDir.toPath().resolve(relativePath);
          Path target = root.resolve(relativePath);
          if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
          }
          Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
      }

      if (extra != null) {
        extra.write(root);
      }
    }
  }

  public void write(File zipFile, File baseDir) throws IOException {
    write(zipFile, baseDir, null);
  }

  public AnalysisExecution<DatapackageAnalysisResult> validate(File baseDir) throws IOException {
    Path descriptorPath = baseDir.toPath().resolve("datapackage.json");
    Files.write(descriptorPath, MAPPER.writeValueAsBytes(this));
    try {
      return dpAnalysisOrchestrator.analyseWithFullReport(
          descriptorPath.toString(),
          new ValidationOptions(20),
          AnalysisFeature.ALL_FEATURES);
    } catch (SQLException e) {
      throw new IOException("Data package validation could not be executed", e);
    }
  }

  @FunctionalInterface
  public interface ExtraFilesWriter {
    void write(Path zipRoot) throws IOException;
  }
}
