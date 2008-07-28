package org.gbif.provider.job;

import org.gbif.scheduler.scheduler.Launchable;

public interface Job extends Launchable{
	public int getSourceType();
}
