package org.gbif.provider.tapir;

import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.util.QuoteTokenizer;

public class Filter {
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
		//String hql = root.toHQL();
		return "";
	}
	public String toString(){
		return root.toString();
	}
	
	// needed due to the Digester based parsing
	public void addOperand(LogicalOperator operand) {
	}
	
}
