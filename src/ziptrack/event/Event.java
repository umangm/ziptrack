package ziptrack.event;


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

//	public void copyFrom(Event fromEvent){
//		this.id = fromEvent.getId();
//		this.auxId = fromEvent.getAuxId();
//		this.locId = fromEvent.getLocId();
//		this.name = fromEvent.getName();
//		this.type = fromEvent.getType();
//		this.thread = fromEvent.getThread();
//		
//		//Data for Acquire/Release
//		if(this.getType().isLockType()){
//			this.lock = fromEvent.getLock();
//			this.setReadVarSet(fromEvent.getReadVarSet());
//			this.setWriteVarSet(fromEvent.getWriteVarSet());
//		}
//		
//		//Data for Read/Write
//		if(this.getType().isAccessType()){
//			this.variable = fromEvent.getVariable();
//			this.setLockSet(fromEvent.getLockSet());
//		}
//		
//		//Data for Fork/Join
//		if(this.getType().isExtremeType()){
//			this.target = fromEvent.getTarget();
//		}
//	}
	
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