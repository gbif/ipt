package org.gbif.provider.tapir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.util.QuoteTokenizer;

public class FilterHandler{
	private static final String UNNARY_LOP = "not"; 
	private static final Map<String, Class> BINARY_LOPS; 
		  static  
		  {  
		    Map<String, Class> ops = new HashMap<String, Class>();  
		    ops.put("and", And.class);  
		    ops.put("or", Or.class);  
		    BINARY_LOPS = Collections.unmodifiableMap(ops);  
		  }  
	private static final Map<String, Class> UNNARY_COP; 
	  static  {  
		    Map<String, Class> ops = new HashMap<String, Class>();  
		    ops.put("isnull", IsNull.class);  
		    UNNARY_COP = Collections.unmodifiableMap(ops);  
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
	private QuoteTokenizer tokenizer;
	private Stack<LogicalOperator> LOPstack = new Stack<LogicalOperator>(); 

	public void parse(String filter) throws ParseException{
		tokenizer = new QuoteTokenizer(filter," ()");
		while (tokenizer.hasMoreTokens()) {
			String token;
			try {
				token = tokenizer.nextToken();
			} catch (NoSuchElementException e) {
				throw new ParseException("Filter cant be parsed. Invalid", e);
			}
			String t = token.toLowerCase();
			if (token.equals(" ")){
			}else if (token.equals("(")){
				System.out.println("Opening brackets");
			}else if (token.equals(")")){
				System.out.println("Closing brackets");
			}else if (t.equals(UNNARY_LOP)){
				System.out.println("Closing brackets");
			}else if (token.equals(")")){
				System.out.println("Closing brackets");
			}else if (token.equals(")")){
				System.out.println("Closing brackets");
			}else{
				
				System.out.println(token);
			}
		} 
	}
	
	private ComparisonOperator createCOP(String op, String concept, String ... values) {
		ComparisonOperator cop = null;
		if (UNNARY_COP.containsKey(op)){
			try {
				cop = (ComparisonOperator) UNNARY_COP.get(op).newInstance();
				
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return cop;
		
	}

}
