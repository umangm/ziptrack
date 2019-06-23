import print.cmd.CmdOptions;
import print.cmd.GetOptions;
import print.engine.ZipTrackPrintEngine;

public class PrintTrace {

	public PrintTrace() {
	}
	
	// Prints RVPredict traces in human-readable format.
	public static void main(String[] args) {	
		CmdOptions options = new GetOptions(args).parse();
		ZipTrackPrintEngine engine = new ZipTrackPrintEngine(options.parserType, options.path, options.map_file, true);
		engine.analyzeTrace();
	}
}
