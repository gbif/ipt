package org.gbif.provider.upload;

import java.util.concurrent.Callable;

import org.gbif.provider.model.UploadEvent;

public interface CachingTask extends Callable<UploadEvent>{
	Long getResourceId();
	String status();
}
