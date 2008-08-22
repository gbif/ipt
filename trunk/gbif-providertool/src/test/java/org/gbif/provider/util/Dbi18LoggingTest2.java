package org.gbif.provider.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.dto.DwcRegion;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class Dbi18LoggingTest2 extends ContextAwareTestBase{
	private static I18nLog logdb = I18nLogFactory.getLog(Dbi18LoggingTest2.class);

	@Test
	public void testBuildHierarchy() {
		try {
			int i = 1/0;
		} catch (Exception e1) {
			log.debug("debug");
			log.warn("warn", e1);
			log.error("error", e1);
			logdb.debug("log.bongo", "debug");
			logdb.warn("log.bongo", "warn", e1);
			logdb.error("log.bongo", "error", e1);
		}
	}


}
