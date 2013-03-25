package org.gbif.provider.util;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.mail.MethodNotSupportedException;

import org.apache.commons.lang.StringUtils;

public class QuoteTokenizer extends StringTokenizer{
	private boolean quoted = false;
	
	public QuoteTokenizer(String str) {
		this(str, " ");
	}
	public QuoteTokenizer(String str, String delim) {
		super(StringUtils.trimToEmpty(str), "\""+delim, true);
	}

	@Override
	public Object nextElement(){
		throw new IllegalAccessError();
	}

	@Override
	public String nextToken() throws NoSuchElementException{
		// NoSuchElementException()
		String token = super.nextToken();
		if (token.equals("\"")){
			quoted=true;
		}
		while(quoted){
			try {
				token+=super.nextToken();
				if (token.endsWith("\"")){
					quoted=false;
				}
			} catch (NoSuchElementException e) {
				throw new NoSuchElementException("Quoted string not closed");
			}
		}
		while(token.equals(" ")){
			token = nextToken();
		}
		return token; 
	}

	@Override
	public String nextToken(String delim) {
		throw new IllegalAccessError();
	}
	
	
}
