package org.gbif.ipt.service.manage;

public interface IdentifierService {

  /**
   * Reserves an identifier. The identifier status is set to RESERVED.
   * </p>
   * With DataCite, this is done by sending a POST request with the XML metadata (as the request body) to
   * https://mds.datacite.org/metadata. The DOI is specified inside the metadata.
   * </p>
   * With EZID, this is done by setting the "_status" metadata element to "reserved".
   */
  void reserve();

  /**
   * Mints an identifier for the first time. The identifier gets assigned a new major version such as 1.0.
   * The identifier status is set to PUBLIC. At this time the identifier will be registered with resolvers.
   * TODO: decide how the IPT auto generates its DOIs - random probably best.
   * TODO: decide whether it's possible to assign an existing DOI to a dataset
   * </p>
   * With DataCite, this is done by sending a POST request with the  DOI and URL (as the request body) to
   * https://mds.datacite.org/doi.
   * </p>
   * With EZID, this can be done by either 1) specifying a complete identifier, sending a PUT request to
   * to http://ezid.cdlib.org/id/{id} where {id} is a unique id with shoulder/namespace prefix or
   * 2) letting EZID generate it, sending a POST request to http://ezid.cdlib.org/shoulder/{shoulder} where {shoulder}
   * is the desired namespace, 3) Updating the "_status" metadata element from "reserved" to "public".
   * In either case, the user's group must have permission to create identifiers in the
   * given shoulder/namespace.
   */
  void create();

  /**
   * Get an identifier, including its metadata.
   * </p>
   * With DataCite, this is done by sending a GET request to https://mds.datacite.org/doi.
   * </p>
   * TODO: possible in EZID?
   */
  String get();

  /**
   * Get the metadata for an identifier.
   * </p>
   * With DataCite, this is done by sending a GET request to https://mds.datacite.org/metadata.
   * </p>
   * With EZID, this is done by sending a GET request to http://ezid.cdlib.org/id/{id}.
   */
  String getMetadata();

  /**
   * Delete an identifier. The identifier status is set to UNAVAILABLE. At this time the identifier will be removed
   * from any external services.
   * </p>
   * With DataCite, this is done by sending a DELETE request to https://mds.datacite.org/metadata, which inactivates
   * the DOI.
   * With EZID, this is done by setting the "_status" metadata element to "unavailable". Otherwise, the DELETE request
   * is only used to delete identifiers that have been reserved. The metadata element can be deleted in EZID by setting
   * it to null.
   */
  void delete();

  /**
   * Increments the major version number of a resource. E.g. from v1.0 to v2.0. GBIF policy is to mint a new identifier
   * for major version changes, an action that should precede calling this method.
   * </p>
   * With DataCite and EZID, this is done by updating the metadata property 'version'.
   */
  void incrementMajorVersion();

  /**
   * Increments the minor version number of a resource. E.g. from v1.0 to v1.1.
   * </p>
   * With DataCite and EZID, this is done by updating the metadata property 'version'.
   */
  void incrementMinorVersion();

  /**
   *  Updates the identifier metadata.
   *  </p>
   *  With DataCite, this is done by sending a POST request with XML metadata (as the request body) to
   *  https://mds.datacite.org/metadata.
   *  </p>
   *  With EZID, this is done by sending a POST request with metadata (as request body) to
   *  http://ezid.cdlib.org/id/{id}.
   */
  void updateMetadata();

  /**
   * Restore an identifier that has been deleted.
   * </p>
   * With DataCite, this is done by sending a POST request with XML metadata (as the request body) to
   * https://mds.datacite.org/metadata.
   * </p>
   * With EZID, this is done by switching the "_status" metadata element from "unavailable" to "public".
   */
  void restore();
}
