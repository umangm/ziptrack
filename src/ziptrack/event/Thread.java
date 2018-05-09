package ziptrack.event;

public class Thread extends HBObject {
	
	public static int threadCountTracker = 0;

	public Thread() {
		super();
		this.id = threadCountTracker;
		threadCountTracker++;
		this.name = "__thread::" + Integer.toString(this.id) + "__";
	}

	public Thread(String sname) {
		super();
		this.id = threadCountTracker;
		threadCountTracker++;
		this.name = sname;
	}

}
