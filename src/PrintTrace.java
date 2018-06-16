import print.cmd.CmdOptions;
import print.cmd.GetOptions;
import print.engine.ZipTrackPrintEngine;

public class PrintTrace {

	public PrintTrace() {

	}
	
	public static void main(String[] args) {	
		CmdOptions options = new GetOptions(args).parse();
		ZipTrackPrintEngine engine = new ZipTrackPrintEngine(options.path, true);
		engine.analyzeTrace();
	}
}
