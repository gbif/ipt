package org.gbif.provider.upload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.TreeNode;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.TreeNodeManager;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class NestedSetBuilderBase<T extends TreeNode<T, ?>> extends TaskBase{
	protected TreeNodeManager<T> nodeManager;
	// results
	protected Map<Integer, T> nodes;
	protected Set<T> terminalNodes;

	protected NestedSetBuilderBase(TreeNodeManager<T> nodeManager) {
		super();
		this.nodeManager = nodeManager;
	}
	
	abstract boolean logType(Enum typ);
	
	protected SortedSet<T> getSortedNodes(){
		// convert to sorted set
		SortedSet<T> taxonomy = new TreeSet<T>(nodes.values());
		return taxonomy;
	}
	
	protected SortedSet<T> setNestedSetIndices(){
		SortedSet<T> taxonomy = getSortedNodes();
		log.info(String.format("Calculating nested set indices for hierarchy with %s nodes", taxonomy.size()));
		Stack<T> parentStack = new Stack<T>();
		Long idx = 0l;
		for (T t : taxonomy){
			if (t.getType() == null){
				// dont do nothing special
			}
			else if (logType(t.getType())){
				log.debug(String.format("process %s %s", t.getType(), t.getLabel()));
			}
			// process right values for taxa on stack. But only ...
			// if stack has parents at all and if new taxon is either 
			// a) a root taxon (parent==null)
			// b) or the last stack taxon is not the parent of this taxon
			while (parentStack.size()>0 && (t.getParent() == null || !t.getParent().equals(parentStack.peek()))){
				// last taxon on the stack is not the parent. 
				// Get last taxon from stack, set rgt index and compare again
				T nonParent = parentStack.pop();
				nonParent.setRgt(idx++);				
				nodeManager.save(nonParent);
			}
			// the last taxon on stack is the parent or stack is empty. 
			// Next taxon might be a child, so dont set rgt index yet, but put onto stack
			t.setLft(idx++);
			parentStack.push(t);
			
			// flush to database from time to time
			if (idx % 100 == 0){
				nodeManager.flush();
			}
			
		}
		// finally empty the stack, assign rgt value and persist
		for (T t : parentStack){
			t.setRgt(idx++);				
			nodeManager.save(t);
		}
		
		return taxonomy;
	}
	

	
	/* Finalizes the nested set / hierarchy generation run via the processRecord handler.
	 * Creates a SortedSet of nodes, calculates + persists nested list indices and resource stats through an abstract method to be implemented.
	 * Should be called when the external iterator for the DarwinCore handler is finished.
	 * (non-Javadoc)
	 * @see org.gbif.provider.upload.RecordPostProcessor#close()
	 */
	public Set<T> close() {

		setFinalStats();
		
		// assign nested set indices for hierarchy and save each node (incl stats)
		SortedSet<T> hierarchy = setNestedSetIndices();

		occResourceManager.save(getResource());
		
		return hierarchy;		
	}
	
	
	protected abstract void setFinalStats();

	public void prepare() {
		terminalNodes = new HashSet<T>();
		nodes = new TreeMap<Integer, T>();
		nodeManager.removeAll(getResource());		
	}
	
	public String status() {
		return String.format("%s nodes", nodes.size());
	}

}
