package org.gbif.ipt.task;

import java.util.concurrent.Callable;

public interface ReportingTask<V> extends Callable<V>{
	public String state();
}
