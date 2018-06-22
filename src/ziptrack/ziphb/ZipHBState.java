package ziptrack.ziphb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import ziptrack.event.EventType;
import ziptrack.util.VectorClock;

public class ZipHBState {

	// Internal data
	protected HashMap<Integer, Integer> threadToIndex;
	protected HashSet<Integer> performerThreads;
	protected HashMap<Integer, Integer> lockToIndex;
	protected int numThreads;
	protected int numLocks;

	//These will be indexed by the original Thread and lock IDs 
	//(which do not intersect for threads and locks), 
	//and not using internal indices.
	protected HashMap<Integer, VectorClock> firstVC;

	//These will be indexed by the original variable IDs 
	//and not using internal indices.
	protected HashMap<Integer, VectorClock> firstVC_W;
	protected HashMap<Integer, HashMap<Integer, VectorClock>> firstVC_t_R; // t -> x -> VC

	protected HashMap<Integer, VectorClock> lastVC_W;
	protected HashMap<Integer, HashMap<Integer, VectorClock>> lastVC_t_R; // t -> x -> VC

	// Data used for algorithm
	protected ArrayList<Integer> clockThread;
	public ArrayList<VectorClock> HBPredecessorThread;
	public ArrayList<VectorClock> lastReleaseLock;

	public ZipHBState(HashSet<Integer> tSet) {
		initInternalData(tSet);
		initData(tSet);
	}

	protected void initInternalData(HashSet<Integer> tSet) {
		this.threadToIndex = new HashMap<Integer, Integer>();
		this.performerThreads = new HashSet<Integer> ();
		this.numThreads = 0;
		Iterator<Integer> tIter = tSet.iterator();
		while (tIter.hasNext()) {
			int thread = tIter.next();
			this.threadToIndex.put(thread, (Integer)this.numThreads);
			this.numThreads ++;
		}
		this.lockToIndex = new HashMap<Integer, Integer>();
		this.numLocks = 0;
	}

	protected void initialize1DArrayOfVectorClocksWithBottom(ArrayList<VectorClock> arr, int len) {
		for (int i = 0; i < len; i++) {
			arr.add(new VectorClock(this.numThreads));
		}
	}

	public void initData(HashSet<Integer> tSet) {
		// Initialize clockThread
		this.clockThread = new ArrayList<Integer>();
		for (int i = 0; i < this.numThreads; i++) {
			this.clockThread.add((Integer)1);
		}

		// initialize HBPredecessorThread
		this.HBPredecessorThread = new ArrayList<VectorClock>();
		initialize1DArrayOfVectorClocksWithBottom(this.HBPredecessorThread, this.numThreads);

		// initialize lastReleaseLock
		this.lastReleaseLock = new ArrayList<VectorClock>();

		//initialize firstVC
		this.firstVC = new HashMap<Integer, VectorClock> ();

		//initialize firstVC_W and lastVC_W
		this.firstVC_W = new HashMap<Integer, VectorClock> ();
		this.lastVC_W = new HashMap<Integer, VectorClock> ();

		//initialize firstVC_t_R and lastVC_t_R
		this.firstVC_t_R = new HashMap<Integer, HashMap<Integer, VectorClock>> ();
		this.lastVC_t_R = new HashMap<Integer, HashMap<Integer, VectorClock>> ();
	}

	// Access methods
	protected VectorClock getVectorClockFrom1DArray(ArrayList<VectorClock> arr, int index) {
		if (index < 0 || index >= arr.size()) {
			throw new IllegalArgumentException("Illegal Out of Bound access");
		}
		return arr.get(index);
	}

	protected int checkAndAddLock(int l){
		if(!lockToIndex.containsKey(l)){
			//System.err.println("New lock found " + this.numLocks);
			lockToIndex.put(l, this.numLocks);
			this.numLocks ++;

			lastReleaseLock.add(new VectorClock(this.numThreads));
		}
		return lockToIndex.get(l);
	}

	public int getClockThread(int t) {
		int tIndex = threadToIndex.get(t);
		return clockThread.get(tIndex);
	}

	public VectorClock generateVectorClockFromClockThread(int t) {
		int tIndex = threadToIndex.get(t);
		VectorClock pred = getVectorClock_Thread(HBPredecessorThread, t);
		VectorClock hbClock = new VectorClock(pred);
		int tValue = getClockThread(t);
		hbClock.copyFrom(pred);
		hbClock.setClockIndex(tIndex, tValue);
		return hbClock;
	}

	public void incClockThread(int t) {
		int tIndex = threadToIndex.get(t);
		int origVal = clockThread.get(tIndex);
		clockThread.set(tIndex, (Integer)(origVal + 1));
	}

	public VectorClock getVectorClock_Thread(ArrayList<VectorClock> arr, int t) {
		int tIndex = threadToIndex.get(t);
		return getVectorClockFrom1DArray(arr, tIndex);
	}

	public VectorClock getVectorClock_Lock(ArrayList<VectorClock> arr, int l) {
		int lIndex = checkAndAddLock(l);
		return getVectorClockFrom1DArray(arr, lIndex);
	}

	public void printThreadClock(){
		ArrayList<VectorClock> printVC = new ArrayList<VectorClock>();
		for(int thread : threadToIndex.keySet()){
			VectorClock C_t = generateVectorClockFromClockThread(thread);
			printVC.add(C_t);
		}
		System.out.println(printVC);
		System.out.println();
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	}

	public void printMemory(){
		System.err.println("Number of threads = " + Integer.toString(this.numThreads));
		System.err.println("Number of locks = " + Integer.toString(this.numLocks));
		//		System.err.println("Number of variables = " + Integer.toString(this.numVariables));
	}

	//Returns true if firstVC is freshly populated .
	//This means the event from where this function is called is the first event of u
	protected boolean checkAndPutFirstVC(int u, VectorClock vc){
		if(!firstVC.containsKey(u)){
			VectorClock VC_copy = new VectorClock(vc);
			firstVC.put(u, VC_copy);
			return true;
		}
		return false;
	}

	//Returns true if firstVC_RW is freshly populated .
	//This means the event from where this function is called is the first event of u
	protected void checkAndPutVC_W(int x, VectorClock vc){
		if(!firstVC_W.containsKey(x)){
			VectorClock VC_copy = new VectorClock(vc);
			firstVC_W.put(x, VC_copy);
		}
		VectorClock VC_copy = new VectorClock(vc);
		lastVC_W.put(x, VC_copy);
	}

	protected void checkAndPutVC_t_R(int t, int x, VectorClock vc){
		if(!firstVC_t_R.containsKey(t)){
			firstVC_t_R.put(t, new HashMap<Integer, VectorClock> ());
		}
		if(!lastVC_t_R.containsKey(t)){
			lastVC_t_R.put(t, new HashMap<Integer, VectorClock> ());
		}

		if(!firstVC_t_R.get(t).containsKey(x)){
			VectorClock VC_copy = new VectorClock(vc);
			firstVC_t_R.get(t).put(x, VC_copy);
		}
		VectorClock VC_copy = new VectorClock(vc);
		lastVC_t_R.get(t).put(x, VC_copy);
	}

	public boolean HandleSubAcquire(int thread, int lock) {		
		VectorClock H_t = getVectorClock_Thread(HBPredecessorThread, thread);
		VectorClock L_l = getVectorClock_Lock(lastReleaseLock, lock);				
		H_t.updateWithMax(H_t, L_l);

		VectorClock C_t = generateVectorClockFromClockThread(thread);
		checkAndPutFirstVC(thread, C_t);
		checkAndPutFirstVC(lock, C_t);
		return false;
	}

	public boolean HandleSubRelease(int thread, int lock) {
		VectorClock C_t = generateVectorClockFromClockThread(thread);				
		VectorClock L_l = getVectorClock_Lock(lastReleaseLock, lock);
		L_l.copyFrom(C_t);

		checkAndPutFirstVC(thread, C_t);

		incClockThread(thread);
		return false;
	}

	public boolean HandleSubRead(int thread, int var) {
		VectorClock C_t = generateVectorClockFromClockThread(thread);
		checkAndPutFirstVC(thread, C_t);
		return false;
	}

	public boolean HandleSubWrite(int thread, int var) {
		VectorClock C_t = generateVectorClockFromClockThread(thread);
		checkAndPutFirstVC(thread, C_t);
		return false;
	}

	public boolean HandleSubFork(int thread, int target) {
		VectorClock C_t = generateVectorClockFromClockThread(thread);			
		VectorClock H_tc = getVectorClock_Thread(HBPredecessorThread, target);
		H_tc.copyFrom(C_t);

		checkAndPutFirstVC(thread, C_t);

		incClockThread(thread);
		return false;
	}

	public boolean HandleSubJoin(int thread, int target) {
		VectorClock H_t = getVectorClock_Thread(HBPredecessorThread, thread);
		VectorClock C_tc = generateVectorClockFromClockThread(target);
		H_t.updateWithMax(H_t, C_tc);

		VectorClock C_t = generateVectorClockFromClockThread(thread);
		checkAndPutFirstVC(thread, C_t);
		boolean first_event_of_target = checkAndPutFirstVC(target, C_t);

		//Setting the last clock of target to be bottom, when this event is the last event of target.
		if(first_event_of_target){
			VectorClock H_tc = getVectorClock_Thread(HBPredecessorThread, target);
			H_tc.setToZero();
			int target_index = this.threadToIndex.get(target);
			this.clockThread.set(target_index, 0);
		}
		return false;
	}

	public boolean HandleSub(EventType tp, int thread, int decor){
		this.performerThreads.add(thread);

		boolean raceDetected = false;
		if(tp.isAcquire()) 	raceDetected = this.HandleSubAcquire(thread, decor);
		if(tp.isRelease()) 	raceDetected = this.HandleSubRelease(thread, decor);
		if(tp.isRead())		raceDetected = this.HandleSubRead(thread, decor);
		if(tp.isWrite())	raceDetected = this.HandleSubWrite(thread, decor);
		if(tp.isFork()) 	raceDetected = this.HandleSubFork(thread, decor);
		if(tp.isJoin())		raceDetected = this.HandleSubJoin(thread, decor);
		return raceDetected;
	}

	public HashMap<Integer, VectorClock> getLastVC(HashSet<Integer> relevantThreadsOrLocks){
		HashMap<Integer, VectorClock> lastVC = new HashMap<Integer, VectorClock> ();
		for(int u: relevantThreadsOrLocks){
			VectorClock u_last_vc;
			if(threadToIndex.containsKey(u)){ //u is a thread
				u_last_vc = generateVectorClockFromClockThread(u);
			}
			else{ //u is a lock
				u_last_vc = getVectorClock_Lock(lastReleaseLock, u);
			}
			if(!u_last_vc.isZero()){ // That is, lock u is atleast released once
				lastVC.put(u, u_last_vc);
			}
		}
		return lastVC;
	}

	public static HashMap<Integer, HashSet<Integer>> computeAfterFirst(HashSet<Integer> relevantThreadsOrLocks, HashMap<Integer, VectorClock> firstVC, HashMap<Integer, VectorClock> lastVC){
		HashMap<Integer, HashSet<Integer>> afterFirst = new HashMap<Integer, HashSet<Integer>> ();

		VectorClock u_first_vc, uprime_last_vc;
		for(int u: relevantThreadsOrLocks){
			if(firstVC.containsKey(u)){
				u_first_vc = firstVC.get(u);
				HashSet<Integer> after_set = new HashSet<Integer> ();
				for(int uprime: relevantThreadsOrLocks){
					if(lastVC.containsKey(uprime)){
						uprime_last_vc = lastVC.get(uprime);
						if(u_first_vc.isLessThanOrEqual(uprime_last_vc)){
							after_set.add(uprime);
						}
					}
				}
				afterFirst.put(u, after_set);
			}
		}
		return afterFirst;
	}

	public static HashMap<Integer, HashSet<Integer>> computeBeforeLast(HashSet<Integer> relevantThreadsOrLocks, HashMap<Integer, VectorClock> firstVC, HashMap<Integer, VectorClock> lastVC){
		HashMap<Integer, HashSet<Integer>> beforeLast = new HashMap<Integer, HashSet<Integer>> ();

		VectorClock u_last_vc, uprime_first_vc;
		for(int u: relevantThreadsOrLocks){
			if(lastVC.containsKey(u)){
				u_last_vc = lastVC.get(u);
				HashSet<Integer> before_set = new HashSet<Integer> ();
				for(int uprime: relevantThreadsOrLocks){
					if(firstVC.containsKey(uprime)){
						uprime_first_vc = firstVC.get(uprime);
						if(uprime_first_vc.isLessThanOrEqual(u_last_vc)){
							before_set.add(uprime);
						}
					}	
				}
				beforeLast.put(u, before_set);
			}	
		}
		return beforeLast;
	}

	public static HashMap<Integer, HashMap<Integer, HashSet<Integer>>> computeAfterReads(HashSet<Integer> relevantThreadsOrLocks, HashMap<Integer, VectorClock> lastVC, HashMap<Integer, HashMap<Integer, VectorClock>> lastVC_t_R){
		HashMap<Integer, HashMap<Integer, HashSet<Integer>>> relAftRead = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>> ();

		for (HashMap.Entry<Integer, HashMap<Integer, VectorClock>> entry : lastVC_t_R.entrySet()){
			int t = entry.getKey();
			relAftRead.put(t,  new HashMap<Integer, HashSet<Integer>>());
			HashMap<Integer, VectorClock> x_map = entry.getValue();
			for(HashMap.Entry<Integer, VectorClock> x_entry : x_map.entrySet()){
				int x = x_entry.getKey();
				VectorClock t_x_vc = x_entry.getValue();
				relAftRead.get(t).put(x, computeAfter(relevantThreadsOrLocks, lastVC, t_x_vc));
			}
		}
		return relAftRead;
	}

	public static HashMap<Integer, HashMap<Integer, HashSet<Integer>>> computeBeforeReads(HashSet<Integer> relevantThreadsOrLocks, HashMap<Integer, VectorClock> firstVC, HashMap<Integer, HashMap<Integer, VectorClock>> firstVC_t_R){
		HashMap<Integer, HashMap<Integer, HashSet<Integer>>> relBefRead = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>> ();

		for (HashMap.Entry<Integer, HashMap<Integer, VectorClock>> entry : firstVC_t_R.entrySet()){
			int t = entry.getKey();
			relBefRead.put(t,  new HashMap<Integer, HashSet<Integer>>());
			HashMap<Integer, VectorClock> x_map = entry.getValue();
			for(HashMap.Entry<Integer, VectorClock> x_entry : x_map.entrySet()){
				int x = x_entry.getKey();
				VectorClock t_x_vc = x_entry.getValue();
				relBefRead.get(t).put(x, computeBefore(relevantThreadsOrLocks, firstVC, t_x_vc));
			}
		}
		return relBefRead;
	}

	public static HashMap<Integer, HashSet<Integer>> computeAfterWrites(HashSet<Integer> relevantThreadsOrLocks, HashMap<Integer, VectorClock> lastVC, HashMap<Integer, VectorClock> lastVC_W){
		HashMap<Integer, HashSet<Integer>> relAftWrite = new HashMap<Integer, HashSet<Integer>> ();

		for(HashMap.Entry<Integer, VectorClock> entry : lastVC_W.entrySet()){
			int x = entry.getKey();
			VectorClock x_vc = entry.getValue();
			relAftWrite.put(x, computeAfter(relevantThreadsOrLocks, lastVC, x_vc));
		}
		return relAftWrite;
	}
	
	public static HashMap<Integer, HashSet<Integer>> computeBeforeWrites(HashSet<Integer> relevantThreadsOrLocks, HashMap<Integer, VectorClock> firstVC, HashMap<Integer, VectorClock> firstVC_W){
		HashMap<Integer, HashSet<Integer>> relBefWrite = new HashMap<Integer, HashSet<Integer>> ();

		for(HashMap.Entry<Integer, VectorClock> entry : firstVC_W.entrySet()){
			int x = entry.getKey();
			VectorClock x_vc = entry.getValue();
			relBefWrite.put(x, computeBefore(relevantThreadsOrLocks, firstVC, x_vc));
		}
		return relBefWrite;
	}

	public static HashSet<Integer> computeAfter(HashSet<Integer> relevantThreadsOrLocks, HashMap<Integer, VectorClock> lastVC, VectorClock VC){
		VectorClock last_vc;
		HashSet<Integer> after_set = new HashSet<Integer> ();
		for(int u: relevantThreadsOrLocks){
			if(lastVC.containsKey(u)){
				last_vc = lastVC.get(u);
				if(VC.isLessThanOrEqual(last_vc)){
					after_set.add(u);
				}
			}	
		}
		return after_set;
	}

	public static HashSet<Integer> computeBefore(HashSet<Integer> relevantThreadsOrLocks, HashMap<Integer, VectorClock> firstVC, VectorClock VC){
		VectorClock first_vc;
		HashSet<Integer> before_set = new HashSet<Integer> ();
		for(int u: relevantThreadsOrLocks){
			if(firstVC.containsKey(u)){
				first_vc = firstVC.get(u);
				if(first_vc.isLessThanOrEqual(VC)){
					before_set.add(u);
				}
			}	
		}
		return before_set;
	}
}