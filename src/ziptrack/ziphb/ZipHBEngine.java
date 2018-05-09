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

	/*
	private static HashSet<String> getNames(HashSet<Integer> set, HashMap<Integer, String> nameMap){
		HashSet<String> names = new HashSet<String> ();
		for(int x : set){
			names.add(nameMap.get(x));
		}
		return names;
	}
	*/

	/*
	private static void printTS(ArrayList<SymbolZipHB> arr, 
			HashMap<Integer, String> variableNames, HashMap<Integer, String> threadNames, HashMap<Integer, String> lockNames){
		//		for(SymbolHBZDDOpt s: arr){
		//			System.out.println(s.getName());
		////			System.out.print(" ");
		////			System.out.println("Relevant Vars = " + getNames(s.relevantVariables, variableNames));
		//		}

		for(int idx = arr.size() - 1; idx >= 0; idx --){
			SymbolZipHB s = arr.get(idx);
			System.out.println(s.getName() + " " + s.topologicalIndex);
			//			System.out.print(" ");
			//			System.out.println("Relevant Vars = " + getNames(s.relevantVariables, variableNames));
		}

		//		System.out.println("Relevant Intermediate :");
		//		for(SymbolHBZDDOpt s: arr){
		//			if(s instanceof NonTerminalHBZDDOpt){
		//				ArrayList<HashSet<Integer>> IRV = ((NonTerminalHBZDDOpt) s).getIntermediateRelevantVariables();
		//				for(int idx = 0; idx < IRV.size(); idx ++){
		//					System.out.println("IRV[" + idx + "] = " + getNames(IRV.get(idx), variableNames));
		//				}
		//			}
		//		}

	}
	*/

	private static ArrayList<SymbolZipHB> initialAnalysis(ParseZipHB parser, NonTerminalZipHB start){

		long startTimeAnalysis = System.currentTimeMillis();
		ArrayList<SymbolZipHB> inverseTopologicalSort = getTopologicalOrder(start);
		int totalSymbols = inverseTopologicalSort.size();

//		System.out.println("Done topological sorting");

		assert(inverseTopologicalSort.get(totalSymbols-1) == start);
		for(int idx = totalSymbols-1; idx >= 0; idx --){
			inverseTopologicalSort.get(idx).countObjects();
		}

//		System.out.println("Done counting objects");

		assignParents(parser.terminalMap, parser.nonTerminalMap, start);

//		System.out.println("Done assigning parents");
		
		assignCriticalChildren(inverseTopologicalSort);

//		System.out.println("Done assigning critical children");


		for(int idx = 0; idx < totalSymbols; idx ++){
			inverseTopologicalSort.get(idx).computeRelevantData();
			//			System.out.println(inverseTopologicalSort.get(idx).getName() + " :: " + getNames(inverseTopologicalSort.get(idx).relevantVariables, parser.variableNames));
		}

//		System.out.println("Done computing relevant data");

		for (HashMap.Entry<String, NonTerminalZipHB> entry : parser.nonTerminalMap.entrySet())
		{
			entry.getValue().setIntermediateRelevantData();
		}

		for(int idx = 0; idx < totalSymbols; idx ++){
			inverseTopologicalSort.get(idx).deleteCounts();
		}

//		System.out.println("Done computing relevant data for intermediate non terminals");

		//		printTS(inverseTopologicalSort, parser.variableNames, parser.threadNames, parser.lockNames);

		long stopTimeAnalysis = System.currentTimeMillis();
		long timeAnalysis = stopTimeAnalysis - startTimeAnalysis;
//		System.out.println("Time for initial analysis = " + timeAnalysis + " miliseconds");

		return inverseTopologicalSort;

	}

	public static void analyze(String mapFile, String traceFile,boolean stopAfterFirstRace, boolean sanityCheck){
		
		assert(stopAfterFirstRace == true);
		
		ParseZipHB parser = new ParseZipHB();
		parser.parse(mapFile,traceFile);
//		System.out.println("Done parsing");

		NonTerminalZipHB start = parser.nonTerminalMap.get("0");
		//		System.out.println(parser.nonTerminalMap.keySet());
		ArrayList<SymbolZipHB> inverseTopologicalSort = initialAnalysis(parser, start);
		int totalSymbols = inverseTopologicalSort.size();
		SymbolZipHB topologicalSort[] = new SymbolZipHB[totalSymbols];
		for(int idx = 0; idx < totalSymbols; idx ++){
			topologicalSort[idx] = inverseTopologicalSort.get(totalSymbols-idx-1);
		}
		inverseTopologicalSort = null;
		if(!sanityCheck){
			parser = null;
		}
//		System.out.println("Begin analysis");
		
//		System.out.println("Thread#1 = " + parser.threadNames.get(1));
//		System.out.println("Variable#28 = " + parser.variableNames.get(28));

		long startTimeAnalysis = System.currentTimeMillis();
		boolean race = false;
		for(int idx = 0; idx < totalSymbols; idx ++){
			SymbolZipHB symb = topologicalSort[idx];
			if(symb instanceof TerminalZipHB){
				if (((TerminalZipHB) symb).allParentsNative){
					continue;
				}
			}
			symb.computeData(stopAfterFirstRace, sanityCheck);
			race = symb.hasRace;
			if(race && stopAfterFirstRace){
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

		if(sanityCheck){
			System.out.println("\n==== Sanity Check ====\n");
			for(int x: start.setRange.keySet()){
				System.out.println(parser.variableNames.get(x) + " : " + start.setRange.get(x));	
				System.out.println("");
			}
			System.out.println("\n==== Sanity Check ====\n");
		}
	}
}
