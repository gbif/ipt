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
package org.gbif.ipt.action;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.ExtendedResponse;
import org.gbif.utils.HttpClient;

import jakarta.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.json.annotations.JSON;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serial;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HealthAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = 5332856472390163029L;

  private static final Logger LOG = LogManager.getLogger(HealthAction.class);

  // Free space thresholds. Absolute, not a share of the disk: 15% free is plenty on a 2 TB disk and nothing on a
  // 20 GB one. What matters is room for a publication: source files, the archive being built, archived versions.
  static final long DISK_LOW_BYTES = 10L * FileUtils.ONE_GB;
  static final long DISK_FULL_BYTES = 2L * FileUtils.ONE_GB;
  static final String DISK_OK = "ok";
  static final String DISK_LOW = "warn";
  static final String DISK_FULL = "bad";

  private DataDir dataDir;
  private final HttpClient http;

  public Status status;

  public String networkRegistryURL = "";
  public boolean networkRegistry = false;

  public String networkRepositoryURL = "http://rs.gbif.org";
  public boolean networkRepository = false;

  public String networkPublicAccessURL = "";
  public String networkCheckPublicAccessURL = "https://tools.gbif.org/ws-validurl/?url=";
  public boolean networkPublicAccess = false;
  // false when the validator itself failed: then nothing is known about the IPT's public access
  public boolean networkPublicAccessChecked = false;
  public String networkPublicAccessMessage = "";
  public String networkPublicAccessStatus = "";

  public long diskTotal = 0;
  public long diskUsed = 0;
  public long diskFree = 0;
  public int diskUsedRatio = 0;
  public String diskLevel = DISK_OK;

  public boolean readConfigDir = false;
  public boolean writeConfigDir = false;
  public boolean readLogDir = false;
  public boolean writeLogDir = false;
  public boolean readTmpDir = false;
  public boolean writeTmpDir = false;
  public boolean readResourcesDir = false;
  public boolean writeResourcesDir = false;
  public boolean readSubResourcesDir = false;
  public boolean writeSubResourcesDir = false;

  public String hrDiskTotal = "";
  public String hrDiskUsed = "";
  public String hrDiskFree = "";

  public String osName = "";
  public String osVersion = "";
  public String javaVersion = "";
  public String appServerVersion = "";
  public String iptMode = "";

  @Inject
  public HealthAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      HttpClient client,
      RegistrationManager registrationManager,
      DataDir dataDir) {
    super(textProvider, cfg, registrationManager);
    this.dataDir = dataDir;
    this.http = client;
  }

  @Override
  public void prepare() {
    super.prepare();

    // Network
    try {
      networkRegistryURL = cfg.getRegistryUrl();
      ExtendedResponse resp = http.get(networkRegistryURL);
      if ((resp != null) && (resp.getStatusCode() == 200)) {
        networkRegistry = true;
      }
    } catch (Exception e) {
      // do nothing
    }

    try {
      ExtendedResponse resp = http.get(networkRepositoryURL);
      if ((resp != null) && (resp.getStatusCode() == 200)) {
        networkRepository = true;
      }
    } catch (Exception e) {
      // do nothing
    }

    // Public access
    networkPublicAccessURL = cfg.getBaseUrl();
    PublicAccessCheck check;
    try {
      String checkUrl = networkCheckPublicAccessURL + networkPublicAccessURL;
      ExtendedResponse resp = http.get(checkUrl);
      LOG.debug("Public access check: url={}, status={}, body={}",
          checkUrl, resp != null ? resp.getStatusCode() : null,
          resp != null ? resp.getContent() : null);
      check = readPublicAccessResponse(resp, networkPublicAccessURL);
    } catch (Exception e) {
      LOG.error("Public access check failed for {}", networkPublicAccessURL, e);
      check = new PublicAccessCheck(false, false, "ERROR",
          "Could not run the public access check. See logs for details.");
    }
    networkPublicAccessChecked = check.checked();
    networkPublicAccess = check.reachable();
    networkPublicAccessStatus = check.status();
    networkPublicAccessMessage = check.message();

    // Disk
    diskTotal = dataDir.getDataDirTotalSpace();
    diskFree = dataDir.getDataDirUsableSpace();
    diskUsed = diskTotal - diskFree;
    diskUsedRatio = diskTotal > 0 ? (int) (100 * diskUsed / diskTotal) : 0;
    hrDiskTotal = FileUtils.byteCountToDisplaySize(diskTotal);
    hrDiskUsed = FileUtils.byteCountToDisplaySize(diskUsed);
    hrDiskFree = FileUtils.byteCountToDisplaySize(diskFree);
    diskLevel = diskLevel(diskFree);

    // File permissions (config dir)
    DataDir.DirStatus configDirStatus = dataDir.getDirectoryReadWriteStatus(dataDir.configDir());
    readConfigDir = (configDirStatus == DataDir.DirStatus.READ_ONLY) || (configDirStatus == DataDir.DirStatus.READ_WRITE);
    writeConfigDir = (configDirStatus == DataDir.DirStatus.READ_WRITE);

    // File permissions (log dir)
    DataDir.DirStatus logDirStatus = dataDir.getDirectoryReadWriteStatus(dataDir.loggingDir());
    readLogDir = (logDirStatus == DataDir.DirStatus.READ_ONLY) || (logDirStatus == DataDir.DirStatus.READ_WRITE);
    writeLogDir = (logDirStatus == DataDir.DirStatus.READ_WRITE);

    // File permissions (tmp dir)
    DataDir.DirStatus tmpDirStatus = dataDir.getDirectoryReadWriteStatus(dataDir.tmpRootDir());
    readTmpDir = (tmpDirStatus == DataDir.DirStatus.READ_ONLY) || (tmpDirStatus == DataDir.DirStatus.READ_WRITE);
    writeTmpDir = (tmpDirStatus == DataDir.DirStatus.READ_WRITE);

    // File permissions (resources dir)
    DataDir.DirStatus resourcesDirStatus = dataDir.getDirectoryReadWriteStatus(dataDir.resourcesDir());
    readResourcesDir = (resourcesDirStatus == DataDir.DirStatus.READ_ONLY) || (resourcesDirStatus == DataDir.DirStatus.READ_WRITE);
    writeResourcesDir = (resourcesDirStatus == DataDir.DirStatus.READ_WRITE);

    // File permissions (sub resources dir)
    DataDir.DirStatus subResourcesDirStatus = dataDir.getSubDirectoriesReadWriteStatus(dataDir.resourcesDir());
    readSubResourcesDir = (subResourcesDirStatus == DataDir.DirStatus.READ_ONLY) || (subResourcesDirStatus == DataDir.DirStatus.READ_WRITE);
    writeSubResourcesDir = (subResourcesDirStatus == DataDir.DirStatus.READ_WRITE);

    // System
    osName = System.getProperty("os.name");
    osVersion = System.getProperty("os.version");
    javaVersion = Runtime.class.getPackage().getImplementationVersion();
    if (javaVersion == null) {
      javaVersion = System.getProperty("java.version");
    }
    appServerVersion = ServletActionContext.getServletContext().getServerInfo();
    iptMode = ((cfg != null) && (cfg.getRegistryType() != null)) ? cfg.getRegistryType().name() : "";
  }

  /**
   * @return "ok", "warn" (running low) or "bad" (almost full) for the usable space left on the data directory's disk
   */
  static String diskLevel(long freeBytes) {
    if (freeBytes < DISK_FULL_BYTES) {
      return DISK_FULL;
    }
    return freeBytes < DISK_LOW_BYTES ? DISK_LOW : DISK_OK;
  }

  /**
   * The validator's answer about the IPT's public URL.
   *
   * @param checked   true when the validator ran; false when it didn't answer, failed or answered something unreadable
   * @param reachable true when the validator could open the URL
   */
  record PublicAccessCheck(boolean checked, boolean reachable, String status, String message) {}

  /**
   * Reads a ws-validurl response. Only a readable answer from the validator says something about the IPT; a missing or
   * failed answer means the check couldn't run.
   */
  static PublicAccessCheck readPublicAccessResponse(ExtendedResponse resp, String publicUrl) {
    if (resp == null) {
      return new PublicAccessCheck(false, false, "NO_RESPONSE", "No response from the URL validation service.");
    }
    if (resp.getStatusCode() != 200) {
      return new PublicAccessCheck(false, false, String.valueOf(resp.getStatusCode()),
          "The URL validation service returned HTTP " + resp.getStatusCode() + ".");
    }

    JsonObject json;
    try {
      json = JsonParser.parseString(resp.getContent()).getAsJsonObject();
    } catch (RuntimeException e) {
      return new PublicAccessCheck(false, false, "BAD_RESPONSE",
          "The URL validation service returned a response that could not be read.");
    }

    JsonElement success = json.get("success");
    if (success != null && !success.isJsonNull() && success.getAsBoolean()) {
      return new PublicAccessCheck(true, true, "", "");
    }

    // the validator couldn't reach the IPT, or the IPT answered with an error status
    String status = jsonString(json, "status");
    String error = jsonString(json, "error");
    if (error.isEmpty()) {
      error = jsonString(json, "message");
    }
    String message;
    if ("NonPublicAddress".equals(error)) {
      // validurl-ws refuses localhost and private network addresses
      message = publicUrl + " points to this computer or a private network, which the internet cannot reach.";
    } else if (!status.isEmpty()) {
      message = "IPT responded to the validator with HTTP " + status + ".";
    } else if (!error.isEmpty()) {
      message = error;
    } else {
      message = "The validator could not reach " + publicUrl
          + ". Check that the base URL is correct and reachable from the public internet.";
    }
    return new PublicAccessCheck(true, false, status, message);
  }

  private static String jsonString(JsonObject json, String key) {
    JsonElement el = json.get(key);
    return (el != null && !el.isJsonNull()) ? el.getAsString() : "";
  }

  @Override
  public String execute() {
    status = new Status();
    status.setNetworkRegistryURL(this.networkRegistryURL);
    status.setNetworkRegistry(this.networkRegistry);
    status.setNetworkRepositoryURL(this.networkRepositoryURL);
    status.setNetworkRepository(this.networkRepository);
    status.setNetworkPublicAccessURL(this.networkPublicAccessURL);
    status.setNetworkPublicAccess(this.networkPublicAccess);
    status.setNetworkPublicAccessChecked(this.networkPublicAccessChecked);
    status.setNetworkPublicAccessMessage(this.networkPublicAccessMessage);
    status.setNetworkPublicAccessStatus(this.networkPublicAccessStatus);
    status.setDiskTotal(this.diskTotal);
    status.setDiskUsed(this.diskUsed);
    status.setDiskFree(this.diskFree);
    status.setDiskUsedRatio(this.diskUsedRatio);
    status.setReadConfigDir(this.readConfigDir);
    status.setWriteConfigDir(this.writeConfigDir);
    status.setReadLogDir(this.readLogDir);
    status.setWriteLogDir(this.writeLogDir);
    status.setReadTmpDir(this.readTmpDir);
    status.setWriteTmpDir(this.writeTmpDir);
    status.setReadResourcesDir(this.readResourcesDir);
    status.setWriteResourcesDir(this.writeResourcesDir);
    status.setReadSubResourcesDir(this.readSubResourcesDir);
    status.setWriteSubResourcesDir(this.writeSubResourcesDir);
    return SUCCESS;
  }

  @JSON(name = "status")
  public Status getStatus() {
    return status;
  }

  public static class Status {
    private String networkRegistryURL;
    private boolean networkRegistry;
    private String networkRepositoryURL;
    private boolean networkRepository;
    private String networkPublicAccessURL;
    private boolean networkPublicAccess;
    private boolean networkPublicAccessChecked;
    private String networkPublicAccessMessage;
    private String networkPublicAccessStatus;
    private long diskTotal;
    private long diskUsed;
    private long diskFree;
    private int diskUsedRatio;
    private boolean readConfigDir;
    private boolean writeConfigDir;
    private boolean readLogDir;
    private boolean writeLogDir;
    private boolean readTmpDir;
    private boolean writeTmpDir;
    private boolean readResourcesDir;
    private boolean writeResourcesDir;
    private boolean readSubResourcesDir;
    private boolean writeSubResourcesDir;

    public String getNetworkRegistryURL() {
      return networkRegistryURL;
    }

    public void setNetworkRegistryURL(String networkRegistryURL) {
      this.networkRegistryURL = networkRegistryURL;
    }

    public boolean isNetworkRegistry() {
      return networkRegistry;
    }

    public void setNetworkRegistry(boolean networkRegistry) {
      this.networkRegistry = networkRegistry;
    }

    public String getNetworkRepositoryURL() {
      return networkRepositoryURL;
    }

    public void setNetworkRepositoryURL(String networkRepositoryURL) {
      this.networkRepositoryURL = networkRepositoryURL;
    }

    public boolean isNetworkRepository() {
      return networkRepository;
    }

    public void setNetworkRepository(boolean networkRepository) {
      this.networkRepository = networkRepository;
    }

    public String getNetworkPublicAccessURL() {
      return networkPublicAccessURL;
    }

    public void setNetworkPublicAccessURL(String networkPublicAccessURL) {
      this.networkPublicAccessURL = networkPublicAccessURL;
    }

    public boolean isNetworkPublicAccess() {
      return networkPublicAccess;
    }

    public void setNetworkPublicAccess(boolean networkPublicAccess) {
      this.networkPublicAccess = networkPublicAccess;
    }

    public boolean isNetworkPublicAccessChecked() {
      return networkPublicAccessChecked;
    }

    public void setNetworkPublicAccessChecked(boolean networkPublicAccessChecked) {
      this.networkPublicAccessChecked = networkPublicAccessChecked;
    }

    public String getNetworkPublicAccessMessage() {
      return networkPublicAccessMessage;
    }

    public void setNetworkPublicAccessMessage(String networkPublicAccessMessage) {
      this.networkPublicAccessMessage = networkPublicAccessMessage;
    }

    public String getNetworkPublicAccessStatus() {
      return networkPublicAccessStatus;
    }

    public void setNetworkPublicAccessStatus(String networkPublicAccessStatus) {
      this.networkPublicAccessStatus = networkPublicAccessStatus;
    }

    public long getDiskTotal() {
      return diskTotal;
    }

    public void setDiskTotal(long diskTotal) {
      this.diskTotal = diskTotal;
    }

    public long getDiskUsed() {
      return diskUsed;
    }

    public void setDiskUsed(long diskUsed) {
      this.diskUsed = diskUsed;
    }

    public long getDiskFree() {
      return diskFree;
    }

    public void setDiskFree(long diskFree) {
      this.diskFree = diskFree;
    }

    public int getDiskUsedRatio() {
      return diskUsedRatio;
    }

    public void setDiskUsedRatio(int diskUsedRatio) {
      this.diskUsedRatio = diskUsedRatio;
    }

    public boolean isReadConfigDir() {
      return readConfigDir;
    }

    public void setReadConfigDir(boolean readConfigDir) {
      this.readConfigDir = readConfigDir;
    }

    public boolean isWriteConfigDir() {
      return writeConfigDir;
    }

    public void setWriteConfigDir(boolean writeConfigDir) {
      this.writeConfigDir = writeConfigDir;
    }

    public boolean isReadLogDir() {
      return readLogDir;
    }

    public void setReadLogDir(boolean readLogDir) {
      this.readLogDir = readLogDir;
    }

    public boolean isWriteLogDir() {
      return writeLogDir;
    }

    public void setWriteLogDir(boolean writeLogDir) {
      this.writeLogDir = writeLogDir;
    }

    public boolean isReadTmpDir() {
      return readTmpDir;
    }

    public void setReadTmpDir(boolean readTmpDir) {
      this.readTmpDir = readTmpDir;
    }

    public boolean isWriteTmpDir() {
      return writeTmpDir;
    }

    public void setWriteTmpDir(boolean writeTmpDir) {
      this.writeTmpDir = writeTmpDir;
    }

    public boolean isReadResourcesDir() {
      return readResourcesDir;
    }

    public void setReadResourcesDir(boolean readResourcesDir) {
      this.readResourcesDir = readResourcesDir;
    }

    public boolean isWriteResourcesDir() {
      return writeResourcesDir;
    }

    public void setWriteResourcesDir(boolean writeResourcesDir) {
      this.writeResourcesDir = writeResourcesDir;
    }

    public boolean isReadSubResourcesDir() {
      return readSubResourcesDir;
    }

    public void setReadSubResourcesDir(boolean readSubResourcesDir) {
      this.readSubResourcesDir = readSubResourcesDir;
    }

    public boolean isWriteSubResourcesDir() {
      return writeSubResourcesDir;
    }

    public void setWriteSubResourcesDir(boolean writeSubResourcesDir) {
      this.writeSubResourcesDir = writeSubResourcesDir;
    }
  }
}
