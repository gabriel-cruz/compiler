package lexicAnalyser;

public class Symbol {
	
	private String regex;
	private String symbol;
	
	public Symbol(String info){
		String[] split = info.split(",");
		this.regex = split[0];
		this.symbol = split[1];
	}

	public String getRegex() {
		return regex;
	}

	public String getSymbol() {
		return symbol;
	}

}
