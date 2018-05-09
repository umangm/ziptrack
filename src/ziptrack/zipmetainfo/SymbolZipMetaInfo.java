package ziptrack.zipmetainfo;

import ziptrack.grammar.Symbol;

public abstract class SymbolZipMetaInfo extends Symbol {
	protected String name;

	SymbolZipMetaInfo(String n){
		super(n);
		this.name = n;
	}

	public String getName(){
		return this.name;
	}
}
