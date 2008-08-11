package org.gbif.provider.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.service.TabFileProcessManager;

public class TabFileProcessManagerImpl implements TabFileProcessManager{

	public List<String> getColumnHeaders(File file) {
		List<String> headers = new ArrayList<String>();
		headers.add("col1");
		headers.add("col2");
		headers.add("col3");
		return headers;
	}

}
