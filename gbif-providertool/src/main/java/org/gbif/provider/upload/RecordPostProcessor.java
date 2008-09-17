package org.gbif.provider.upload;

import java.util.Set;


public interface RecordPostProcessor<IN, OUT> extends Task<OUT>{
	void prepare();
	IN processRecord(IN record) throws InterruptedException;
	void statsPerRecord(IN record) throws InterruptedException;
	OUT close();
}
