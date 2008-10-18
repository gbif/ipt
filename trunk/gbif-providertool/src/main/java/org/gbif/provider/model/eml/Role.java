package org.gbif.provider.model.eml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Role implements Serializable{
	ORIGINATOR,
	AUTHOR,
	CONTENT_PROVIDER,
	CUSTODIAN_STEWARD,
	DISTRIBUTOR,
	EDITOR,
	METADATA_PROVIDER,
	OWNER,
	POINT_OF_CONTACT,
	PRINCIPAL_INVESTIGATOR,
	PROCESSOR,
	PUBLISHER,
	USER,
	FIELD_STATION_MANAGER,
	INFORMATION_MANAGER;
}
