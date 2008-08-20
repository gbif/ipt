package org.gbif.provider.upload;


public interface RecordPostProcessor<T, R> {
	T processRecord(T record);
	R close();
}
