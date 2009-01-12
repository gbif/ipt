package org.gbif.provider.tapir;

public class Filter {
	private LogicalOperator root;

	public Filter(String filter) throws ParseException{
		if (filter != null && filter.startsWith("kacke")){
			throw new ParseException("Filter invalid");
		}
	}

	public LogicalOperator getRoot() {
		return root;
	}

	public void setRoot(LogicalOperator root) {
		this.root = root;
	}
	
	public String toHQL(){
		return root.toHQL();
	}
	public String toString(){
		return root.toString();
	}
}
