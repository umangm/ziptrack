package ziptrack.grammar;

// Abstract class for a symbol in a context free grammar.
// A symbol can be a terminal or a non-terminal.
public abstract class Symbol {
	protected String name;
	
	protected Symbol(String n){
		this.name = n;
	}
	
	public String getName(){
		return this.name;
	}

}
