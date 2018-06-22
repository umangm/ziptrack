package ziptrack.ziphb;

import java.util.HashMap;
import java.util.HashSet;

import ziptrack.event.EventType;

public class TerminalZipHB extends SymbolZipHB {

	protected EventType type;
	protected int thread;
	protected int decor;

	public boolean allParentsNative;

	public TerminalZipHB(String name, EventType tp, int th, int dec){
		super(name);
		this.type = tp;
		this.thread = th;
		this.decor = dec;
		this.allParentsNative = false;
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
	protected void countVariables(){
		this.writeCount = new HashMap<Integer, Integer> ();
		if(this.getType().isWrite()){
			this.writeCount.put(this.getDecor(), 1);
		}
		this.readCount = new HashMap<Integer, HashMap<Integer, Integer>> ();
		if(this.getType().isRead()){
			this.readCount.put(this.getThread(), new HashMap<Integer, Integer> ());
			this.readCount.get(this.getThread()).put(this.getDecor(), 1);
		}
	}

	@Override
	protected void countThreadsOrLocks(){
		this.threadOrLockCount = new HashMap<Integer, Integer> ();
		this.threadOrLockCount.put(this.getThread(), 1);
		if(this.getType().isLockType() || this.getType().isExtremeType()){
			this.threadOrLockCount.put(this.getDecor(), 1);
		}
	}

	public void computeAfterFirst(){
		this.relevantAfterFirst = new HashMap<Integer, HashSet<Integer>> ();

		boolean thread_relevant = this.relevantThreadsOrLocks.contains(this.getThread());
		boolean decor_relevant = false;
		if(this.getType().isLockType() || this.getType().isExtremeType()){
			decor_relevant = this.relevantThreadsOrLocks.contains(this.getDecor());
		}

		//Release
		if (this.getType().isRelease()){
			if(thread_relevant){
				HashSet<Integer> setThLk = new HashSet<Integer> ();
				setThLk.add(this.getThread());
				if(decor_relevant){
					setThLk.add(this.getDecor());
				}
				this.relevantAfterFirst.put(this.getThread(), setThLk);
			}
		}
		//Acquire
		else if(this.getType().isAcquire()){
			if(thread_relevant){
				HashSet<Integer> setThLk = new HashSet<Integer> ();
				setThLk.add(this.getThread());
				this.relevantAfterFirst.put(this.getThread(), setThLk);
				if(decor_relevant){
					this.relevantAfterFirst.put(this.getDecor(), new HashSet<Integer> (setThLk));
				}
			}
		}
		//Fork
		else if(this.getType().isFork()){
			if(thread_relevant){
				HashSet<Integer> setThLk = new HashSet<Integer> ();
				setThLk.add(this.getThread());
				if(decor_relevant){
					setThLk.add(this.getDecor());
				}
				this.relevantAfterFirst.put(this.getThread(), setThLk);
			}
		}
		//Join
		else if(this.getType().isJoin()){
			if(thread_relevant){
				HashSet<Integer> setThLk = new HashSet<Integer> ();
				setThLk.add(this.getThread());
				this.relevantAfterFirst.put(this.getThread(), setThLk);
				if(decor_relevant){
					this.relevantAfterFirst.put(this.getDecor(), new HashSet<Integer> (setThLk));
				}
			}
		}
		else{
			if(thread_relevant){
				HashSet<Integer> setThLk = new HashSet<Integer> ();
				setThLk.add(this.getThread());
				this.relevantAfterFirst.put(this.getThread(), setThLk);
			}
		}
	}

	public void computeBeforeLast(){
		this.relevantBeforeLast = new HashMap<Integer, HashSet<Integer>> ();

		boolean thread_relevant = this.relevantThreadsOrLocks.contains(this.getThread());
		boolean decor_relevant = false;
		if(this.getType().isLockType() || this.getType().isExtremeType()){
			decor_relevant = this.relevantThreadsOrLocks.contains(this.getDecor());
		}

		//Acquire
		if (this.getType().isAcquire()){
			if(thread_relevant){
				HashSet<Integer> setThLk = new HashSet<Integer> ();
				setThLk.add(this.getThread());
				if(decor_relevant){
					setThLk.add(this.getDecor());
				}
				this.relevantBeforeLast.put(this.getThread(), setThLk);
			}
		}
		//Release
		else if(this.getType().isRelease()){
			if(thread_relevant){
				HashSet<Integer> setThLk = new HashSet<Integer> ();
				setThLk.add(this.getThread());
				this.relevantBeforeLast.put(this.getThread(), setThLk);
				if(decor_relevant){
					this.relevantBeforeLast.put(this.getDecor(), new HashSet<Integer> (setThLk));
				}
			}
		}
		//Join
		else if(this.getType().isJoin()){
			if(thread_relevant){
				HashSet<Integer> setThLk = new HashSet<Integer> ();
				setThLk.add(this.getThread());
				if(decor_relevant){
					setThLk.add(this.getDecor());
				}
				this.relevantBeforeLast.put(this.getThread(), setThLk);
			}
		}
		//Fork
		else if(this.getType().isFork()){
			if(thread_relevant){
				HashSet<Integer> setThLk = new HashSet<Integer> ();
				setThLk.add(this.getThread());
				this.relevantBeforeLast.put(this.getThread(), setThLk);
				if(decor_relevant){
					this.relevantBeforeLast.put(this.getDecor(), new HashSet<Integer> (setThLk));
				}
			}
		}
		else{
			if(thread_relevant){
				HashSet<Integer> setThLk = new HashSet<Integer> ();
				setThLk.add(this.getThread());
				this.relevantBeforeLast.put(this.getThread(), setThLk);
			}
		}
	}
	
	private void aft_bef_reads_helper(HashMap<Integer, HashMap<Integer, HashSet<Integer>>> map_to_be_filled){
		if(this.getType().isRead()){
			int t = this.getThread();
			int x = this.getDecor();
			if(this.relevantReads.containsKey(t)){
				if(this.relevantReads.get(t).contains(x)){
					map_to_be_filled.put(t, new HashMap<Integer, HashSet<Integer>> ());
					HashSet<Integer> singletonSet = new HashSet<Integer> ();
					singletonSet.add(t);
					map_to_be_filled.get(t).put(x, singletonSet);
				}
			}
		}
	}

	public void computeAfterReads(){
		this.relevantAfterReads = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>> ();
		aft_bef_reads_helper(this.relevantAfterReads);
	}

	public void computeBeforeReads(){
		this.relevantBeforeReads = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>> ();
		aft_bef_reads_helper(this.relevantBeforeReads);
	}
	
	private void aft_bef_writes_helper(HashMap<Integer, HashSet<Integer>> map_to_be_filled){
		if(this.getType().isWrite()){
			int t = this.getThread();
			int x = this.getDecor();
			if(this.relevantWrites.contains(x)){
				if(this.relevantThreadsOrLocks.contains(t)){
					HashSet<Integer> singletonSet = new HashSet<Integer> ();
					singletonSet.add(t);
					map_to_be_filled.put(x, singletonSet);
				}
			}
		}
	}

	public void computeAfterWrites(){
		this.relevantAfterWrites = new HashMap<Integer, HashSet<Integer>> ();
		aft_bef_writes_helper(this.relevantAfterWrites);
	}

	public void computeBeforeWrites(){
		this.relevantBeforeWrites = new HashMap<Integer, HashSet<Integer>> ();
		aft_bef_writes_helper(this.relevantBeforeWrites);
	}

	public void computeRace(){
		this.hasRace = false;
	}

	@Override
	public void computeData() {
		this.computeAfterFirst();
		this.computeBeforeLast();
		this.computeAfterReads();
		this.computeBeforeReads();
		this.computeAfterWrites();
		this.computeBeforeWrites();
		this.computeRace();
	}

	@Override
	public void destroy() {
		this.destroy_helper();
	}
}
