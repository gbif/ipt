package org.gbif.provider.service;

import java.io.File;
import java.util.List;

public interface TabFileProcessManager {
	public List<String> getColumnHeaders(File file);
}
