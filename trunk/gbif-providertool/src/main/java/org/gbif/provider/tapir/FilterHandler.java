package org.gbif.provider.tapir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.util.QuoteTokenizer;

public class FilterHandler{
	protected Log log = LogFactory.getLog(this.getClass());
	private static final Map<String, Class> UNARY_LOP; 
	  static  {  
		    Map<String, Class> ops = new HashMap<String, Class>();  
		    ops.put("not", Not.class);  
		    UNARY_LOP = Collections.unmodifiableMap(ops);  
		  }  
	private static final Map<String, Class> BINARY_LOPS; 
		  static  
		  {  
		    Map<String, Class> ops = new HashMap<String, Class>();  
		    ops.put("and", And.class);  
		    ops.put("or", Or.class);  
		    BINARY_LOPS = Collections.unmodifiableMap(ops);  
		  }  
	private static final Map<String, Class> UNARY_COP; 
	  static  {  
		    Map<String, Class> ops = new HashMap<String, Class>();  
		    ops.put("isnull", IsNull.class);  
		    UNARY_COP = Collections.unmodifiableMap(ops);  
		  }  
	private static final Map<String, Class> UNBOUND_COP; 
	  static  {  
	    Map<String, Class> ops = new HashMap<String, Class>();  
	    ops.put("in", In.class);  
	    UNBOUND_COP = Collections.unmodifiableMap(ops);  
	  }  
	private static final Map<String, Class> BINARY_COPS; 
		  static{  
		    Map<String, Class> ops = new HashMap<String, Class>();  
		    ops.put("equals", Equals.class);  
		    ops.put("like", Like.class);  
		    ops.put("greaterthan", GreaterThan.class);  
		    ops.put("lessthan", LessThan.class);  
		    ops.put("greaterthanorequals", GreaterThanOrEquals.class);  
		    ops.put("lessthanorequals", LessThanOrEquals.class);  
		    BINARY_COPS = Collections.unmodifiableMap(ops);  
		  }
    // instance vars 
	private QuoteTokenizer tokenizer;
	private Stack<LogicalOperator> LOPstack = new Stack<LogicalOperator>();
	private Stack<ComparisonOperator> COPstack = new Stack<ComparisonOperator>();
	private Stack<String> tokenStack = new Stack<String>();
	private LinkedList<String> queuedTokens = new LinkedList<String>();
	// wrapper for the tokenizer
	private boolean hasMoreTokens(){
		if (queuedTokens.size()>0){
			return true;
		}
		return tokenizer.hasMoreTokens();
	}
	private String nextToken(){
		String tok;
		if (queuedTokens.isEmpty()){
			tok = tokenizer.nextToken();
			System.out.println("NEW TOKEN from tokenizer: '"+tok+"'");
		}else{
			tok = queuedTokens.poll();
			System.out.println("NEW TOKEN from queue: '"+tok+"'");
		}
		return tok;
	}
	public void parse(String filter) throws ParseException{
		tokenizer = new QuoteTokenizer(filter," ()");
		try{
			while (hasMoreTokens()) {
				String token;
				try {
					token = nextToken();
				} catch (NoSuchElementException e) {
					throw new ParseException("Filter cant be parsed. Invalid", e);
				}
				String t = token.toLowerCase();
				if (token.equals(" ")){
				}else if (token.equals("(")){
					System.out.println("Opening brackets");
				}else if (token.equals(")")){
					System.out.println("Closing brackets");
				}else if (UNARY_LOP.containsKey(t)){
					try {
						LogicalOperator lop = (LogicalOperator) UNARY_LOP.get(t).newInstance();
						LOPstack.add(lop);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else if (BINARY_LOPS.containsKey(t)){
					LogicalOperator lop = (LogicalOperator) BINARY_LOPS.get(t).newInstance();
					LOPstack.add(lop);
				}else if (UNARY_COP.containsKey(t)){
					ComparisonOperator cop = (ComparisonOperator) UNARY_COP.get(t).newInstance();
					cop.setProperty(getConcept(nextToken()));
					COPstack.add(cop);
				}else if (BINARY_COPS.containsKey(t)){
					ComparisonBinaryOperator cop = (ComparisonBinaryOperator) BINARY_COPS.get(t).newInstance();
					cop.setProperty(getConcept(tokenStack.pop()));
					cop.setValue(getLiteral(nextToken()));
					COPstack.add(cop);
				}else if (UNBOUND_COP.containsKey(t)){
					In cop = new In();
					cop.setProperty(getConcept(tokenStack.pop()));
					// the next thing is ugly but works fine... sorry, no time
					String tok=null;
					try{
						while (true){
							tok=nextToken();
							cop.setValue(getLiteral(tok));							
						}
					}catch (ParseException e){
						// expected to throw this error at some point when there are no literals anymore
						// put the last token back on the queue, it hasnt been properly processed yet
						queuedTokens.add(tok);
					}
					COPstack.add(cop);					
				}else if (token.equals(")")){
					System.out.println("Closing brackets");
				}else if (token.equals(")")){
					System.out.println("Closing brackets");
				}else{
					System.out.println("Queuing token: "+token);
					queuedTokens.add(token);
				}
			} 
		} catch (Exception e) {
			log.error("Filter parsing error.", e);
		}
		System.out.println(LOPstack);
		System.out.println(COPstack);
		System.out.println(tokenStack);
		System.out.println(queuedTokens);
	}
	
	private ComparisonOperator createCOP(String op, String concept, String ... values) {
		ComparisonOperator cop = null;
		if (UNARY_COP.containsKey(op)){
			try {
				cop = (ComparisonOperator) UNARY_COP.get(op).newInstance();
				
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return cop;
		
	}
	private boolean isReserved(String token){
		if (UNARY_COP.containsKey(token) || BINARY_COPS.containsKey(token) || UNARY_LOP.containsKey(token) || BINARY_LOPS.containsKey(token) || UNBOUND_COP.containsKey(token)){
			return true;
		}
		return false;
	}
	private String getLiteral(String token) throws ParseException{
		if (token.startsWith("\"") && token.endsWith("\"")){
			return token.substring(1, token.length()-1);
		}
		throw new ParseException("Literal expected, but found "+token);
	}
	private String getConcept(String token) throws ParseException{
		if (isReserved(token)){
			throw new ParseException("Reserved word found instead of concept: "+token);
		}
		return token;
	}
}
