package lexicAnalyser;

public class Token {
	
	public final int tag;
	public final int line;
	
	public Token (int t, int l) {
		tag = t;
		line = l;
	}
	
	protected String getSymbol(){
		String symbol = "";
		switch(tag) {
			case 256: symbol = "IDE";break;
			case 257: symbol = "NRO";break;
			case 258: symbol = "DEL";break;
			case 259: symbol = "REL";break;
			case 260: symbol = "LOG";break;
			case 261: symbol = "ART";break;
			case 262: symbol = "SIB";break;
			case 263: symbol = "CMF";break;
			case 264: symbol = "NMF";break;
			case 265: symbol = "CoMF";break;
			case 266: symbol = "OpMF";break;
			case 267: symbol = "CAD";break;
			case 268: symbol = "PRE";break;
		}
		return symbol;
	}
	
	
}
