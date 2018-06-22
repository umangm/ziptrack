package ziptrack.event;

public class Lock extends Decoration {
	
	public static int lockCountTracker = 0;

	public Lock() {
		super();
		this.id = lockCountTracker;
		lockCountTracker++;
		this.name = "__lock::" + Integer.toString(this.id) + "__";
	}

	public Lock(String sname) {
		super();
		this.id = lockCountTracker;
		lockCountTracker++;
		this.name = sname;
	}

}
