package ziptrack.event;

// Event class.
// An event is a tuple (id, type, thread, decor).
// 'id' is a unique identifier.
// 'type' corresponds to one of read/write/acquire/release/fork/join.
// 'thread' is the thread performing the event.
// 'decor' is the additional decoration:
// (a) when 'type' is read/write, decor corresponds to the memory 
// location being read from/written to.
// (b) when 'type' is acquire/release, decor corresponds to the lock 
// begin acquire or released.
// (c) when 'type' is fork/join, decor corresponds to the child 
// thread forked or joined by 'thread'.
public class Event {
	//Data for Event
	public static Long eventCountTracker = (long) 0;
	protected Long id;
	protected EventType type;
	protected Thread thread;
	
	private Decoration decor;
	
	public void updateEvent(EventType tp, Thread th, Decoration d) {
		this.id = eventCountTracker;
		eventCountTracker++;
		this.type = tp;
		this.thread = th;
		this.decor = d;
	}
	
	public void updateEvent(){
		this.updateEvent(EventType.DUMMY, null, null);
	}
	
	public Event() {
		this.updateEvent();
	}
	
	public Event(EventType tp, Thread th, Decoration d) {
		this.updateEvent(tp, th, d);
	}

	public Long getId() {
		return this.id;
	}

	public EventType getType() {
		return type;
	}
	
	public void setType(EventType tp) {
		this.type = tp;
	}

	public Thread getThread() {
		return thread;
	}
	
	public String toString() {
		return "<" +this.thread.toString() + "," + this.type.toString() + "(" + this.decor.toString() + ")>";
	}
	
	private Decoration getDecor(){
		return this.decor;
	}
	
	/**************Acquire/Release*******************/
	public Lock getLock() {
		if (! this.getType().isLockType()) throw new IllegalArgumentException("Illegal operation getLock() for EventType " + this.getType().toString());
		return (Lock)getDecor();
	}
	/************************************************/
	
	/****************Read/Write**********************/
	public Variable getVariable() {
		if (! this.getType().isAccessType()) throw new IllegalArgumentException("Illegal operation getVariable() for EventType " + this.getType().toString());		
		return (Variable)this.getDecor();
	}
	/************************************************/
	
	/*****************Fork/Join**********************/
	public Thread getTarget() {
		if (! this.getType().isExtremeType()) throw new IllegalArgumentException("Illegal operation getTarget() for EventType " + this.getType().toString());
		return (Thread) this.getDecor();
	}
	
	/************************************************/
}