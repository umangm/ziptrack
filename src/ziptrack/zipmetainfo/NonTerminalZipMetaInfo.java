package ziptrack.zipmetainfo;

import java.util.ArrayList;

public class NonTerminalZipMetaInfo extends SymbolZipMetaInfo {

	protected ArrayList<SymbolZipMetaInfo> rule;
	public boolean allTerminals; //on RHS

	public NonTerminalZipMetaInfo(String name){
		super(name);
		this.rule = null;
	}

	public NonTerminalZipMetaInfo(String name, ArrayList<SymbolZipMetaInfo> r){
		super(name);
		this.rule = r;
	}

	public void setRule(ArrayList<SymbolZipMetaInfo> r){
		this.rule = r;
	}

	public void printRule(){
		for(SymbolZipMetaInfo s: this.rule){
			System.out.print(s.name + " ");
		}
	}
}
