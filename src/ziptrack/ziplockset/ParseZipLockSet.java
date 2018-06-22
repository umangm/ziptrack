package ziptrack.ziplockset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ziptrack.event.EventType;
import ziptrack.parse.CannotParseException;

public class ParseZipLockSet {
	public static HashMap<String, EventType> eventTypeMap = new HashMap<String, EventType> (getEventTypeMap());
	public static HashMap<String, EventType> getEventTypeMap (){
		HashMap<String, EventType> eMap = new HashMap<String, EventType> ();
		eMap.put("R", EventType.READ);
		eMap.put("W", EventType.WRITE);
		eMap.put("L", EventType.ACQUIRE);
		eMap.put("U", EventType.RELEASE);
		eMap.put("F", EventType.FORK);
		eMap.put("J", EventType.JOIN);
		return eMap;
	}
	
	public HashMap<String, Integer> lockMap;
	public HashMap<String, Integer> threadMap;
	public HashMap<String, Integer> variableMap;
	public HashMap<String, TerminalZipLockSet> terminalMap;
	public HashMap<String, NonTerminalZipLockSet> nonTerminalMap;
	public String eventSplitBy;
	
	public HashMap<Integer, String> threadNames;
	public HashMap<Integer, String> lockNames;
	public HashMap<Integer, String> variableNames;
	
	public int hbObjectIndex;
	public int variableIndex;
	
	public ParseZipLockSet(){
		init();
	}
	
	public void init(){
		lockMap = new HashMap<String, Integer> ();
		threadMap = new HashMap<String, Integer> ();
		variableMap = new HashMap<String, Integer> ();
		terminalMap = new HashMap<String, TerminalZipLockSet> ();
		nonTerminalMap = new HashMap<String, NonTerminalZipLockSet> ();
		eventSplitBy = "|";
		
		hbObjectIndex = 0;
		variableIndex = 0;
		
		threadNames = new HashMap<Integer, String> ();
		lockNames = new HashMap<Integer, String> ();
		variableNames = new HashMap<Integer, String> ();
	}
	
	private static EventType getEventTypeFromText(String eTypeText) throws CannotParseException{
		if (eventTypeMap.containsKey(eTypeText)){
			return eventTypeMap.get(eTypeText);
		}
		else{
			throw new CannotParseException(eTypeText);
		}
	}
	
	public void processEvent(String line){
		String[] splitArray = line.split("[|]");
		String idx = splitArray[0];
		String[] eInfo = splitArray[1].split(",");
		
		String threadName = eInfo[0];
		int t = -1;
		if(!threadMap.containsKey(threadName)){
			t = hbObjectIndex;
			hbObjectIndex = hbObjectIndex + 1;
			threadMap.put(threadName, t);
			threadNames.put(t, threadName);
		}
		else{
			t = threadMap.get(threadName);
		}
		
		EventType eType = null;
		try{
			eType = getEventTypeFromText(eInfo[1]);
		}
		catch(CannotParseException e){
			System.out.println("Unknown type for " + e.getLine());
			e.printStackTrace();
		}
		
		String decorName = eInfo[2];
		int decor = -1;
		
		if (eType.isLockType()){
			if(!lockMap.containsKey(decorName)){
				decor = hbObjectIndex;
				lockMap.put(decorName, decor);
				hbObjectIndex = hbObjectIndex + 1;
				lockNames.put(decor, decorName);
			}
			else{
				decor = lockMap.get(decorName);
			}
		}
		else if (eType.isAccessType()){
			if(!variableMap.containsKey(decorName)){
				decor = variableIndex;
				variableIndex = variableIndex + 1;
				variableMap.put(decorName, decor);
				variableNames.put(decor, decorName);
			}
			else{
				decor = variableMap.get(decorName);
			}
		}
		else if (eType.isExtremeType()){
			if(!threadMap.containsKey(decorName)){
				decor = hbObjectIndex;
				hbObjectIndex = hbObjectIndex + 1;
				threadMap.put(decorName, decor);
				threadNames.put(decor, decorName);
			}
			else{
				decor = threadMap.get(decorName);
			}
		}
		else{
			throw new IllegalArgumentException("Unkown event type " + eType.toString());
		}
		
		
		if(terminalMap.containsKey(idx)){
			throw new IllegalArgumentException("Event already processed ? Terminal-" + idx);
		}
		else{
			TerminalZipLockSet terminal = new TerminalZipLockSet("Term" + idx, eType, t, decor);
			terminalMap.put(idx, terminal);	
		}
	}
	
	public void buildMap(String mapFile){
		try (Stream<String> stream = Files.lines(Paths.get(mapFile))) {
			stream.forEach(s->{processEvent(s);});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isRule(String line){
		return line.matches("^[0-9]+ -> ((([\\[][0-9]+[\\]])|([0-9]+))\\s).*");
//		return line.matches("^[0-9]+ -> ([\\&]?[0-9]+\\s).*");
	}
	
	public NonTerminalZipLockSet processRule(String line){
		String[] parts = line.split(" -> ");
		String nt_name = parts[0];
		NonTerminalZipLockSet nt = null;
		if(!nonTerminalMap.containsKey(nt_name)){
			nt = new NonTerminalZipLockSet("NTerm" + nt_name);
			nonTerminalMap.put(nt_name, nt);
		}
		else{
			nt = nonTerminalMap.get(nt_name);
		}
		
		String[] rule_str_lst = parts[1].split("\\s+");
		ArrayList<SymbolZipLockSet> rule = new ArrayList<SymbolZipLockSet> ();
		
		for(String symb_str : rule_str_lst){
			if(symb_str.length() <= 0){
				throw new IllegalArgumentException("Symbol length is non-positive " + symb_str);
			}
			SymbolZipLockSet symb = null;
			
			if(symb_str.matches("^\\d+$")){
				if(nonTerminalMap.containsKey(symb_str)){
					symb = nonTerminalMap.get(symb_str);
				}
				else{
					symb = new NonTerminalZipLockSet("NTerm" + symb_str);
					nonTerminalMap.put(symb_str, (NonTerminalZipLockSet) symb);
				}
			}
//			else if(symb_str.matches("^[&]\\d+$")){
			else if(symb_str.matches("^[\\[]\\d+[\\]]$")){
				symb_str = symb_str.substring(1, symb_str.length()-1);
				if(!terminalMap.containsKey(symb_str)){
					throw new IllegalArgumentException("Terminal symbol not found : " + symb_str);
				}
				else{
					symb = terminalMap.get(symb_str);
				}
			}
			else{
				throw new IllegalArgumentException("Absurd symbol : " + symb_str);
			}
			
			rule.add(symb);
			nt.setRule(rule);
		}
		return nt;
	}
	
	public ArrayList<NonTerminalZipLockSet> buildGrammar(String traceFile){
		ArrayList<NonTerminalZipLockSet> cfg = null;
		try (Stream<String> stream = Files.lines(Paths.get(traceFile))) {
			cfg  = stream.filter(s->isRule(s)).map(s->processRule(s)).collect(Collectors.toCollection(ArrayList::new));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cfg;
	}
	
	public ArrayList<NonTerminalZipLockSet> parse(String mapFile, String traceFile){
		this.buildMap(mapFile);
		return this.buildGrammar(traceFile);
	}
}
