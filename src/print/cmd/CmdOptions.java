package print.cmd;

import print.parse.ParserType;

public class CmdOptions {
	public String path;
	public ParserType parserType;
	public String map_file;
	
	public CmdOptions() {
		this.path = null;
		this.map_file = null;
		this.parserType = ParserType.RR;
	}
	
	public String toString(){
		String str = "";
		str += "path			" + " = " + this.path					+ "\n";
		str += "path to map file" + " = " + this.map_file				+ "\n";
		str += "parserType		" + " = " + this.parserType.toString() 	+ "\n";
		return str;
	}

}
