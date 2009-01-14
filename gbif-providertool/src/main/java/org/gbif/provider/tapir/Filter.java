package org.gbif.provider.tapir;

import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Filter {
	protected Log log = LogFactory.getLog(this.getClass());
	private BooleanOperator root;

	public Filter() {
	}

	public Filter(String filter) throws ParseException{
		parse(filter);
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
	
	/** Parses filters used in KVP requests according to TAPIR 1.0 specification
	 * @See http://www.tdwg.org/dav/subgroups/tapir/1.0/docs/TAPIRSpecification_2008-09-18.html#toc72
	 * @param filter the string encoded filter to parse
	 */
	private void parse(String filter) throws ParseException{
		QuoteTokenizer tokenizer = new QuoteTokenizer(filter," ()");
		Stack<BooleanOperator> stack = new Stack<BooleanOperator>(); 
		while (tokenizer.hasMoreTokens()) {
			String token;
			try {
				token = tokenizer.nextToken();
			} catch (NoSuchElementException e) {
				throw new ParseException("Cannot tokenize filter", e);
			}
			if (token.equals(" ")){
			}else if (token.equals("(")){
				System.out.println("Opening brackets");
			}else if (token.equals(")")){
				System.out.println("Closing brackets");
			}else{
				System.out.println(token);
			}
		} 
	}
}
