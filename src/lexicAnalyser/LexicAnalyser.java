package lexicAnalyser;

import java.util.ArrayList;
import java.util.List;

public class LexicAnalyser {

	List<Symbol> symbolsTable = new ArrayList<Symbol>();
	
	public LexicAnalyser(List<String> infoSymbol){
		for(String symbol : infoSymbol) {
			//System.out.println(symbol);
			symbolsTable.add(new Symbol(symbol));
		}
	}
	
	public void analyseCode(List<String> code){
		for(Symbol regex : symbolsTable) {
			System.out.println(regex.getSymbol());
		}
	}
	
}
