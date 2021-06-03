package org.gbif.ipt.action;

import com.google.gson.*;
import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.json.annotations.JSON;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.HttpUtil;

public class HealthAction extends BaseAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(HealthAction.class);

  private DataDir dataDir;
  private HttpUtil http;

  public Status status;

  public String networkRegistryURL = "";
  public boolean networkRegistry = false;

  public String networkRepositoryURL = "http://rs.gbif.org";
  public boolean networkRepository = false;

  public String networkPublicAccessURL = "";
  public String networkCheckPublicAccessURL = "https://tools.gbif.org/ws-validurl/?url=";
  public boolean networkPublicAccess = false;

  public long diskTotal = 0;
  public long diskUsed = 0;
  public long diskFree = 0;
  public int diskUsedRatio = 0;

  public boolean readConfigDir = false;
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
  public HealthAction(SimpleTextProvider textProvider, AppConfig cfg, HttpUtil httpUtil, RegistrationManager registrationManager,
                      DataDir dataDir) {
    super(textProvider, cfg, registrationManager);
    this.dataDir = dataDir;
    this.http = httpUtil;
  }

  @Override
  public void prepare() {
    super.prepare();

    // Network
    try {
      networkRegistryURL = cfg.getRegistryUrl();
      HttpUtil.Response resp = http.get(networkRegistryURL);
      if ((resp != null) && (resp.getStatusCode() == 200)) {
        networkRegistry = true;
      }
    }
    catch (Exception e) {
      // do nothing
    }

    try {
      HttpUtil.Response resp = http.get(networkRepositoryURL);
      if ((resp != null) && (resp.getStatusCode() == 200)) {
        networkRepository = true;
      }
    }
    catch (Exception e) {
      // do nothing
    }

    try {
      networkPublicAccessURL = cfg.getBaseUrl();
      HttpUtil.Response resp = http.get(networkCheckPublicAccessURL + networkPublicAccessURL);
      if ((resp != null) && (resp.getStatusCode() == 200)) {
        JsonObject jsonObject = new JsonParser().parse(resp.content).getAsJsonObject();
        JsonElement success = jsonObject.get("success");
        if ((success != null) && success.getAsBoolean()) {
          networkPublicAccess = true;
        }
      }
    }
    catch (Exception e) {
      // do nothing
    }

    // Disk
    diskTotal = dataDir.getDataDirTotalSpace();
    diskFree = dataDir.getDataDirUsableSpace();
    diskUsed = diskTotal - diskFree;
    diskUsedRatio = (int) (100 * diskUsed / diskTotal);
    hrDiskTotal = FileUtils.byteCountToDisplaySize(diskTotal);
    hrDiskUsed = FileUtils.byteCountToDisplaySize(diskUsed);
    hrDiskFree = FileUtils.byteCountToDisplaySize(diskFree);

    // File permissions (config dir)
    DataDir.DirStatus configDirStatus = dataDir.getDirectoryReadWriteStatus(dataDir.configDir());
    readConfigDir = (configDirStatus == DataDir.DirStatus.READ_ONLY) || (configDirStatus == DataDir.DirStatus.READ_WRITE);

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
    appServerVersion = ServletActionContext.getServletContext().getServerInfo();
    iptMode = ((cfg != null) && (cfg.getRegistryType() != null)) ? cfg.getRegistryType().name() : "";
  }

  public String execute() {
    status = new Status();
    status.setNetworkRegistryURL(this.networkRegistryURL);
    status.setNetworkRegistry(this.networkRegistry);
    status.setNetworkRepositoryURL(this.networkRepositoryURL);
    status.setNetworkRepository(this.networkRepository);
    status.setNetworkPublicAccessURL(this.networkPublicAccessURL);
    status.setNetworkPublicAccess(this.networkPublicAccess);
    status.setDiskTotal(this.diskTotal);
    status.setDiskUsed(this.diskUsed);
    status.setDiskFree(this.diskFree);
    status.setDiskUsedRatio(this.diskUsedRatio);
    status.setReadConfigDir(this.readConfigDir);
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

  public class Status {
    private String networkRegistryURL;
    private boolean networkRegistry;
    private String networkRepositoryURL;
    private boolean networkRepository;
    private String networkPublicAccessURL;
    private boolean networkPublicAccess;
    private long diskTotal;
    private long diskUsed;
    private long diskFree;
    private int diskUsedRatio;
    private boolean readConfigDir;
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
