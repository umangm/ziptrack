package ziptrack.ziplockset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;


public class ZipLockSetEngine {

	// For every symbol symb in the grammar, construct a 
	// map symb.parents : non-terminal -> integer
	// such that symb.parents(nt) = # of occurrences of symb 
	// in the unique production rule of nt.
	private static void assignParents(HashMap<String, TerminalZipLockSet> terminalMap, 
			HashMap<String, NonTerminalZipLockSet> nonTerminalMap, SymbolZipLockSet start){
		for (HashMap.Entry<String, TerminalZipLockSet> entry : terminalMap.entrySet()){
			entry.getValue().parents = new HashSet<NonTerminalZipLockSet> ();
		}
		for (HashMap.Entry<String, NonTerminalZipLockSet> entry : nonTerminalMap.entrySet()){
			entry.getValue().parents = new HashSet<NonTerminalZipLockSet> ();
		}

		for (HashMap.Entry<String, NonTerminalZipLockSet> entry : nonTerminalMap.entrySet()){
			NonTerminalZipLockSet nt = entry.getValue();
			for(SymbolZipLockSet symb: nt.rule){
				symb.parents.add(nt);
			}
		}
	}
	
	private static void topoHelper(SymbolZipLockSet curr, HashSet<SymbolZipLockSet> visited, Stack<SymbolZipLockSet> stack){
		visited.add(curr);
		if(curr instanceof NonTerminalZipLockSet){
			for(SymbolZipLockSet symb: ((NonTerminalZipLockSet)curr).rule){
				if(!visited.contains(symb)){
					topoHelper(symb, visited, stack);
				}
			}
		}
		stack.push(curr);
	}

	// Return the topological ordering of the symbols in the grammar.
	private static ArrayList<SymbolZipLockSet> getTopologicalOrder(NonTerminalZipLockSet start){
		HashSet<SymbolZipLockSet> visited = new HashSet<SymbolZipLockSet> ();

		Stack<SymbolZipLockSet> stack = new Stack<SymbolZipLockSet>();
		topoHelper(start, visited, stack);
		ArrayList<SymbolZipLockSet> topoArray = new ArrayList<SymbolZipLockSet> ();
		int topoInd = stack.size() - 1;
		SymbolZipLockSet symb;
		while(!stack.empty()){
			symb = stack.pop();
			symb.topologicalIndex = topoInd;
			topoArray.add(symb);
			topoInd = topoInd - 1;
		}
		return topoArray;
	}
	
	private static ArrayList<SymbolZipLockSet> initialAnalysis(ParseZipLockSet parser, NonTerminalZipLockSet start){

		ArrayList<SymbolZipLockSet> inverseTopologicalSort = getTopologicalOrder(start);
		int totalSymbols = inverseTopologicalSort.size();

		assert(inverseTopologicalSort.get(totalSymbols-1) == start);
		for(int idx = totalSymbols-1; idx >= 0; idx --){
			inverseTopologicalSort.get(idx).countObjects();
		}

		assignParents(parser.terminalMap, parser.nonTerminalMap, start);

		for(int idx = 0; idx < totalSymbols; idx ++){
			inverseTopologicalSort.get(idx).computeRelevantData();
		}
		
		return inverseTopologicalSort;
	}
	
	public static void analyze(String mapFile, String traceFile){
		ParseZipLockSet parser = new ParseZipLockSet();
		parser.parse(mapFile,traceFile);

		// The "start" symbol in the context-free grammar.
		NonTerminalZipLockSet start = parser.nonTerminalMap.get("0");
		
		// Sort the symbols in the grammar in the inverse topological ordering.
		ArrayList<SymbolZipLockSet> inverseTopologicalSort = initialAnalysis(parser, start);
		int totalSymbols = inverseTopologicalSort.size();
		
		// Get the topological ordering.
		SymbolZipLockSet topologicalSort[] = new SymbolZipLockSet[totalSymbols];
		for(int idx = 0; idx < totalSymbols; idx ++){
			topologicalSort[idx] = inverseTopologicalSort.get(totalSymbols-idx-1);
		}
		inverseTopologicalSort = null;
		parser = null;

		long startTimeAnalysis = System.currentTimeMillis();
		boolean violationFound = false;
		//Analyze each of the symbols for lockset violations.
		for(int idx = 0; idx < totalSymbols; idx ++){
			topologicalSort[idx].computeData();
			violationFound = topologicalSort[idx].violationFound;
			if(violationFound){
				break;
			}
		}

		long stopTimeAnalysis = System.currentTimeMillis();
		long timeAnalysis = stopTimeAnalysis - startTimeAnalysis;
		System.out.println("Time for LockSet analysis = " + timeAnalysis + " miliseconds");
		
		if(violationFound){
			System.out.println("Lockset violation detected");
		}
		else{
			System.out.println("No lockset violation");
		}
	}
}
