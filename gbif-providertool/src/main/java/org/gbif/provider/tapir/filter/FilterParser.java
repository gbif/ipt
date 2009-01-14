package org.gbif.provider.tapir.filter;

public interface FilterParser {
	public String literal();
	public String concept();
	public String parameter();

	public String startLOP();
	public void endLOP();
}
