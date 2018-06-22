package cmd;

public class CmdOptions {
	
	public String trace_file;
	public String map_file;
	public int verbosity;

	public CmdOptions() {
		this.trace_file = null;
		this.map_file = null;
		this.verbosity = 0;
	}
	
	public String toString(){
		String str = "";
		str += "trace_file		" + " = " + this.trace_file				+ "\n";
		str += "map_file		" + " = " + this.map_file				+ "\n";
		str += "verbosity		" + " = " + this.verbosity				+ "\n";
		return str;
	}

}
