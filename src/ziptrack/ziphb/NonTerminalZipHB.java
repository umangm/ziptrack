package ziptrack.ziphb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import ziptrack.util.VectorClock;

public class NonTerminalZipHB extends SymbolZipHB {

	protected ArrayList<SymbolZipHB> rule;
	public boolean allTerminals; //on RHS

	protected HashSet<Integer> intermediateRelevantWrites;
	protected HashMap<Integer, HashSet<Integer>> intermediateRelevantReads; // t -> Set(relevant reads)
	protected HashSet<Integer> intermediateRelevantThreadsOrLocks;

	public HashSet<SymbolZipHB> criticalChildren;

	public NonTerminalZipHB(String name){
		super(name);
		this.rule = null;
		this.intermediateRelevantWrites = null;
		this.intermediateRelevantReads = null;
		this.intermediateRelevantThreadsOrLocks = null;
		this.criticalChildren = null;
	}

	public NonTerminalZipHB(String name, ArrayList<SymbolZipHB> r){
		super(name);
		this.rule = r;
		this.intermediateRelevantWrites = null;
		this.intermediateRelevantReads = null;
		this.intermediateRelevantThreadsOrLocks = null;
		this.criticalChildren = null;
	}

	public void setRule(ArrayList<SymbolZipHB> r){
		this.rule = r;
	}

	public void printRule(){
		for(SymbolZipHB s: this.rule){
			System.out.print(s.name + " ");
		}
	}

	@Override
	protected void countVariables(){
		this.writeCount = new HashMap<Integer, Integer> ();
		for(SymbolZipHB symb: this.rule){
			for (HashMap.Entry<Integer, Integer> entry : symb.writeCount.entrySet()){
				int x = entry.getKey();
				int x_cnt = 0;
				if(this.writeCount.containsKey(x)){
					x_cnt = this.writeCount.get(x);
				}
				this.writeCount.put(x,  x_cnt + 1);	
			}
		}

		this.readCount = new HashMap<Integer, HashMap<Integer, Integer>> ();
		for(SymbolZipHB symb: this.rule){
			for (HashMap.Entry<Integer, HashMap<Integer, Integer>> entry : symb.readCount.entrySet()){
				int t = entry.getKey();

				HashMap<Integer, Integer> read_to_cnt = entry.getValue();
				for(int x : read_to_cnt.keySet()){
					if(!this.readCount.containsKey(t)){
						this.readCount.put(t, new HashMap<Integer, Integer> ());
					}
					int x_t_cnt = 0;
					if(this.readCount.get(t).containsKey(x)){
						x_t_cnt = this.readCount.get(t).get(x);
					}
					this.readCount.get(t).put(x, x_t_cnt + 1);
				}
			}
		}
	}

	@Override
	protected void countThreadsOrLocks(){
		this.threadOrLockCount = new HashMap<Integer, Integer> ();
		for(SymbolZipHB symb: this.rule){
			for (HashMap.Entry<Integer, Integer> entry : symb.threadOrLockCount.entrySet()){
				int u = entry.getKey();
				int u_cnt = 0;
				if(this.threadOrLockCount.containsKey(u)){
					u_cnt = this.threadOrLockCount.get(u);
				}
				this.threadOrLockCount.put(u,  u_cnt + 1);
			}
		}
	}

	private static HashSet<Integer> aft_fst_bef_lst_helper(int u, HashSet<Integer> relevantTL_A, HashMap<Integer, HashSet<Integer>> input_map1, HashMap<Integer, HashSet<Integer>> input_map2){
		HashSet<Integer> rel_map_u = new HashSet<Integer> ();
		if(input_map1.containsKey(u)){
			rel_map_u.addAll(input_map1.get(u));
		}
		if(input_map2.containsKey(u)){
			HashSet<Integer> map2_u = input_map2.get(u);
			rel_map_u.addAll(map2_u);
			for(int uprime : map2_u){
				if(input_map1.containsKey(uprime)){
					rel_map_u.addAll(input_map1.get(uprime));
				}
			}
		}
		rel_map_u.retainAll(relevantTL_A);
		return rel_map_u;
	}

	public static HashMap<Integer, HashSet<Integer>> getAfterFirst(SymbolZipHB B, SymbolZipHB C, HashSet<Integer> relevantTL_A){
		HashMap<Integer, HashSet<Integer>> afterFirst_A = new HashMap<Integer, HashSet<Integer>> ();

		for(int u : relevantTL_A){
			HashSet<Integer> rel_aft_fst_A_u = aft_fst_bef_lst_helper(u,relevantTL_A, C.relevantAfterFirst, B.relevantAfterFirst);
			afterFirst_A.put(u, rel_aft_fst_A_u);
		}
		return afterFirst_A;
	}

	public static HashMap<Integer, HashSet<Integer>> getBeforeLast(SymbolZipHB B, SymbolZipHB C, HashSet<Integer> relevantTL_A){
		HashMap<Integer, HashSet<Integer>> beforeLast_A = new HashMap<Integer, HashSet<Integer>> ();

		for(int u : relevantTL_A){
			HashSet<Integer> rel_bef_lst_A_u = aft_fst_bef_lst_helper(u,relevantTL_A, B.relevantBeforeLast, C.relevantBeforeLast);
			beforeLast_A.put(u, rel_bef_lst_A_u);
		}
		return beforeLast_A;
	}

	private static <T> boolean two_dim_map_membership(HashMap<Integer, HashMap<Integer, T>> map, int key1, int key2){
		boolean contains = false;
		if(map.containsKey(key1)){
			if(map.get(key1).containsKey(key2)){
				contains = true;
			}
		}
		return contains;
	}

	private static void aft_bef_reads_helper(
			HashMap<Integer, HashMap<Integer, HashSet<Integer>>> map_to_be_filled, 
			int t, int x,  
			HashSet<Integer> relevantTL,
			HashMap<Integer, HashMap<Integer, HashSet<Integer>>> input_map1, 
			HashMap<Integer, HashSet<Integer>> helper_map1, 
			HashMap<Integer, HashMap<Integer, HashSet<Integer>>> input_map2)
	{
		boolean input_map1_has_t_x =  two_dim_map_membership(input_map1, t, x);
		if(input_map1_has_t_x){
			if(!map_to_be_filled.containsKey(t)){
				map_to_be_filled.put(t, new HashMap<Integer, HashSet<Integer>> ());
			}
			map_to_be_filled.get(t).put(x, new HashSet<Integer> (input_map1.get(t).get(x)));
			map_to_be_filled.get(t).get(x).retainAll(relevantTL);
		}
		else{
			if(two_dim_map_membership(input_map2, t, x)){
				if(!map_to_be_filled.containsKey(t)){
					map_to_be_filled.put(t, new HashMap<Integer, HashSet<Integer>> ());
				}
				HashSet<Integer> map2_t_x = input_map2.get(t).get(x);
				map_to_be_filled.get(t).put(x, new HashSet<Integer> (map2_t_x));
				for(int u: map2_t_x){
					if(helper_map1.containsKey(u)){
						map_to_be_filled.get(t).get(x).addAll(helper_map1.get(u));
					}
				}
				map_to_be_filled.get(t).get(x).retainAll(relevantTL);
			}
		}
	}

	public static HashMap<Integer, HashMap<Integer, HashSet<Integer>>> getAfterReads(SymbolZipHB B, SymbolZipHB C, HashSet<Integer> relevantTL_A, HashMap<Integer, HashSet<Integer>> relevantTR_A){

		HashMap<Integer, HashMap<Integer, HashSet<Integer>>> afterReads_A = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>> ();
		for(int t : relevantTR_A.keySet()){
			HashSet<Integer> t_reads = relevantTR_A.get(t);
			for(int x : t_reads){
				aft_bef_reads_helper(afterReads_A, t, x, relevantTL_A, C.relevantAfterReads, C.relevantAfterFirst, B.relevantAfterReads);
			}
		}
		return afterReads_A;
	}

	public static HashMap<Integer, HashMap<Integer, HashSet<Integer>>> getBeforeReads(SymbolZipHB B, SymbolZipHB C, HashSet<Integer> relevantTL_A, HashMap<Integer, HashSet<Integer>> relevantTR_A){

		HashMap<Integer, HashMap<Integer, HashSet<Integer>>> beforeReads_A = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>> ();
		for(int t : relevantTR_A.keySet()){
			HashSet<Integer> t_reads = relevantTR_A.get(t);
			for(int x : t_reads){
				aft_bef_reads_helper(beforeReads_A, t, x, relevantTL_A, B.relevantBeforeReads, B.relevantBeforeLast, C.relevantBeforeReads);
			}
		}
		return beforeReads_A;
	}

	private static void aft_bef_writes_helper(
			HashMap<Integer, HashSet<Integer>> map_to_be_filled,
			int x,  
			HashSet<Integer> relevantTL,
			HashMap<Integer, HashSet<Integer>> input_map1, 
			HashMap<Integer, HashSet<Integer>> helper_map1, 
			HashMap<Integer, HashSet<Integer>> input_map2
			){

		if(input_map1.containsKey(x)){
			map_to_be_filled.put(x, new HashSet<Integer>(input_map1.get(x)));
			map_to_be_filled.get(x).retainAll(relevantTL);
		}
		else if(input_map2.containsKey(x)){
			HashSet<Integer> map2_x = input_map2.get(x);
			map_to_be_filled.put(x, new HashSet<Integer>(map2_x));
			for(int u: map2_x){
				if(helper_map1.containsKey(u)){
					map_to_be_filled.get(x).addAll(helper_map1.get(u));
				}
			}
			map_to_be_filled.get(x).retainAll(relevantTL);
		}
	}

	public static HashMap<Integer, HashSet<Integer>> getAfterWrites(SymbolZipHB B, SymbolZipHB C, HashSet<Integer> relevantTL_A, HashSet<Integer> relevantWrites_A){
		HashMap<Integer, HashSet<Integer>> afterWrites_A = new HashMap<Integer, HashSet<Integer>> ();
		for(int x : relevantWrites_A){
			aft_bef_writes_helper(afterWrites_A, x, relevantTL_A, C.relevantAfterWrites, C.relevantAfterFirst, B.relevantAfterWrites);
		}
		return afterWrites_A;
	}

	public static HashMap<Integer, HashSet<Integer>> getBeforeWrites(SymbolZipHB B, SymbolZipHB C, HashSet<Integer> relevantTL_A, HashSet<Integer> relevantWrites_A){
		HashMap<Integer, HashSet<Integer>> beforeWrites_A = new HashMap<Integer, HashSet<Integer>> ();
		for(int x : relevantWrites_A){
			aft_bef_writes_helper(beforeWrites_A, x, relevantTL_A, B.relevantBeforeWrites, B.relevantBeforeLast, C.relevantBeforeWrites);
		}
		return beforeWrites_A;
	}

	private static boolean get_race_helper_WW(Integer x, HashMap<Integer, HashSet<Integer>> map1, HashMap<Integer, HashSet<Integer>> map2){
		boolean hasRace = false;
		if(map1.containsKey(x) && map2.containsKey(x)){
			HashSet<Integer> set1 = map1.get(x);
			HashSet<Integer> set2 = map2.get(x);
			if(Collections.disjoint(set1, set2)){
				hasRace = true;
			}

		}
		return hasRace;
	}

	private static boolean get_race_helper_RW(Integer x, Integer t, HashMap<Integer, HashMap<Integer, HashSet<Integer>>> map1, HashMap<Integer, HashSet<Integer>> map2){
		boolean hasRace = false;
		if(map1.containsKey(t)){
			if(map1.get(t).containsKey(x) && map2.containsKey(x)){
				HashSet<Integer> set1 = map1.get(t).get(x);
				HashSet<Integer> set2 = map2.get(x);
				if(Collections.disjoint(set1, set2)){
					hasRace = true;
				}
			}
		}
		return hasRace;
	}

	private static boolean get_race_helper_WR(Integer x, Integer t, HashMap<Integer, HashSet<Integer>> map1, HashMap<Integer, HashMap<Integer, HashSet<Integer>>> map2){
		boolean hasRace = false;
		if(map2.containsKey(t)){
			if(map1.containsKey(x) && map2.get(t).containsKey(x)){
				HashSet<Integer> set1 = map1.get(x);
				HashSet<Integer> set2 = map2.get(t).get(x);
				if(Collections.disjoint(set1, set2)){
					hasRace = true;
				}
			}
		}
		return hasRace;
	}

	public static boolean getRace(SymbolZipHB B, SymbolZipHB C, HashSet<Integer> B_occuring_writes, HashMap<Integer, HashSet<Integer>> B_occuring_reads){
		boolean hasRace_A = B.hasRace || C.hasRace;
		if(! hasRace_A){
			//Write-Write
			for(int x : B_occuring_writes){
				if(C.writeCount.containsKey(x)){
					hasRace_A = get_race_helper_WW(x, B.relevantAfterWrites, C.relevantBeforeWrites);
					if(hasRace_A) return hasRace_A;
				}
			}

			//Write-Read
			for(int x : B_occuring_writes){
				for(int t : C.readCount.keySet()){
					if(C.readCount.get(t).containsKey(x)){
						hasRace_A = get_race_helper_WR(x, t, B.relevantAfterWrites, C.relevantBeforeReads);
						if(hasRace_A) return hasRace_A;
					}
				}
			}

			//Read-Write
			for(int x : C.writeCount.keySet()){
				for(int t : B_occuring_reads.keySet()){
					if(B_occuring_reads.get(t).contains(x)){
						hasRace_A = get_race_helper_RW(x, t, B.relevantAfterReads, C.relevantBeforeWrites);
						if(hasRace_A) return hasRace_A;
					}
				}
			}
		}
		return hasRace_A;
	}

	private void initialize_data(){
		this.relevantAfterFirst = new HashMap<Integer, HashSet<Integer>> ();
		this.relevantBeforeLast = new HashMap<Integer, HashSet<Integer>> ();
		this.relevantAfterReads = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>> ();
		this.relevantBeforeReads = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>> ();
		this.relevantAfterWrites = new HashMap<Integer, HashSet<Integer>> ();
		this.relevantBeforeWrites = new HashMap<Integer, HashSet<Integer>> ();
		hasRace = false;
	}

	private void setIntermediateRelevantVariables(){

		this.intermediateRelevantWrites = new HashSet<Integer> ();
		for(int idx = 0; idx < this.rule.size(); idx ++){
			this.intermediateRelevantWrites.addAll(this.rule.get(idx).relevantWrites);
		}
		this.intermediateRelevantWrites.addAll(this.relevantWrites);

		this.intermediateRelevantReads = new HashMap<Integer, HashSet<Integer>> ();
		for(int idx = 0; idx < this.rule.size(); idx ++){
			for(int t: this.rule.get(idx).relevantReads.keySet()){
				if(!this.intermediateRelevantReads.containsKey(t)){
					this.intermediateRelevantReads.put(t, new HashSet<Integer> ());
				}
				this.intermediateRelevantReads.get(t).addAll(this.rule.get(idx).relevantReads.get(t));
			}
		}
		for(int t: this.relevantReads.keySet()){
			if(!this.intermediateRelevantReads.containsKey(t)){
				this.intermediateRelevantReads.put(t, new HashSet<Integer> ());
			}
			this.intermediateRelevantReads.get(t).addAll(this.relevantReads.get(t));
		}
	}

	private void setIntermediateRelevantThredsOrLocks(){
		this.intermediateRelevantThreadsOrLocks = new HashSet<Integer> ();
		for(int idx = 0; idx < this.rule.size(); idx ++){
			this.intermediateRelevantThreadsOrLocks.addAll(this.rule.get(idx).relevantThreadsOrLocks);
		}
		this.intermediateRelevantThreadsOrLocks.addAll(this.relevantThreadsOrLocks);
	}

	public void setIntermediateRelevantData(){
		if(!this.allTerminals){
			setIntermediateRelevantVariables();
			setIntermediateRelevantThredsOrLocks();
		}
	}

	private void computeData_mix(){
		this.initialize_data();
		HashMap<Integer, HashSet<Integer>> newAfterFirst;
		HashMap<Integer, HashSet<Integer>> newBeforeLast;
		HashMap<Integer, HashMap<Integer, HashSet<Integer>>> newAfterReads;
		HashMap<Integer, HashMap<Integer, HashSet<Integer>>> newBeforeReads;
		HashMap<Integer, HashSet<Integer>> newAfterWrites;
		HashMap<Integer, HashSet<Integer>> newBeforeWrites;
		int rule_size = this.rule.size();

		HashSet<Integer> relevantWritesCopy = new HashSet<Integer>(this.relevantWrites);
		HashMap<Integer, HashSet<Integer>> relevantReadsCopy = new HashMap<Integer, HashSet<Integer>>(this.relevantReads);

		this.relevantReads = new HashMap<> (this.rule.get(0).relevantReads);
		this.relevantWrites = new HashSet<> (this.rule.get(0).relevantWrites);
		this.relevantAfterFirst = new HashMap<> (this.rule.get(0).relevantAfterFirst);
		this.relevantBeforeLast = new HashMap<> (this.rule.get(0).relevantBeforeLast);
		this.relevantAfterReads = new HashMap<> (this.rule.get(0).relevantAfterReads);
		this.relevantBeforeReads = new HashMap<> (this.rule.get(0).relevantBeforeReads);
		this.relevantAfterWrites = new HashMap<> (this.rule.get(0).relevantAfterWrites);
		this.relevantBeforeWrites = new HashMap<> (this.rule.get(0).relevantBeforeWrites);

		HashSet<Integer> occuringWrites =  new HashSet<Integer> (this.rule.get(0).writeCount.keySet());
		HashMap<Integer, HashSet<Integer>> occuringReads = new HashMap<Integer, HashSet<Integer>> ();
		for(int t: this.rule.get(0).readCount.keySet()){
			occuringReads.put(t, new HashSet<Integer> (this.rule.get(0).readCount.get(t).keySet()));
		}

		for(int idx = 1; idx < rule_size; idx ++){
			SymbolZipHB symb = this.rule.get(idx);

			this.hasRace = getRace(this, symb, occuringWrites, occuringReads);
			if(this.hasRace){
				break;
			}

			occuringWrites.addAll(symb.writeCount.keySet());
			for(int t: symb.readCount.keySet()){
				if(!occuringReads.containsKey(t)){
					occuringReads.put(t, new HashSet<Integer> ());
				}
				occuringReads.get(t).addAll(symb.readCount.get(t).keySet());
			}

			newAfterFirst = getAfterFirst(this, symb, this.intermediateRelevantThreadsOrLocks);
			newBeforeLast = getBeforeLast(this, symb, this.intermediateRelevantThreadsOrLocks);
			newAfterReads = getAfterReads(this, symb, this.intermediateRelevantThreadsOrLocks, this.intermediateRelevantReads);
			newBeforeReads = getBeforeReads(this, symb, this.intermediateRelevantThreadsOrLocks, this.intermediateRelevantReads);
			newAfterWrites = getAfterWrites(this, symb, this.intermediateRelevantThreadsOrLocks, this.intermediateRelevantWrites);
			newBeforeWrites = getBeforeWrites(this, symb, this.intermediateRelevantThreadsOrLocks, this.intermediateRelevantWrites);

			this.relevantAfterFirst = newAfterFirst;
			this.relevantBeforeLast = newBeforeLast;
			this.relevantAfterReads = newAfterReads;
			this.relevantBeforeReads = newBeforeReads;
			this.relevantAfterWrites = newAfterWrites;
			this.relevantBeforeWrites = newBeforeWrites;

		}
		this.intermediateRelevantThreadsOrLocks= null;
		this.intermediateRelevantReads = null;
		this.intermediateRelevantWrites = null;

		this.relevantReads = relevantReadsCopy;
		this.relevantWrites = relevantWritesCopy;
	}

	private void computeData_allTerminals(){
		HashSet<Integer> tSet = new HashSet<Integer> ();
		int rule_size = this.rule.size();
		for(int idx = 0; idx < rule_size; idx ++){
			TerminalZipHB term = (TerminalZipHB) this.rule.get(idx);
			tSet.add(term.getThread());
			if(term.getType().isExtremeType()){
				tSet.add(term.getDecor());
			}
		}

		this.initialize_data();
		ZipHBStateRWClocks hbStateRW = new ZipHBStateRWClocks(tSet);
		for(int idx = 0; idx < rule_size; idx ++){
			TerminalZipHB term = ((TerminalZipHB) this.rule.get(idx));
			boolean race_occured = hbStateRW.HandleSub(term.getType(), term.getThread(), term.getDecor());
			this.hasRace = this.hasRace || race_occured;
			if(this.hasRace){
				break;
			}
		}
		if(!this.hasRace){
			HashMap<Integer, VectorClock> firstVC = hbStateRW.firstVC;
			HashMap<Integer, VectorClock> lastVC = hbStateRW.getLastVC(this.relevantThreadsOrLocks);

			HashMap<Integer, VectorClock> firstVC_W = hbStateRW.firstVC_W;
			HashMap<Integer, VectorClock> lastVC_W = hbStateRW.lastVC_W;

			HashMap<Integer, HashMap<Integer, VectorClock>> firstVC_t_R = hbStateRW.firstVC_t_R;
			HashMap<Integer, HashMap<Integer, VectorClock>> lastVC_t_R = hbStateRW.lastVC_t_R;

			hbStateRW = null;

			this.relevantAfterFirst = ZipHBState.computeAfterFirst(this.relevantThreadsOrLocks, firstVC, lastVC);
			this.relevantBeforeLast = ZipHBState.computeBeforeLast(this.relevantThreadsOrLocks, firstVC, lastVC);

			this.relevantAfterReads = ZipHBState.computeAfterReads(relevantThreadsOrLocks, lastVC, lastVC_t_R);
			this.relevantBeforeReads = ZipHBState.computeBeforeReads(relevantThreadsOrLocks, firstVC, firstVC_t_R);

			this.relevantAfterWrites = ZipHBState.computeAfterWrites(this.relevantThreadsOrLocks, lastVC, lastVC_W);
			this.relevantBeforeWrites = ZipHBState.computeBeforeWrites(this.relevantThreadsOrLocks, firstVC, firstVC_W);
		}
	}

	@Override
	public void computeData() {	
		if(this.allTerminals){
			computeData_allTerminals();	
		}
		else{
			computeData_mix();
		}
	}

	public void destroyCriticalChldren(){
		for(SymbolZipHB symb : this.criticalChildren){
			symb.destroy();
		}
	}

	@Override
	public void destroy() {
		this.rule = null;
		this.destroy_helper();
	}
}
