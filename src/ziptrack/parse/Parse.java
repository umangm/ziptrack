package ziptrack.parse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ziptrack.event.Event;
import ziptrack.event.EventType;
import ziptrack.event.Thread;
import ziptrack.event.Lock;
import ziptrack.event.Variable;
import ziptrack.grammar.NonTerminal;
import ziptrack.grammar.Symbol;
import ziptrack.grammar.Terminal;

public class Parse {
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
	
	public HashMap<String, Lock> lockMap;
	public HashMap<String, Thread> threadMap;
	public HashMap<String, Variable> variableMap;
	public HashMap<String, Terminal> terminalMap;
	public HashMap<String, NonTerminal> nonTerminalMap;
	public String eventSplitBy;
	
	public Parse(){
		init();
	}
	
	public void init(){
		lockMap = new HashMap<String, Lock> ();
		threadMap = new HashMap<String, Thread> ();
		variableMap = new HashMap<String, Variable> ();
		terminalMap = new HashMap<String, Terminal> ();
		nonTerminalMap = new HashMap<String, NonTerminal> ();
		eventSplitBy = "|";
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
		Thread t = null;
		if(!threadMap.containsKey(threadName)){
			t = new Thread(threadName);
			threadMap.put(threadName, t);
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
		Event e = null;
		
		if (eType.isLockType()){
			Lock l = null;
			if(!lockMap.containsKey(decorName)){
				l = new Lock(decorName);
				lockMap.put(decorName, l);
			}
			else{
				l = lockMap.get(decorName);
			}
			e = new Event(eType, t, l);
		}
		else if (eType.isAccessType()){
			Variable v = null;
			if(!variableMap.containsKey(decorName)){
				v = new Variable(decorName);
				variableMap.put(decorName, v);
			}
			else{
				v = variableMap.get(decorName);
			}
			e = new Event(eType, t, v);
		}
		else if (eType.isExtremeType()){
			Thread tar = null;
			if(!threadMap.containsKey(decorName)){
				tar = new Thread(decorName);
				threadMap.put(decorName, tar);
			}
			else{
				tar = threadMap.get(decorName);
			}
			e = new Event(eType, t, tar);
		}
		else{
			throw new IllegalArgumentException("Unkown event type " + eType.toString());
		}
		
		
		if(terminalMap.containsKey(idx)){
			throw new IllegalArgumentException("Event already processed ? Terminal-" + idx);
		}
		else{
			Terminal terminal = new Terminal("Term" + idx, e);
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
		//return line.matches("^[0-9]+ -> (([\\&]?[0-9]+\\s))*[\\&]?[0-9]+\\s+$");
		return line.matches("^[0-9]+ -> ([\\&]?[0-9]+\\s).*");
	}
	
	public NonTerminal processRule(String line){
		String[] parts = line.split(" -> ");
		String nt_name = parts[0];
		NonTerminal nt = null;
		if(!nonTerminalMap.containsKey(nt_name)){
			nt = new NonTerminal("NTerm" + nt_name);
			nonTerminalMap.put(nt_name, nt);
		}
		else{
			nt = nonTerminalMap.get(nt_name);
		}
		
		
		String[] rule_str_lst = parts[1].split("\\s+");
		ArrayList<Symbol> rule = new ArrayList<Symbol> ();
		
		for(String symb_str : rule_str_lst){
			if(symb_str.length() <= 0){
				throw new IllegalArgumentException("Symbol length is non-positive " + symb_str);
			}
			Symbol symb = null;
			
			if(symb_str.matches("^\\d+$")){
				if(nonTerminalMap.containsKey(symb_str)){
					symb = nonTerminalMap.get(symb_str);
				}
				else{
					symb = new NonTerminal(symb_str);
					nonTerminalMap.put(symb_str, (NonTerminal) symb);
				}
			}
			else if(symb_str.matches("^[&]\\d+$")){
				symb_str = symb_str.substring(1);
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
	
	public ArrayList<NonTerminal> buildGrammar(String traceFile){
		ArrayList<NonTerminal> cfg = null;
		try (Stream<String> stream = Files.lines(Paths.get(traceFile))) {
			cfg  = stream.filter(s->isRule(s)).map(s->processRule(s)).collect(Collectors.toCollection(ArrayList::new));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cfg;
	}
	
	public ArrayList<NonTerminal> parse(String mapFile, String traceFile){
		this.buildMap(mapFile);
		return this.buildGrammar(traceFile);
	}

}
