import cmd.CmdOptions;
import cmd.GetOptions;
import ziptrack.ziphb.ZipHBEngine;

public class ZipHB {
	public static void main(String args[]){
		CmdOptions options = new GetOptions(args).parse();
		ZipHBEngine.analyze(options.map_file, options.trace_file);
	}
}
