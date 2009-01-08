package org.gbif.provider.tapir;

public interface BooleanOperator {
	public String toString();
	public String toSQL();
	public boolean evaluate();
}
