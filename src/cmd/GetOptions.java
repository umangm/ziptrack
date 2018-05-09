package cmd;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class GetOptions {

    private static final Logger log = Logger.getLogger(GetOptions.class.getName());
    private String[] args = null;
    private Options options = new Options();

    public GetOptions(String[] args) {
        this.args = args;
        options.addOption("h", "help", false, "generate this message");
        options.addOption("t", "trace", true, "the path to the trace file (Required)");
        options.addOption("m", "map", true, "the path to the map file [mapping event ids to their descriptions] (Required)");
        options.addOption("v", "verbosity", true, "for setting verbosity: Allowed levels = 0, 1, 2 (Default : 0)");
        options.addOption("s", "single", false, "force the algorithm to terminate after the first race is detected");
        options.addOption("c", "check", false, "if enabled, also calculates, for every variable, the set of interval-pairs containing the two components of any race on that variable. (Cannot be enabled when single is enabled)");
    }

    public CmdOptions parse() {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        CmdOptions cmdOpt = new CmdOptions();;
        
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h"))
                help();

            if (cmd.hasOption("v")) {
            	//log.log(Level.INFO, "Using cli argument -s ");
            	try{
            		cmdOpt.verbosity = Integer.parseInt(cmd.getOptionValue("v"));
            		if(cmdOpt.verbosity < 0 || cmdOpt.verbosity > 3){
            			log.log(Level.INFO, "Invalid verbosity level : " + cmdOpt.verbosity);
            			help();
                    	System.exit(0);
            		}
            	}
            	catch (NumberFormatException nfe){
            		log.log(Level.INFO, "Invalid verbosity option : " + cmd.getOptionValue("v"));
            		help();
                	System.exit(0);
            	}
            } 
            
            if (cmd.hasOption("s")) {
            	//log.log(Level.INFO, "Using cli argument -s ");
                cmdOpt.singleRace = true;  
            }
            
            if (cmd.hasOption("c")) {
            	//log.log(Level.INFO, "Using cli argument -c ");
                cmdOpt.sanityCheck = true;  
                if(cmdOpt.singleRace){
                	log.log(Level.INFO, "Cannot switch both single and check flags to true.");
                	help();
                	System.exit(0);
                }
            }
            
            if (cmd.hasOption("t")) {
            	//log.log(Level.INFO, "Using cli argument -t=" + cmd.getOptionValue("t"));
                cmdOpt.trace_file = cmd.getOptionValue("t") ;   
            }
            else {
                log.log(Level.INFO, "MIssing path to trace file");
                help();
            }
            
            if (cmd.hasOption("m")) {
            	//log.log(Level.INFO, "Using cli argument -m=" + cmd.getOptionValue("m"));
                cmdOpt.map_file = cmd.getOptionValue("m") ;   
            }
            else {
                log.log(Level.INFO, "Missing path to map file");
                help();
            }

        } catch (ParseException e) {
        	//log.log(Level.INFO, "Failed to parse command line properties", e);
            help();
        }
        
        //System.out.println(cmdOpt.toString());
        return cmdOpt;
    }

    private void help() {
        // This prints out some help
        HelpFormatter formater = new HelpFormatter();

        formater.printHelp("ZipTrack", options);
        System.exit(0);
    }

    public static void main(String[] args) {
        new GetOptions(args).parse();
    }
}
