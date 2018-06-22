package ziptrack.ziplockset;

import java.util.HashMap;
import java.util.HashSet;
import ziptrack.event.EventType;

public class TerminalZipLockSet extends SymbolZipLockSet {

	protected EventType type;
	protected int thread;
	protected int decor;

	public TerminalZipLockSet(String name, EventType tp, int th, int dec){
		super(name);
		this.type = tp;
		this.thread = th;
		this.decor = dec;
	}

	public String toEventString(String tname, String decorName){
		return "<" + tname+ "," + this.type.toString() + "(" + decorName + ")>";
	}

	public String toEventString(){
		return "<" + this.thread+ "," + this.type.toString() + "(" + this.decor + ")>";
	}

	public EventType getType(){
		return this.type;
	}

	public int getThread(){
		return this.thread;
	}

	public int getDecor(){
		return this.decor;
	}

	@Override
	public String getName(){
		return "T-" + this.name;
	}

	@Override
	protected void countThreads() {
		this.threadCount = new HashMap<Integer, Integer> ();
		this.threadCount.put(this.getThread(), 1);
	}

	@Override
	protected void countLocks() {
		this.lockCount = new HashMap<Integer, Integer> ();
		if(this.getType().isLockType()){
			this.lockCount.put(this.getDecor(), 1);
		}
	}

	@Override
	protected void countVariables() {
		this.variableCount = new HashMap<Integer, Integer> ();
		if(this.getType().isAccessType()){
			this.variableCount.put(this.getDecor(), 1);
		}
	}

	@Override
	public void computeData() {
		this.violationFound = false;
		this.relevantWrittenVars = new HashSet<Integer> ();
		this.relevantOpenAcquires = new HashMap<Integer, HashMap<Integer, Integer>> ();
		this.relevantOpenReleases = new HashMap<Integer, HashMap<Integer, Integer>> ();
		this.relevantLockSet = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>> ();

		if(this.getType().isWrite()){
			int x = this.getDecor();
			if(this.relevantVariables.contains(x)){
				this.relevantWrittenVars.add(x);
			}
		}

		int t = this.getThread();
		if(this.relevantThreads.contains(t)){

			//open Acquires
			if(this.getType().isAcquire() && this.relevantLocks.contains(this.getDecor())){
				this.relevantOpenAcquires.put(t, new HashMap<Integer, Integer> ());
				this.relevantOpenAcquires.get(t).put(this.getDecor(), 1);
			}

			//open Releases
			if(this.getType().isRelease() && this.relevantLocks.contains(this.getDecor())){
				this.relevantOpenReleases.put(t, new HashMap<Integer, Integer> ());
				this.relevantOpenReleases.get(t).put(this.getDecor(), 1);
			}

			//lockset
			if(this.getType().isAccessType() && this.relevantVariables.contains(this.getDecor())){
				this.relevantLockSet.put(t, new HashMap<Integer, HashSet<Integer>> ());
				this.relevantLockSet.get(t).put(this.getDecor(), new HashSet<Integer> ());
				this.relevantLockSet.get(t).get(this.getDecor()).add(t);
			}
		}
	}

}
