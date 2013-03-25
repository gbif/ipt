package org.gbif.provider.model.voc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum ImageType {
	ChartByRegion(),
	ChartByTaxon(),
	ChartByRank(),
	ChartByStatus(),
	ChartByHost(),
	ChartByBasisOfRecord(),
	ChartByDateCollected(),
	CountryMapOfOccurrence();
}
