import cmd.CmdOptions;
import cmd.GetOptions;
import ziptrack.zipmetainfo.ZipMetaInfoEngine;

// Prints metadata information about the input compressed trace.
public class ZipMetaInfo {
	public static void main(String args[]){
		CmdOptions options = new GetOptions(args).parse();
		ZipMetaInfoEngine.analyze(options.map_file, options.trace_file);
	}
}
