package org.gbif.provider.job;

import java.util.Map;

import org.gbif.scheduler.scheduler.Launchable;

public abstract class BaseJobLauncher<T extends Launchable> implements Launchable{

	protected T job;

	public BaseJobLauncher(T job) {
		this.job = job;
	}

	public void launch(Map<String, Object> seed) throws Exception {
		job.launch(seed);
	}

}
