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
import org.gbif.ipt.service.admin.HealthService;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.RequirePost;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.Serial;
import java.util.Date;
import java.util.List;

import jakarta.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.json.annotations.JSON;

public class HealthAction extends BaseAction {

  @Serial
  private static final long serialVersionUID = 5332856472390163029L;

  private final DataDir dataDir;
  private final HealthService healthService;

  public Status status;

  // when the remote checks ran; null before the IPT is set up
  public Date checkedAt;

  public String networkRegistryURL = "";
  public boolean networkRegistry = false;

  public String networkRepositoryURL = "";
  public boolean networkRepository = false;

  public String networkPublicAccessURL = "";
  public boolean networkPublicAccess = false;
  // false when the validator itself failed: then nothing is known about the IPT's public access
  public boolean networkPublicAccessChecked = false;
  public String networkPublicAccessMessage = "";
  public String networkPublicAccessStatus = "";

  public long diskTotal = 0;
  public long diskUsed = 0;
  public long diskFree = 0;
  public int diskUsedRatio = 0;
  public String diskLevel = "ok";
  // admins: what sets the disk level
  public String hrPublishingNeed = "";
  public String largestResource = "";

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

  // for admins fixing permissions; set only for them and not part of the /api/health JSON
  public List<String> problemResourceDirs = List.of();
  public String dataDirPath = "";
  public String systemUser = "";

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
      RegistrationManager registrationManager,
      DataDir dataDir,
      HealthService healthService) {
    super(textProvider, cfg, registrationManager);
    this.dataDir = dataDir;
    this.healthService = healthService;
    setHealthService(healthService);
  }

  /**
   * Runs the remote checks again whatever their age: admins only, POST only (each run calls GBIF). Others are sent
   * to the page, which reuses checks from the last few minutes.
   */
  @RequirePost
  public String check() {
    if (isAdminRights()) {
      healthService.refreshRemoteChecks(true);
    }
    return SUCCESS;
  }

  private void load() {
    // Network
    HealthService.RemoteChecks remote = healthService.refreshRemoteChecks(false);
    if (remote != null) {
      checkedAt = new Date(remote.checked());
      networkRegistryURL = remote.registryUrl();
      networkRegistry = remote.registry();
      networkRepositoryURL = remote.repositoryUrl();
      networkRepository = remote.repository();
      networkPublicAccessURL = remote.publicAccessUrl();
      networkPublicAccessChecked = remote.publicAccessChecked();
      networkPublicAccess = remote.publicAccess();
      networkPublicAccessStatus = remote.publicAccessStatus();
      networkPublicAccessMessage = remote.publicAccessMessage();
    }

    // Disk
    HealthService.LocalChecks local = healthService.checkLocal();
    diskTotal = local.diskTotal();
    diskFree = local.diskFree();
    diskUsed = diskTotal - diskFree;
    diskUsedRatio = diskTotal > 0 ? (int) (100 * diskUsed / diskTotal) : 0;
    hrDiskTotal = FileUtils.byteCountToDisplaySize(diskTotal);
    hrDiskUsed = FileUtils.byteCountToDisplaySize(diskUsed);
    hrDiskFree = FileUtils.byteCountToDisplaySize(diskFree);
    diskLevel = local.diskLevel();
    if (isAdminRights()) {
      hrPublishingNeed = FileUtils.byteCountToDisplaySize(local.publishingNeed());
      largestResource = StringUtils.defaultString(local.largestResource());
    }

    // File permissions
    readConfigDir = readable(local.config());
    writeConfigDir = local.config() == DataDir.DirStatus.READ_WRITE;
    readLogDir = readable(local.log());
    writeLogDir = local.log() == DataDir.DirStatus.READ_WRITE;
    readTmpDir = readable(local.tmp());
    writeTmpDir = local.tmp() == DataDir.DirStatus.READ_WRITE;
    readResourcesDir = readable(local.resources());
    writeResourcesDir = local.resources() == DataDir.DirStatus.READ_WRITE;
    readSubResourcesDir = readable(local.subResources());
    writeSubResourcesDir = local.subResources() == DataDir.DirStatus.READ_WRITE;

    // resource folder names include private resources, and paths and the user are server details
    if (isAdminRights()) {
      if (!writeSubResourcesDir) {
        problemResourceDirs = dataDir.getSubDirectoriesNotReadWrite(dataDir.resourcesDir());
      }
      dataDirPath = dataDir.getDataDir().getAbsolutePath();
      systemUser = System.getProperty("user.name", "");
    }

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

  private static boolean readable(DataDir.DirStatus status) {
    return status == DataDir.DirStatus.READ_ONLY || status == DataDir.DirStatus.READ_WRITE;
  }

  @Override
  public String execute() {
    load();
    status = new Status();
    status.setLevel(healthService.getLevel().css());
    status.setNetworkRegistryURL(this.networkRegistryURL);
    status.setNetworkRegistry(this.networkRegistry);
    status.setNetworkRepositoryURL(this.networkRepositoryURL);
    status.setNetworkRepository(this.networkRepository);
    status.setNetworkPublicAccessURL(this.networkPublicAccessURL);
    status.setNetworkPublicAccess(this.networkPublicAccess);
    status.setNetworkPublicAccessChecked(this.networkPublicAccessChecked);
    status.setDiskLevel(this.diskLevel);
    // the health page shows these to admins only; left out of the JSON for others
    if (isAdminRights()) {
      status.setNetworkPublicAccessMessage(this.networkPublicAccessMessage);
      status.setNetworkPublicAccessStatus(this.networkPublicAccessStatus);
      status.setDiskTotal(this.diskTotal);
      status.setDiskUsed(this.diskUsed);
      status.setDiskFree(this.diskFree);
      status.setDiskUsedRatio(this.diskUsedRatio);
    }
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
    // "ok", "warn" or "bad": the health icon's state for admins
    private String level;
    private String networkRegistryURL;
    private boolean networkRegistry;
    private String networkRepositoryURL;
    private boolean networkRepository;
    private String networkPublicAccessURL;
    private boolean networkPublicAccess;
    private boolean networkPublicAccessChecked;
    private String networkPublicAccessMessage;
    private String networkPublicAccessStatus;
    // "ok", "warn" (room to publish the largest resource only once more) or "bad"
    private String diskLevel;
    // null (left out) for anyone but an admin
    private Long diskTotal;
    private Long diskUsed;
    private Long diskFree;
    private Integer diskUsedRatio;
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

    public String getLevel() {
      return level;
    }

    public void setLevel(String level) {
      this.level = level;
    }

    public String getDiskLevel() {
      return diskLevel;
    }

    public void setDiskLevel(String diskLevel) {
      this.diskLevel = diskLevel;
    }

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

    public Long getDiskTotal() {
      return diskTotal;
    }

    public void setDiskTotal(Long diskTotal) {
      this.diskTotal = diskTotal;
    }

    public Long getDiskUsed() {
      return diskUsed;
    }

    public void setDiskUsed(Long diskUsed) {
      this.diskUsed = diskUsed;
    }

    public Long getDiskFree() {
      return diskFree;
    }

    public void setDiskFree(Long diskFree) {
      this.diskFree = diskFree;
    }

    public Integer getDiskUsedRatio() {
      return diskUsedRatio;
    }

    public void setDiskUsedRatio(Integer diskUsedRatio) {
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
