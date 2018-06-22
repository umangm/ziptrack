package ziptrack.ziplockset;

import java.util.HashSet;
import java.util.HashMap;
import ziptrack.grammar.Symbol;

public abstract class SymbolZipLockSet extends Symbol {
	protected String name;
	
	protected HashMap<Integer, Integer> threadCount;
	protected HashMap<Integer, Integer> lockCount;
	protected HashMap<Integer, Integer> variableCount;
	
	protected HashSet<Integer> relevantThreads;
	protected HashSet<Integer> relevantLocks;
	protected HashSet<Integer> relevantVariables;
	
	protected HashMap<Integer, HashMap<Integer, Integer>> relevantOpenAcquires; //Thread->Lock->int
	protected HashMap<Integer, HashMap<Integer, Integer>> relevantOpenReleases; //Thread->Lock->int
	protected HashMap<Integer, HashMap<Integer, HashSet<Integer>>> relevantLockSet; //Thread->Variables->P(Locks)
	protected HashSet<Integer> relevantWrittenVars;	
	protected boolean violationFound;
	
	public HashSet<NonTerminalZipLockSet> parents;
	public int topologicalIndex;
	
	SymbolZipLockSet(String n){
		super(n);
		this.name = n;
		init();
	}
	
	public String getName(){
		return this.name;
	}
	
	protected void init(){
		threadCount = null;
		lockCount = null;
		variableCount = null;
		
		relevantThreads = null;
		relevantLocks = null;
		relevantVariables = null;
		relevantOpenAcquires = null;
		relevantOpenReleases= null;
		relevantLockSet = null;
		relevantWrittenVars = null;
		violationFound = false;
	}
	
	private void computeRelevantVariables(){
		this.relevantVariables = new HashSet<Integer> ();
		for (HashMap.Entry<Integer, Integer> entry : this.variableCount.entrySet()){
			int x = entry.getKey();
			for(NonTerminalZipLockSet p : this.parents){
				if(p.relevantVariables.contains(x)){
					this.relevantVariables.add(x);
					break;
				}
				if(p.variableCount.get(x) >= 2){
					this.relevantVariables.add(x);
					break;
				}
			}
		}
	}
	
	private void computeRelevantThreads(){
		this.relevantThreads = new HashSet<Integer> ();
		this.relevantThreads.addAll(this.threadCount.keySet());
	}
	
	private void computeRelevantLocks(){
		this.relevantLocks = new HashSet<Integer> ();
		for (HashMap.Entry<Integer, Integer> entry : this.lockCount.entrySet()){
			int l = entry.getKey();
			for(NonTerminalZipLockSet p : this.parents){
				if(p.relevantLocks.contains(l)){
					this.relevantLocks.add(l);
					break;
				}
				if(p.lockCount.get(l) >= 2){
					this.relevantLocks.add(l);
					break;
				}
			}
		}
	}
	
	public void computeRelevantData(){
		computeRelevantThreads();
		computeRelevantLocks();
		computeRelevantVariables();
	}
	
	protected void countObjects() {
		this.countThreads();
		this.countLocks();
		this.countVariables();
	}
	
	protected abstract void countThreads();
	protected abstract void countLocks();
	protected abstract void countVariables();
	
	public abstract void computeData();
}
