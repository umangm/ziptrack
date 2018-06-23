package ziptrack.zipmetainfo;

public class ZipMetaInfoEngine {
	
	// Print meta-data about the SLP and the underlying trace.
	public static void analyze(String mapFile, String traceFile){
		ParseZipMetaInfo parser = new ParseZipMetaInfo();
		parser.parse(mapFile, traceFile);
		
		System.out.println("Number of Threads = " + parser.threadNames.keySet().size());
		System.out.println("Number of Locks = " + parser.lockNames.keySet().size());
		System.out.println("Number of Variables = " + parser.variableNames.keySet().size());
		
		System.out.println("Number of Terminals = " + parser.terminalMap.keySet().size());
		System.out.println("Number of Non-Terminals = " + parser.nonTerminalMap.keySet().size());
		
		int mix_nt = 0;
		for(String nt_str: parser.nonTerminalMap.keySet()){
			NonTerminalZipMetaInfo nt = parser.nonTerminalMap.get(nt_str);
			mix_nt += nt.allTerminals?0:1;
		}
		System.out.println("Number of mix Non-Terminals = " + mix_nt);
		
		int sz_grammar = 0;
		for(String nt_str: parser.nonTerminalMap.keySet()){
			sz_grammar += parser.nonTerminalMap.get(nt_str).rule.size();
		}
		System.out.println("Size of grammar (sum of length of RHSs) = " + sz_grammar);
		
		int sz_trans = 0;
		for(String nt_str: parser.nonTerminalMap.keySet()){
			NonTerminalZipMetaInfo nt = parser.nonTerminalMap.get(nt_str);
			sz_trans += nt.allTerminals?0:nt.rule.size();
		}
		System.out.println("Size of mix grammar (sum of length of RHSs with atleast 1 NonTerminal) = " + sz_trans);
	}
}
