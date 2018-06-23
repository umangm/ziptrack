package ziptrack.ziphb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class ZipHBEngine {

	// For every symbol symb in the grammar, construct a 
	// map symb.parents : non-terminal -> integer
	// such that symb.parents(nt) = # of occurrences of symb 
	// in the unique production rule of nt.
	private static void assignParents(HashMap<String, TerminalZipHB> terminalMap, 
			HashMap<String, NonTerminalZipHB> nonTerminalMap, SymbolZipHB start){
		
		for (HashMap.Entry<String, TerminalZipHB> entry : terminalMap.entrySet()){
			entry.getValue().parents = new HashMap<NonTerminalZipHB, Integer> ();
		}
		for (HashMap.Entry<String, NonTerminalZipHB> entry : nonTerminalMap.entrySet()){
			entry.getValue().parents = new HashMap<NonTerminalZipHB, Integer> ();
		}
		for (HashMap.Entry<String, NonTerminalZipHB> entry : nonTerminalMap.entrySet()){
			NonTerminalZipHB nt = entry.getValue();
			for(SymbolZipHB symb: nt.rule){
				if(!symb.parents.containsKey(nt)){
					symb.parents.put(nt, 0);
				}
				int n = symb.parents.get(nt);
				symb.parents.put(nt, n+1);
			}

		}
		
		// If a terminal t is such that for every nt \in t.parents,
		// the production rule corresponding to nt consists only of terminal symbols,
		// we label t with the flag allParentsNative.
		for (HashMap.Entry<String, TerminalZipHB> entry : terminalMap.entrySet()){
			TerminalZipHB term = entry.getValue();
			boolean allParentsNative = true;
			for(NonTerminalZipHB nt : term.parents.keySet()){
				if(!nt.allTerminals){
					allParentsNative = false;
				}
			}
			term.allParentsNative = allParentsNative;
		}
	}
	
	// For every non-terminal nt, this function assigns a 
	// set of symbols nt.criticalChildren such that
	//  s \in nt.criticalChildren iff nt has the largest topological index
	// amongst any parent of s. Here, by topological index, we mean
	// the index of a node in a fixed topological ordering of the underlying DAG of the grammar. 
	private static void assignCriticalChildren(ArrayList<SymbolZipHB> symbolList)
	{
		for(SymbolZipHB symb : symbolList){
			if(symb instanceof NonTerminalZipHB){
				((NonTerminalZipHB) symb).criticalChildren = new HashSet<SymbolZipHB> ();
			}
		}
		for(SymbolZipHB symb : symbolList){
			if(!symb.parents.isEmpty()){
				int max = -1;
				NonTerminalZipHB maxParent = null;
				for(NonTerminalZipHB p: symb.parents.keySet()){
					int p_topoInd = p.topologicalIndex;
					if(p_topoInd > max){
						max = p_topoInd;
						maxParent = p;
					}
				}
				maxParent.criticalChildren.add(symb);
			}
		}
	}

	private static void topoHelper(SymbolZipHB curr, HashSet<SymbolZipHB> visited, Stack<SymbolZipHB> stack){
		visited.add(curr);
		if(curr instanceof NonTerminalZipHB){
			for(SymbolZipHB symb: ((NonTerminalZipHB)curr).rule){
				if(!visited.contains(symb)){
					topoHelper(symb, visited, stack);
				}
			}
		}
		stack.push(curr);
	}

	// Return the topological ordering of the symbols in the grammar.
	private static ArrayList<SymbolZipHB> getTopologicalOrder(NonTerminalZipHB start)
	{
		HashSet<SymbolZipHB> visited = new HashSet<SymbolZipHB> ();

		Stack<SymbolZipHB> stack = new Stack<SymbolZipHB>();
		topoHelper(start, visited, stack);
		ArrayList<SymbolZipHB> topoArray = new ArrayList<SymbolZipHB> ();
		int topoInd = stack.size() - 1;
		SymbolZipHB symb;
		while(!stack.empty()){
			symb = stack.pop();
			symb.topologicalIndex = topoInd;
			topoArray.add(symb);
			topoInd = topoInd - 1;
		}
		return topoArray;
	}

	private static ArrayList<SymbolZipHB> initialAnalysis(ParseZipHB parser, NonTerminalZipHB start){
		ArrayList<SymbolZipHB> inverseTopologicalSort = getTopologicalOrder(start);
		int totalSymbols = inverseTopologicalSort.size();
	
		assert(inverseTopologicalSort.get(totalSymbols-1) == start);
		
		for(int idx = totalSymbols-1; idx >= 0; idx --){
			inverseTopologicalSort.get(idx).countObjects();
		}
		
		assignParents(parser.terminalMap, parser.nonTerminalMap, start);
		assignCriticalChildren(inverseTopologicalSort);

		for(int idx = 0; idx < totalSymbols; idx ++){
			inverseTopologicalSort.get(idx).computeRelevantData();
		}

		for (HashMap.Entry<String, NonTerminalZipHB> entry : parser.nonTerminalMap.entrySet())
		{
			entry.getValue().setIntermediateRelevantData();
		}

		for(int idx = 0; idx < totalSymbols; idx ++){
			inverseTopologicalSort.get(idx).deleteCounts();
		}

		return inverseTopologicalSort;
	}

	public static void analyze(String mapFile, String traceFile){		
		ParseZipHB parser = new ParseZipHB();
		parser.parse(mapFile,traceFile);

		// The "start" symbol in the context-free grammar.
		NonTerminalZipHB start = parser.nonTerminalMap.get("0");
		
		// Sort the symbols in the grammar in the inverse topological ordering.
		ArrayList<SymbolZipHB> inverseTopologicalSort = initialAnalysis(parser, start);
		int totalSymbols = inverseTopologicalSort.size();
		
		// Get the topological ordering.
		SymbolZipHB topologicalSort[] = new SymbolZipHB[totalSymbols];
		for(int idx = 0; idx < totalSymbols; idx ++){
			topologicalSort[idx] = inverseTopologicalSort.get(totalSymbols-idx-1);
		}
		inverseTopologicalSort = null;
		parser = null;
		
		long startTimeAnalysis = System.currentTimeMillis();
		boolean race = false;
		//Analyze each of the symbols for races.
		for(int idx = 0; idx < totalSymbols; idx ++){
			SymbolZipHB symb = topologicalSort[idx];
			if(symb instanceof TerminalZipHB){
				if (((TerminalZipHB) symb).allParentsNative){
					continue;
				}
			}
			// Analyze 'symb' for races.
			symb.computeData();
			race = symb.hasRace;
			if(race){
				break;
			}
		}
		long stopTimeAnalysis = System.currentTimeMillis();
		long timeAnalysis = stopTimeAnalysis - startTimeAnalysis;
		System.out.println("Time for compressed trace analysis = " + timeAnalysis + " miliseconds");

		if(race){
			System.out.println("Race detected");
		}
		else{
			System.out.println("Race free");
		}
	}
}
