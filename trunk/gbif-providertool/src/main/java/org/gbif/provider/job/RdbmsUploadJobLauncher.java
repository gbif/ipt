package org.gbif.provider.job;


import org.gbif.scheduler.scheduler.Launchable;

public class RdbmsUploadJobLauncher extends BaseJobLauncher<RdbmsUploadJob>{
	private RdbmsUploadJobLauncher(final RdbmsUploadJob job) {
		super(job);
	}
}