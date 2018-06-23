import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cmd.CmdOptions;
import cmd.GetOptions;
import javafx.util.Pair;

// Given an SLP S, the main function produces an equivalent SLP S'
// by introducing new production rules for large contiguous blocks of terminals
// present in the SLP S.
// For example, for the rule A -> b1 . . . bk C d1 . . . dm, 
// where bi's and dj's are terminal,
// the following three rules are introduced
// A -> B C D
// B -> b1 . . . bk
// D -> d1 . . . dm
// where B and D are freshly intoduced non-terminals.
public class TransformGrammar {

	public String eventSplitBy;

	public TransformGrammar(){
	}

	public static boolean isTerminal(String str){
		return str.charAt(0) == '[';
	}

	public static boolean isRule(String line){
		//		return line.matches("^[0-9]+ -> ([\\&]?[0-9]+\\s).*");
		return line.matches("^[0-9]+ -> ((([\\[][0-9]+[\\]])|([0-9]+))\\s).*");
	}

	public static Pair<String, ArrayList<String>> processRule(String line){
		String[] parts = line.split(" -> ");
		String nt_name = parts[0];
		String[] rule_str_lst = parts[1].split("\\s+");
		ArrayList<String> newLst = new ArrayList<String> (Arrays.asList(rule_str_lst));
		return new Pair<String, ArrayList<String>> (nt_name, newLst);
	}

	public static ArrayList<Pair<String, ArrayList<String>>> buildGrammar(String traceFile){
		ArrayList<Pair<String, ArrayList<String>>> cfg = null;
		try (Stream<String> stream = Files.lines(Paths.get(traceFile))) {
			cfg  =  stream.filter(s->isRule(s)).map(s->processRule(s)).collect(Collectors.toCollection(ArrayList::new));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cfg;
	}

	private static int transformRule(Pair<String, ArrayList<String>> nt, int freshIndex, 
			ArrayList<Pair<String, ArrayList<String>>> newCFG, int minLengthOfTerminalSequence){
		ArrayList<String> rule = nt.getValue();
		int rule_len = rule.size();
		ArrayList<String> currRule = new ArrayList<String> ();
		ArrayList<String> tmpNewRule = new ArrayList<String> ();
		boolean insideRule = false;

		for(String symb : rule){
			if(!insideRule){
				if(isTerminal(symb)){
					insideRule = true;
					tmpNewRule.add(symb);
				}
				else{
					currRule.add(symb);
				}
			}
			else{
				if(isTerminal(symb)){
					tmpNewRule.add(symb);
				}
				else{
					if(tmpNewRule.size() >= minLengthOfTerminalSequence){
						String newNTName = Integer.toString(freshIndex);
						Pair<String, ArrayList<String>> newNT = new Pair<String, ArrayList<String>> (newNTName, tmpNewRule);
						freshIndex = freshIndex + 1;
						currRule.add(newNTName);
						newCFG.add(newNT);
					}
					else{
						currRule.addAll(tmpNewRule);
					}
					tmpNewRule = new ArrayList<String>();
					insideRule = false;
					currRule.add(symb);
				}
			}
		}

		if(insideRule){
			if(tmpNewRule.size() >= minLengthOfTerminalSequence && tmpNewRule.size() < rule_len){
				String newNTName = Integer.toString(freshIndex);
				Pair<String, ArrayList<String>> newNT = new Pair<String, ArrayList<String>> (newNTName, tmpNewRule);
				freshIndex = freshIndex + 1;
				currRule.add(newNTName);
				newCFG.add(newNT);
			}
			else{
				currRule.addAll(tmpNewRule);
			}
		}
		Pair<String, ArrayList<String>> newNT = new Pair<String, ArrayList<String>> (nt.getKey(), currRule);
		newCFG.add(newNT);
		return freshIndex;
	}

	public static ArrayList<Pair<String, ArrayList<String>>> transformGrammar_helper(ArrayList<Pair<String, 
			ArrayList<String>>> cfg, int minLengthOfTerminalSequence)
	{
		int freshIndex = -1;
		for(Pair<String, ArrayList<String>> nt : cfg){
			int ntIndex = Integer.parseInt(nt.getKey());
			if(ntIndex > freshIndex){
				freshIndex = ntIndex;
			}
		}
		freshIndex = freshIndex + 1;

		ArrayList<Pair<String, ArrayList<String>>> newCFG = new ArrayList<Pair<String, ArrayList<String>>> ();

		for(Pair<String, ArrayList<String>> nt : cfg){
			freshIndex = transformRule(nt, freshIndex, newCFG, minLengthOfTerminalSequence);
		}

		return newCFG;
	}

	public static void transformGrammar(String traceFile, int minLengthOfTerminalSequence){
		ArrayList<Pair<String, ArrayList<String>>> cfg = buildGrammar(traceFile);
		ArrayList<Pair<String, ArrayList<String>>> newCFG = transformGrammar_helper(cfg, minLengthOfTerminalSequence);
		for(Pair<String, ArrayList<String>> nt: newCFG){
			System.out.print(nt.getKey() + " -> ");
			ArrayList<String> rule = nt.getValue();
			for(String symb: rule){
				System.out.print(symb + " ");
			}
			System.out.print("\n");
		}
	}

	public static void main(String args[]){
		CmdOptions options = new GetOptions(args).parse();
		TransformGrammar.transformGrammar(options.trace_file, 2);
	}
}
