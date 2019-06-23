package print.cmd;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import print.parse.ParserType;

public class GetOptions {

	private static final Logger log = Logger.getLogger(GetOptions.class.getName());
	private String[] args = null;
	private Options options = new Options();

	public GetOptions(String[] args) {
		this.args = args;
		options.addOption("h", "help", false, "generate this message");
		options.addOption("f", "format", true, "format of the trace. Possible choices include rv, rr, std (Default : rr) ");
		options.addOption("p", "path", true, "the path to the trace file/folder (Required)");
        options.addOption("m", "map", true, "file path to dump the map file [mapping event ids to their descriptions] (Required)");

	}

	public CmdOptions parse() {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		CmdOptions cmdOpt = new CmdOptions();;

		try {
			cmd = parser.parse(options, args);
			if (cmd.hasOption("h"))
				help();
			
			if (cmd.hasOption("f")) {
				cmdOpt.parserType = ParserType.getType(cmd.getOptionValue("f")) ; 
				if (cmdOpt.parserType.isRV()) {
					System.err.println("**Warning** - Provided file format is RV. RVPredict support is getting obsolete day-by-day. \nConsider using some other logger.\n");
				}
			} 
			
			if (cmd.hasOption("p")) {
				cmdOpt.path = cmd.getOptionValue("p") ;   
			}
			else {
				log.log(Level.INFO, "MIssing path to trace file/folder");
				help();
			}
			
			if (cmd.hasOption("m")) {
				cmdOpt.map_file = cmd.getOptionValue("m") ;   
			}
			else {
				log.log(Level.INFO, "MIssing path to map file");
				help();
			}

		} catch (ParseException e) {
			help();
		}

		return cmdOpt;
	}

	private void help() {
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Ziptrack-Print", options);
		System.exit(0);
	}

	public static void main(String[] args) {
		new GetOptions(args).parse();
	}
}
