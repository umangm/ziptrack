package ziptrack.ziphb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class ZipHBEngine {

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

		NonTerminalZipHB start = parser.nonTerminalMap.get("0");
		ArrayList<SymbolZipHB> inverseTopologicalSort = initialAnalysis(parser, start);
		int totalSymbols = inverseTopologicalSort.size();
		SymbolZipHB topologicalSort[] = new SymbolZipHB[totalSymbols];
		for(int idx = 0; idx < totalSymbols; idx ++){
			topologicalSort[idx] = inverseTopologicalSort.get(totalSymbols-idx-1);
		}
		inverseTopologicalSort = null;
		parser = null;
		
		long startTimeAnalysis = System.currentTimeMillis();
		boolean race = false;
		for(int idx = 0; idx < totalSymbols; idx ++){
			SymbolZipHB symb = topologicalSort[idx];
			if(symb instanceof TerminalZipHB){
				if (((TerminalZipHB) symb).allParentsNative){
					continue;
				}
			}
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
