package ziptrack.grammar;

import ziptrack.event.Event;

// Terminal symbols in a context free grammar.
// In our setting, these correspond to events in the trace.
public class Terminal extends Symbol {
	
	protected Event event;
	
	public Terminal(String name, Event e){
		super(name);
		this.event = e;
	}
	
	public Event getEvent(){
		return this.event;
	}
}
