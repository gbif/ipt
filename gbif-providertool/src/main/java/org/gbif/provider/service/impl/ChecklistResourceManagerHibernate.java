package org.gbif.provider.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.gbif.provider.geo.GeoserverUtils;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccStatByRegionAndTaxon;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.GChartBuilder;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;

import com.googlecode.gchartjava.GeographicalArea;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ChecklistResourceManagerHibernate extends DataResourceManagerHibernate<ChecklistResource> implements ChecklistResourceManager{

	public ChecklistResourceManagerHibernate() {
		super(ChecklistResource.class);
	}
	
}
