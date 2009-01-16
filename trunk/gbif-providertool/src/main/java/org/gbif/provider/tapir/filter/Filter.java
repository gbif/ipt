package org.gbif.provider.tapir.filter;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.tapir.filter.BooleanBlock.BlockIterator;
import org.gbif.provider.util.QuoteTokenizer;

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
		return root.toHQL();
	}
	public String toString(){
		return root.toString();
	}
	
	// needed due to the Digester based parsing
	public void addOperand(LogicalOperator operand) {
	}

	class FilterIterator implements Iterator<BooleanOperator>{
		private Iterator<BooleanOperator> iter;
		public FilterIterator(){
			iter=root.iterator();
		}
		public boolean hasNext() {
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
