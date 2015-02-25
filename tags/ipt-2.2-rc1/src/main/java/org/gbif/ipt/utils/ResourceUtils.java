package org.gbif.ipt.utils;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.metadata.eml.Eml;

import java.io.File;
import java.math.BigDecimal;
import java.util.Locale;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;

public class ResourceUtils {

  protected static final Logger LOG = Logger.getLogger(ResourceUtils.class);

  /*
   * Empty constructor.
   */
  private ResourceUtils() {
  }

  /**
   * Reconstruct published version, using version's Eml file, version history, etc.
   *
   * @return published version reconstructed
   */
  public static Resource reconstructVersion(@NotNull BigDecimal version, @NotNull String shortname, @NotNull DOI doi,
    @Nullable Organisation organisation, @Nullable VersionHistory versionHistory, @Nullable File versionEmlFile) {
    Preconditions.checkNotNull(version);
    Preconditions.checkNotNull(shortname);
    Preconditions.checkNotNull(doi);

    if (organisation == null || versionHistory == null || versionEmlFile == null) {
      throw new IllegalArgumentException(
        "Failed to reconstruct resource version because not all of organisation, version history, or version eml file were provided");
    }

    // initiate new version, and set properties
    Resource resource = new Resource();
    resource.setShortname(shortname);
    resource.setEmlVersion(version);
    resource.setDoi(doi);
    resource.setOrganisation(organisation);
    resource.setStatus(versionHistory.getPublicationStatus());
    resource.setIdentifierStatus(versionHistory.getStatus());
    resource.setRecordsPublished(versionHistory.getRecordsPublished());
    resource.setLastPublished(versionHistory.getReleased());

    if (versionEmlFile.exists()) {
      Eml eml = EmlUtils.loadWithLocale(versionEmlFile, Locale.US);
      resource.setEml(eml);
    } else {
      throw new IllegalArgumentException(
        "Failed to reconstruct resource: " + versionEmlFile.getAbsolutePath() + " not found!");
    }
    return resource;
  }

}
