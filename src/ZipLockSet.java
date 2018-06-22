import cmd.CmdOptions;
import cmd.GetOptions;
import ziptrack.ziplockset.ZipLockSetEngine;

public class ZipLockSet {
	public static void main(String args[]){
		CmdOptions options = new GetOptions(args).parse();
		ZipLockSetEngine.analyze(options.map_file, options.trace_file);
	}
}
