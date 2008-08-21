package org.gbif.provider.upload;

import java.util.Set;


public interface RecordPostProcessor<IN, OUT> extends Task<OUT>{
	void prepare();
	IN processRecord(IN record);
	OUT close();
}
