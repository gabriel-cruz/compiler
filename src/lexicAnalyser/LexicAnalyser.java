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
		int lineNumber = 0;
		code = removeSpacesAndComments(code);
		for(String line : code){
			System.out.println("line: " + lineNumber + " text: " + line);
			lineNumber++;
		}
	}
	
	private List<String> removeSpacesAndComments(List<String> code){
		List<String> result = new ArrayList<String>();
		boolean isLineComment = false;
		for(String line : code){
			String[] notWithSpace = line.split("[ \t]");
			int index = symbolsTable.indexOf(new Symbol(",CL"));
			String regex = symbolsTable.get(index).getRegex();
			for(String check : notWithSpace){
				if(check.matches(regex)) isLineComment = true;
				if(!isLineComment) result.add(check);
			}
		}
		return result;
	}
	
}
