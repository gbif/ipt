/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.JdbcSupport;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.model.Source.SqlSource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.manage.SourceManager;

import com.google.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author markus
 * 
 */
public class SourceAction extends ManagerBaseAction {
	@Inject
	private SourceManager sourceManager;
	@Inject
	private DataDir dataDir;
	@Inject
	private JdbcSupport jdbcSupport;
	// config
	private Source source;
	private String rdbms;
	private String problem;
	// file upload
	private File file;
	private String fileContentType;
	private String fileFileName;
	private boolean analyze = false;

	public String getProblem() {
		return problem;
	}

	public String add() throws IOException {
		// new one
		if (file != null) {
			// uploaded a new file
			// create a new file source
			boolean replaced=resource.getSource(fileFileName) != null;
			try {
				source = sourceManager.add(resource, file, fileFileName);
				saveResource();
				id=source.getName();
				if (replaced) {
					addActionMessage("Replacing existing source " + source.getName());
				} else {
					addActionMessage("Added a new file source");
				}
			} catch (ImportException e) {
				// even though we have problems with this source we'll keep it for manual corrections
				log.error("Source error: " + e.getMessage(), e);
				addActionError("Source error: " + e.getMessage());
			}
		}
		return INPUT;
	}

	@Override
	public String delete() {
		if (sourceManager.delete(resource, resource.getSource(id))) {
			addActionMessage("Deleted source " + id);
			saveResource();
		} else {
			addActionMessage("Couldnt delete source " + id);
		}
		return SUCCESS;
	}

	public FileSource getFileSource() {
		if (source != null && source instanceof FileSource) {
			return (FileSource) source;
		}
		return null;
	}

	public Map<String, String> getJdbcOptions() {
		return jdbcSupport.optionMap();
	}

	public Source getSource() {
		return source;
	}

	public SqlSource getSqlSource() {
		if (source != null && source instanceof SqlSource) {
			return (SqlSource) source;
		}
		return null;
	}

	@Override
	public void prepare() throws Exception {
		super.prepare();
		if (id != null) {
			source = resource.getSource(id);
		} else if (file == null) {
			// prepare a new, empty sql source
			source = new Source.SqlSource();
			source.setResource(resource);
			((SqlSource) source).setRdbms(jdbcSupport.get("mysql"));
		} else {
			// prepare a new, empty sql source
			source = new Source.FileSource();
			source.setResource(resource);
		}
	}

	@Override
	public String save() throws IOException {
		// treat jdbc special
		String result = INPUT;
		if (source != null && rdbms != null) {
			((SqlSource) source).setRdbms(jdbcSupport.get(rdbms));
		}
		// existing source
		if (id != null && source!=null) {
			if (this.analyze || !source.isReadable()) {
				problem = sourceManager.analyze(source);
			} else {
				result = SUCCESS;
			}
		} else {
			// new one
			if (file != null) {
				// uploaded a new file
				// create a new file source
				try {
					source = sourceManager.add(resource, file, fileFileName);
					if (resource.getSource(source.getName()) != null) {
						addActionMessage("Replacing existing source " + source.getName());
					} else {
						addActionMessage("Added a new file source");
					}
				} catch (ImportException e) {
					// even though we have problems with this source we'll keep it for manual corrections
					log.error("Source error: " + e.getMessage(), e);
					addActionError("Source error: " + e.getMessage());
				}
			} else {
				try {
					resource.addSource(source, false);
				} catch (AlreadyExistingException e) {
					// shouldnt really happen as we validate this beforehand - still catching it here to be safe
					addActionError("Source with that name exists already");
				}
			}
		}
		saveResource();
		return result;
	}

	public void setAnalyze(String analyze) {
		if (StringUtils.trimToNull(analyze) != null) {
			this.analyze = true;
		}
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}

	public void setFileFileName(String fileFileName) {
		this.fileFileName = fileFileName;
	}

	public void setRdbms(String jdbc) {
		this.rdbms = jdbc;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	@Override
	public void validateHttpPostOnly() {
		if (source != null) {
			// ALL SOURCES
			// check if title exists already as a source
			if (StringUtils.trimToEmpty(source.getName()).length() < 3) {
				addFieldError("source.name", getText("validation.required", new String[]{getText("source.name")}));
			} else if (id == null && resource.getSources().contains(source)) {
				addFieldError("source.name", getText("manage.source.unique"));
			}
			if (SqlSource.class.isInstance(source)) {
				// SQL SOURCE
				SqlSource src = (SqlSource) source;
				// pure ODBC connections need only a DSN, no server
				if (rdbms != null && !rdbms.equalsIgnoreCase("odbc") && StringUtils.trimToEmpty(src.getHost()).length() < 2) {
					addFieldError("sqlSource.host", getText("validation.required", new String[]{getText("sqlSource.host")}));
				}
				if (StringUtils.trimToEmpty(src.getDatabase()).length() < 2) {
					addFieldError("sqlSource.database", getText("validation.required", new String[]{getText("sqlSource.database")}));
				}
			} else {
				// FILE SOURCE
				FileSource src = (FileSource) source;
			}
		}
	}
}
