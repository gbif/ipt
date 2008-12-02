package org.gbif.provider.tapir;

public class Filter {
	private BooleanOpBase root;

	public BooleanOpBase getRoot() {
		return root;
	}

	public void setRoot(BooleanOpBase root) {
		this.root = root;
	}
	
	public String toString(){
		return root.toString();
	}
}
