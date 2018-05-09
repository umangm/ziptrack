package ziptrack.grammar;

import ziptrack.event.Event;

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
