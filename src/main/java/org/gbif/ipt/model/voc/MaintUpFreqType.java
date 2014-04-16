package org.gbif.ipt.model.voc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Enumeration that describes the frequency with which changes and additions are made to the dataset after the initial
 * dataset is completed. The enumeration is derived from the EML specification version 2.1.1 {@link -linkoffline
 * http://knb.ecoinformatics.org/software/eml/eml-2.1.1/eml-dataset.html#maintenanceUpdateFrequency}
 */
public enum MaintUpFreqType {
  /**
   * Updated 1 time each year.
   */
  ANNUALLY("annually", 365),
  /**
   * Updated as needed. Not specific enough to use for auto-publishing.
   */
  AS_NEEDED("asNeeded", 0),
  /**
   * Updated 2 times each year.
   */
  BIANNUALLY("biannually", 182),
  /**
   * Updated continuously. Not specific enough to use for auto-publishing.
   */
  CONTINUALLY("continually", 0),
  /**
   * Updated 1 time each day.
   */
  DAILY("daily", 1),
  /**
   * Updated at irregular intervals. Not specific enough to use for auto-publishing.
   */
  IRREGULAR("irregular", 0),
  /**
   * Updated 1 time each month.
   */
  MONTHLY("monthly", 30),
  /**
   * Further updates are not planned.
   */
  NOT_PLANNED("notPlanned", 0),
  /**
   * Updated 1 time each week.
   */
  WEEKLY("weekly", 7),
  /**
   * Further updates may still happen, but it is not known for sure. Not specific enough to use for auto-publishing.
   */
  UNKNOWN("unknown", 0),
  /**
   * Updated according to some other interval. Not specific enough to use for auto-publishing.
   */
  OTHER_MAINTENANCE_PERIOD("otherMaintenancePeriod", 0);

  private final String identifier;
  private final int periodInDays;

  /**
   * Map of frequency name keys to frequency type Enumerations values used to lookup the right Enumeration for a name.
   */
  public static final Map<String, MaintUpFreqType> TYPE_LOOKUP;

  /**
   * List of frequency type Enumerations that are suitable as periods used for auto-publishing.
   */
  public static final List<String> AUTO_PUBLISHING_TYPES;

  static {
    // populate map
    Map<String, MaintUpFreqType> lookup = new HashMap<String, MaintUpFreqType>();
    lookup.put(ANNUALLY.identifier.toLowerCase(), ANNUALLY);
    lookup.put(AS_NEEDED.identifier.toLowerCase(), AS_NEEDED);
    lookup.put(BIANNUALLY.identifier.toLowerCase(), BIANNUALLY);
    lookup.put(CONTINUALLY.identifier.toLowerCase(), CONTINUALLY);
    lookup.put(DAILY.identifier.toLowerCase(), DAILY);
    lookup.put(IRREGULAR.identifier.toLowerCase(), IRREGULAR);
    lookup.put(MONTHLY.identifier.toLowerCase(), MONTHLY);
    lookup.put(NOT_PLANNED.identifier.toLowerCase(), NOT_PLANNED);
    lookup.put(WEEKLY.identifier.toLowerCase(), WEEKLY);
    lookup.put(UNKNOWN.identifier.toLowerCase(), UNKNOWN);
    lookup.put(OTHER_MAINTENANCE_PERIOD.identifier.toLowerCase(), OTHER_MAINTENANCE_PERIOD);
    TYPE_LOOKUP = ImmutableMap.copyOf(lookup);

    // populate list
    List<String> ls = new ArrayList<String>();
    ls.add(MaintUpFreqType.ANNUALLY.getIdentifier().toLowerCase());
    ls.add(MaintUpFreqType.BIANNUALLY.getIdentifier().toLowerCase());
    ls.add(MaintUpFreqType.MONTHLY.getIdentifier().toLowerCase());
    ls.add(MaintUpFreqType.WEEKLY.getIdentifier().toLowerCase());
    ls.add(MaintUpFreqType.DAILY.getIdentifier().toLowerCase());
    AUTO_PUBLISHING_TYPES = ImmutableList.copyOf(ls);
  }

  /**
   * Constructor.
   *
   * @param identifier identifier
   * @param periodInDays update frequency period in days
   */
  private MaintUpFreqType(String identifier, int periodInDays) {
    this.identifier = identifier;
    this.periodInDays = periodInDays;
  }

  /**
   * Tries its best to infer a MaintUpFreqType from a given string.
   *
   * @param type string representing some MaintUpFreqType enumeration
   *
   * @return the inferred MaintUpFreqType or null if the string could not be inferred.
   */
  public static MaintUpFreqType inferType(@Nullable String type) {
    if (type != null) {
      MaintUpFreqType maintUpFreqType = TYPE_LOOKUP.get(type.toLowerCase());
      if (maintUpFreqType != null) {
        return maintUpFreqType;
      }
    }
    return null;
  }

  /**
   * Return the Enumeration's identifier string.
   *
   * @return the Enumeration's identifier string
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Return the Enumeration's frequency update period in days. This is set to 0 if an update period is not applicable
   * to the Enumeration.
   *
   * @return frequency update period in days
   */
  public int getPeriodInDays() {
    return periodInDays;
  }
}
