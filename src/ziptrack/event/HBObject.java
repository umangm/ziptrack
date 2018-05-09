package ziptrack.event;

public abstract class HBObject extends Decoration {
	public static int hbCountTracker = 0;
	
	private int zddIndex;

	public HBObject() {
		this.zddIndex = hbCountTracker;
		hbCountTracker++;
	}
	
	public int getZDDIndex(){
		return this.zddIndex;
	}

}
