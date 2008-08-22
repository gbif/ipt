package org.gbif.provider.util;

public class Concurrency2 {
	/** If the Throwable is an Error, throw it; if it is a
	 *  RuntimeException return it, otherwise throw IllegalStateException
	 */
	public static RuntimeException launderThrowable(Throwable t) {
	    if (t instanceof RuntimeException)
	        return (RuntimeException) t;
	    else if (t instanceof Error)
	        throw (Error) t;
	    else
	        throw new IllegalStateException("Not unchecked", t);
	}
}
