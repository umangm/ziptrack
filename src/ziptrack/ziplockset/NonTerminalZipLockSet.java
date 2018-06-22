package ziptrack.ziplockset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


public class NonTerminalZipLockSet extends SymbolZipLockSet {

	protected ArrayList<SymbolZipLockSet> rule;

	public NonTerminalZipLockSet(String name){
		super(name);
		this.rule = null;
	}

	public NonTerminalZipLockSet(String name, ArrayList<SymbolZipLockSet> r){
		super(name);
		this.rule = r;
	}

	@Override
	public String getName(){
		return this.name;
	}

	public void setRule(ArrayList<SymbolZipLockSet> r){
		this.rule = r;
	}

	public void printRule(){
		for(SymbolZipLockSet s: this.rule){
			System.out.print(s.name + " ");
		}
	}

	private static int getKeyFromMap(HashMap<Integer, HashMap<Integer, Integer>> map, Integer t, Integer l){
		int ret = 0;
		if(map.containsKey(t)){
			if(map.get(t).containsKey(l)){
				ret = map.get(t).get(l);
			}
		}
		return ret;
	}

	private void initialize_data(){
		this.relevantOpenAcquires = new HashMap<Integer, HashMap<Integer, Integer>> ();
		this.relevantOpenReleases = new HashMap<Integer, HashMap<Integer, Integer>> ();
		this.relevantLockSet = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>> ();
		this.relevantWrittenVars = new HashSet<Integer> ();
		this.violationFound = false;
	}

	@Override
	public void computeData(){
		initialize_data();

		HashSet<Integer> intermediateLocks = new HashSet<Integer> ();

		for(int idx = 0; idx < this.rule.size(); idx ++){
			SymbolZipLockSet newSymbol = rule.get(idx);

			if(newSymbol.violationFound){
				this.violationFound = true;
				break;
			}

			this.relevantWrittenVars.addAll(newSymbol.relevantWrittenVars);

			//For those threads which are in this but not in relevantThreads of newSymbol 
			//(and hence, not in occurs of newSymbol), nothing changes.
			for(int t : newSymbol.relevantThreads){
				HashSet<Integer> additionalLocks_this = new HashSet<Integer> ();
				HashSet<Integer> additionalLocks_newSymbol = new HashSet<Integer> ();

				//openAcquires and releases
				for(int l : intermediateLocks){
					int oa_B = getKeyFromMap(this.relevantOpenAcquires, t, l);
					int or_B = getKeyFromMap(this.relevantOpenReleases, t, l);
					int oa_C = getKeyFromMap(newSymbol.relevantOpenAcquires, t, l);
					int or_C = getKeyFromMap(newSymbol.relevantOpenReleases, t, l);

					if(or_C > oa_B){
						additionalLocks_this.add(l);
					}
					if(oa_B > or_C){
						additionalLocks_newSymbol.add(l);
					}

					int oa = oa_C + Math.max(0, oa_B - or_C);
					int or = or_B + Math.max(0, or_C - oa_B);

					if(!this.relevantOpenAcquires.containsKey(t)){
						this.relevantOpenAcquires.put(t, new HashMap<Integer, Integer> ());
					}
					this.relevantOpenAcquires.get(t).put(l, oa);

					if(!this.relevantOpenReleases.containsKey(t)){
						this.relevantOpenReleases.put(t, new HashMap<Integer, Integer> ());
					}
					this.relevantOpenReleases.get(t).put(l, or);
				}

				if(this.relevantLockSet.containsKey(t)){

					//B minus C
					HashSet<Integer> vars_B_alone = new HashSet<Integer> (this.relevantLockSet.get(t).keySet());
					if(newSymbol.relevantLockSet.containsKey(t)){
						vars_B_alone.removeAll(newSymbol.relevantLockSet.get(t).keySet());
					}
					for(int x : vars_B_alone){
						this.relevantLockSet.get(t).get(x).addAll(additionalLocks_this);
					}

					//C minus B
					HashSet<Integer> vars_C_alone = new HashSet<Integer> ();
					if(newSymbol.relevantLockSet.containsKey(t)){
						vars_C_alone.addAll(newSymbol.relevantLockSet.get(t).keySet());
					}
					vars_C_alone.removeAll(this.relevantLockSet.get(t).keySet());
					for(int x : vars_C_alone){
						this.relevantLockSet.get(t).put(x, new HashSet<Integer> (newSymbol.relevantLockSet.get(t).get(x)));
						this.relevantLockSet.get(t).get(x).addAll(additionalLocks_newSymbol);
					}

					//B intersect C
					HashSet<Integer> vars_B_C = new HashSet<Integer> (this.relevantLockSet.get(t).keySet());
					if(newSymbol.relevantLockSet.containsKey(t)){
						vars_B_C.retainAll(newSymbol.relevantLockSet.get(t).keySet());
					}
					for(int x : vars_B_C){
						this.relevantLockSet.get(t).get(x).addAll(additionalLocks_this);
						HashSet<Integer> nextset = new HashSet<Integer> ();
						if(newSymbol.relevantLockSet.containsKey(t)){
							if(newSymbol.relevantLockSet.get(t).containsKey(x)){
								nextset.addAll(newSymbol.relevantLockSet.get(t).get(x));
							}
						}
						nextset.addAll(additionalLocks_newSymbol);
						this.relevantLockSet.get(t).get(x).retainAll(nextset);
					}
				}
				else if(newSymbol.relevantLockSet.containsKey(t)){
					for(int x : newSymbol.relevantLockSet.get(t).keySet()){
						if(!this.relevantLockSet.containsKey(t)){
							this.relevantLockSet.put(t, new HashMap<Integer, HashSet<Integer>> ());	
						}
						this.relevantLockSet.get(t).put(x, new HashSet<Integer> (newSymbol.relevantLockSet.get(t).get(x)));
						this.relevantLockSet.get(t).get(x).addAll(additionalLocks_newSymbol);	
					}
				}
			}

			//Check for races
			getRace(newSymbol);
			if(this.violationFound){
				break;
			}
		}

		//Now project everything to relevant sets
		if(!this.violationFound){
			projectToRelevant();
		}
	}

	private void getRace(SymbolZipLockSet newSymbol){
		for(int x: newSymbol.relevantVariables){
			if(this.relevantWrittenVars.contains(x)){
				HashSet<Integer> lockset_x = null;
				for(int t: this.relevantLockSet.keySet()){
					if(this.relevantLockSet.get(t).containsKey(x)){
						if(lockset_x == null){
							lockset_x = new HashSet<Integer> (this.relevantLockSet.get(t).get(x));
						}
						else{
							lockset_x.retainAll(this.relevantLockSet.get(t).get(x));
						}
						if(lockset_x.isEmpty()){
							this.violationFound = true;
							break;
						}
					}
				}
				assert(lockset_x != null);
				if(this.violationFound){
					break;
				}
			}
		}
	}

	private void projectToRelevant(){
		this.relevantWrittenVars.retainAll(this.relevantVariables);

		for(Iterator<Map.Entry<Integer, HashMap<Integer, Integer>>> it = this.relevantOpenAcquires.entrySet().iterator(); it.hasNext();){
			Map.Entry<Integer, HashMap<Integer, Integer>> entry = it.next();
			if(!this.relevantThreads.contains(entry.getKey())){
				it.remove();
			}
			else{
				HashMap<Integer, Integer> insideMap = entry.getValue();
				for(Iterator<Map.Entry<Integer, Integer>> insideIt = insideMap.entrySet().iterator(); insideIt.hasNext();){
					Map.Entry<Integer, Integer> insideEntry = insideIt.next();
					if(!this.relevantLocks.contains(insideEntry.getKey())){
						insideIt.remove();
					}
				}
			}
		}

		for(Iterator<Map.Entry<Integer, HashMap<Integer, Integer>>> it = this.relevantOpenReleases.entrySet().iterator(); it.hasNext();){
			Map.Entry<Integer, HashMap<Integer, Integer>> entry = it.next();
			if(!this.relevantThreads.contains(entry.getKey())){
				it.remove();
			}
			else{
				HashMap<Integer, Integer> insideMap = entry.getValue();
				for(Iterator<Map.Entry<Integer, Integer>> insideIt = insideMap.entrySet().iterator(); insideIt.hasNext();){
					Map.Entry<Integer, Integer> insideEntry = insideIt.next();
					if(!this.relevantLocks.contains(insideEntry.getKey())){
						insideIt.remove();
					}
				}
			}
		}

		for(Iterator<Map.Entry<Integer, HashMap<Integer, HashSet<Integer>>>> it = this.relevantLockSet.entrySet().iterator(); it.hasNext();){
			Map.Entry<Integer, HashMap<Integer, HashSet<Integer>>> entry = it.next();
			if(!this.relevantThreads.contains(entry.getKey())){
				it.remove();
			}
			else{
				HashMap<Integer, HashSet<Integer>> insideMap = entry.getValue();
				for(Iterator<Map.Entry<Integer, HashSet<Integer>>> insideIt = insideMap.entrySet().iterator(); insideIt.hasNext();){
					Map.Entry<Integer, HashSet<Integer>> insideEntry = insideIt.next();
					if(!this.relevantVariables.contains(insideEntry.getKey())){
						insideIt.remove();
					}
					else{
						insideEntry.getValue().retainAll(this.relevantLocks);
					}
				}
			}
		}

	}

	@Override
	protected void countThreads() {
		this.threadCount = new HashMap<Integer, Integer> ();
		for(SymbolZipLockSet symb: this.rule){
			for (HashMap.Entry<Integer, Integer> entry : symb.threadCount.entrySet()){
				int t = entry.getKey();
				int t_cnt = 0;
				if(this.threadCount.containsKey(t)){
					t_cnt = this.threadCount.get(t);
				}
				this.threadCount.put(t,  t_cnt + 1);
			}
		}
	}

	@Override
	protected void countLocks() {
		this.lockCount = new HashMap<Integer, Integer> ();
		for(SymbolZipLockSet symb: this.rule){
			for (HashMap.Entry<Integer, Integer> entry : symb.lockCount.entrySet()){
				int l = entry.getKey();
				int l_cnt = 0;
				if(this.lockCount.containsKey(l)){
					l_cnt = this.lockCount.get(l);
				}
				this.lockCount.put(l,  l_cnt + 1);
			}
		}
	}

	@Override
	protected void countVariables() {
		this.variableCount = new HashMap<Integer, Integer> ();
		for(SymbolZipLockSet symb: this.rule){
			for (HashMap.Entry<Integer, Integer> entry : symb.variableCount.entrySet()){
				int x = entry.getKey();
				int x_cnt = 0;
				if(this.variableCount.containsKey(x)){
					x_cnt = this.variableCount.get(x);
				}
				this.variableCount.put(x,  x_cnt + 1);
			}
		}
	}

}
