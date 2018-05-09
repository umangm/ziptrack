package ziptrack.grammar;

import java.util.ArrayList;

public class NonTerminal extends Symbol {
	
	protected ArrayList<Symbol> rule;
	
	public NonTerminal(String name){
		super(name);
		this.rule = null;
	}
	
	public NonTerminal(String name, ArrayList<Symbol> r){
		super(name);
		this.rule = r;
	}
	
	public void setRule(ArrayList<Symbol> r){
		this.rule = r;
	}
	
	public void printRule(){
		for(Symbol s: this.rule){
			System.out.print(s.name + " ");
		}
	}

}
