package ziptrack.ziplockset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;


public class ZipLockSetEngine {

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

		long startTimeAnalysis = System.currentTimeMillis();
		ArrayList<SymbolZipLockSet> inverseTopologicalSort = getTopologicalOrder(start);
		int totalSymbols = inverseTopologicalSort.size();

//		System.out.println("Done topological sorting");

		assert(inverseTopologicalSort.get(totalSymbols-1) == start);
		for(int idx = totalSymbols-1; idx >= 0; idx --){
			//			System.out.println("Counting objects for " +inverseTopologicalSort.get(idx).getName());
			inverseTopologicalSort.get(idx).countObjects();
		}

//		System.out.println("Done counting objects");

		assignParents(parser.terminalMap, parser.nonTerminalMap, start);

//		System.out.println("Done assigning parents");

//		assignCriticalChildren(inverseTopologicalSort);

//		System.out.println("Done assigning critical children");


		for(int idx = 0; idx < totalSymbols; idx ++){
			inverseTopologicalSort.get(idx).computeRelevantData();
		}

//		System.out.println("Done computing relevant data");

		long stopTimeAnalysis = System.currentTimeMillis();
		long timeAnalysis = stopTimeAnalysis - startTimeAnalysis;
//		System.out.println("Time for initial analysis = " + timeAnalysis + " miliseconds");

		return inverseTopologicalSort;

	}
	
	public static void analyze(String mapFile, String traceFile, boolean stopAfterFirstRace){
		ParseZipLockSet parser = new ParseZipLockSet();
		parser.parse(mapFile,traceFile);

//		System.out.println("Done parsing");

		NonTerminalZipLockSet start = parser.nonTerminalMap.get("0");
		
		ArrayList<SymbolZipLockSet> inverseTopologicalSort = initialAnalysis(parser, start);
		int totalSymbols = inverseTopologicalSort.size();
		SymbolZipLockSet topologicalSort[] = new SymbolZipLockSet[totalSymbols];
		for(int idx = 0; idx < totalSymbols; idx ++){
			topologicalSort[idx] = inverseTopologicalSort.get(totalSymbols-idx-1);
		}
		inverseTopologicalSort = null;
		parser = null;
//		System.out.println("Begin analysis");

		long startTimeAnalysis = System.currentTimeMillis();
		boolean violationFound = false;
		for(int idx = 0; idx < totalSymbols; idx ++){
			topologicalSort[idx].computeData(stopAfterFirstRace);
			violationFound = topologicalSort[idx].violationFound;
			if(violationFound && stopAfterFirstRace){
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
