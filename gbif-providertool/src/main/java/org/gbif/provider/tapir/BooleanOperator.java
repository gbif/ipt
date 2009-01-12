package org.gbif.provider.tapir;

public interface BooleanOperator {
	public String toString();
	public String toHQL();
	public boolean evaluate();
}
