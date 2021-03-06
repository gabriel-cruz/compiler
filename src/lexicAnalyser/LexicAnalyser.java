package lexicAnalyser;

import java.util.ArrayList;
import java.util.List;

public class LexicAnalyser {

	//List<Symbol> symbolsTable = new ArrayList<Symbol>();
	
	public LexicAnalyser(/*List<String> infoSymbol*/){
		/*for(String symbol : infoSymbol) {
			//System.out.println(symbol);
			symbolsTable.add(new Symbol(symbol));
		}*/
	}
	
	public void analyseCode(List<String> code){
		int lineNumber = 0;
		int initialPointer = 0;
		int finalPointer = 0;
		
		code = removeSpacesAndComments(code);
		for(String line : code){
			System.out.println("line: " + lineNumber + " text: " + line);
			lineNumber++;
			
			while(finalPointer < line.length()) {
				char peek = line.charAt(finalPointer);
				
				if(Character.isLetter(peek) || Character.isDigit(peek)){
					finalPointer += 1;
				}else {
					//pegar a substring do index initialpointer até o finalpointer -1 e analisar o token
					switch(peek) {
						/*caso encontre um símbolo, o initialpointer vai para esse simbolo e verifica caso tenha outro símbolo
						
						*/
					case '+':
						
					}
				}
					
				
			}
			
		}
	}
	
	private List<String> removeSpacesAndComments(List<String> code){
		List<String> result = new ArrayList<String>();
		boolean isLineComment = false;
		boolean isBlockComment = false;
		//boolean isBlockFinal = false;
		
		for(String line : code){
			String[] notWithSpace = line.split("[ \t]");
			
			//int index = symbolsTable.indexOf(new Symbol(",CL"));
			//String regex = symbolsTable.get(index).getRegex();
			for(String check : notWithSpace){
				if(check.matches("^[//]{2}")) isLineComment = true;
								
				else if(check.matches("^[/][*][\\s\\S]*")) isBlockComment = true;
				else if(check.matches("^[\\s\\S]+[*][/]")) isBlockComment = false;
				
				if(!isLineComment && !isBlockComment) result.add(check);
			}
		}
		return result;
	}
	
}
