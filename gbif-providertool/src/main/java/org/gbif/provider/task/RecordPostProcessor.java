package org.gbif.provider.task;

import java.util.Set;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;


public interface RecordPostProcessor<IN, OUT, R extends DataResource> extends Task<OUT>{
	void prepare();
	IN processRecord(IN record) throws InterruptedException;
	void statsPerRecord(IN record) throws InterruptedException;
	OUT close(R resource);
}
