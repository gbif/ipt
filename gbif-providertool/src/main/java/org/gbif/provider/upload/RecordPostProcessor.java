package org.gbif.provider.upload;

import java.util.Set;

import org.gbif.provider.model.OccurrenceResource;


public interface RecordPostProcessor<IN, OUT> extends Task<OUT>{
	void prepare();
	IN processRecord(IN record) throws InterruptedException;
	void statsPerRecord(IN record) throws InterruptedException;
	OUT close(OccurrenceResource resource);
}
