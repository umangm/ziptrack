package ziptrack.zipmetainfo;

import ziptrack.event.EventType;

public class TerminalZipMetaInfo extends SymbolZipMetaInfo {

	protected EventType type;
	protected int thread;
	protected int decor;

	public TerminalZipMetaInfo(String name, EventType tp, int th, int dec){
		super(name);
		this.type = tp;
		this.thread = th;
		this.decor = dec;
	}

	public String toEventString(String tname, String decorName){
		return "<" + tname+ "," + this.type.toString() + "(" + decorName + ")>";
	}

	public String toEventString(){
		return "<" + this.thread+ "," + this.type.toString() + "(" + this.decor + ")>";
	}

	public EventType getType(){
		return this.type;
	}

	public int getThread(){
		return this.thread;
	}

	public int getDecor(){
		return this.decor;
	}
}
