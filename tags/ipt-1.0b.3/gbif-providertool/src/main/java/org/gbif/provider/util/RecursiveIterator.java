package org.gbif.provider.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecursiveIterator<T> implements Iterator<T>{
	private List<Iterator<T>> iters=new ArrayList<Iterator<T>>();
	private Iterator<T> currIter;
	private T self;
	private boolean returnedThis=false;
	public RecursiveIterator(T self){
		this.self=self;
	}
	public RecursiveIterator(T self, Iterator<T> iter){
		this.self=self;
		currIter = iter;
	}
	public RecursiveIterator(T self, List<Iterator<T>> iters){
		this.self=self;
		this.iters=iters;
		if (!this.iters.isEmpty()){
			currIter = this.iters.remove(0);
		}
	}
	public boolean hasNext() {
		if(!returnedThis){
			return true;
		}
		if (!iters.isEmpty()){
			return true;
		}
		if (currIter!=null){
			return currIter.hasNext();			
		}
		return false;
	}
	public T next() {
		if(!returnedThis){
			returnedThis=true;
			return self;
		}
		if (currIter.hasNext()){
			return currIter.next();
		}
		currIter = iters.remove(0);
		return currIter.next();
	}
	public void remove() {
	    throw new UnsupportedOperationException();
	}
}
