package org.gbif.provider.upload;

import java.util.Set;


public interface RecordPostProcessor<IN, OUT> extends Task<OUT>{
	IN processRecord(IN record);
	OUT close();
}
