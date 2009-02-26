package org.gbif.provider.tapir.filter;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Filter implements Iterable<BooleanOperator>{
	protected Log log = LogFactory.getLog(this.getClass());
	private BooleanOperator root;

	public Filter() {
	}

	public BooleanOperator getRoot() {
		return root;
	}

	/**
	 * Will only set the root if it currently null
	 */
	public void setRootIfNull(BooleanOperator root) {
		if (this.root==null && root!=null) {
			log.debug("Setting root to: " + root.getClass());
			this.root = root;
		}
	}
	
	public void setRoot(BooleanOperator root) {
		this.root = root;
	}
	
	public String toHQL(){
		if (root==null){
			return "";
		}
		return root.toHQL();
	}
	public String toString(){
		String rot = "NULL";
		if (root!=null){
			rot=root.toString();
		}
		return "Filter: " + rot;
	}
	
	// needed due to the Digester based parsing
	public void addOperand(BooleanOperator operand) {
	}

	class FilterIterator implements Iterator<BooleanOperator>{
		private Iterator<BooleanOperator> iter=null;
		public FilterIterator(){
			if (root!=null){
				iter=root.iterator();
			}
		}
		public boolean hasNext() {
			if (iter==null){
				return false;
			}
			return iter.hasNext();
		}
		public BooleanOperator next() {
			return iter.next();
		}
		public void remove() {
		    throw new UnsupportedOperationException();
		}
	}
	public Iterator<BooleanOperator> iterator() {
		return new FilterIterator();
	}
}
