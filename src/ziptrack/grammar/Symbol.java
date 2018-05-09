package ziptrack.grammar;

public abstract class Symbol {
	protected String name;
	
	protected Symbol(String n){
		this.name = n;
	}
	
	public String getName(){
		return this.name;
	}

}
