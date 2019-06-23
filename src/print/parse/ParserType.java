package print.parse;

public enum ParserType {
	RV, RR, STD;
	
	public boolean isRV(){
		return this.ordinal() == RV.ordinal();
	}
	
	public boolean isRR(){
		return this.ordinal() == RR.ordinal();
	}
	
	public boolean isSTD(){
		return this.ordinal() == STD.ordinal();
	}

	public boolean isLogType() {
		return this.isRR() || this.isSTD();
	}
	
	public boolean isBinType() {
		return this.isRV();
	}
	
	public String toString(){
		String str = "";
		if(isRV()) str = "RV";
		else if (isRR()) str = "RR";
		else if (isSTD()) str = "STD";
		return str;
	}
	
	public static ParserType getType(String str){
		if (str.equals("rr")) return RR;
		else if (str.equals("std")) return STD;
		else return RV;
	} 
}
