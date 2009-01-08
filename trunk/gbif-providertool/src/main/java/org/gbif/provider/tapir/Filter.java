package org.gbif.provider.tapir;

public class Filter {
	private LogicalOperator root;

	public Filter(String filter) throws ParseException{
		if (filter.startsWith("kacke")){
			throw new ParseException("Filter invalid");
		}
	}

	public LogicalOperator getRoot() {
		return root;
	}

	public void setRoot(LogicalOperator root) {
		this.root = root;
	}
	
	public String toSQL(){
		return root.toSQL();
	}
	public String toString(){
		return root.toString();
	}
}
