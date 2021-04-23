package syntacticAnalyser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lexicAnalyser.Token;
import lexicAnalyser.Word;
import lexicAnalyser.Number;

public class SyntacticAnalyser {

	private List<Token> tokens = new ArrayList<Token>();;

	public SyntacticAnalyser(List<Token> tokens) {
		this.tokens = tokens;
	}
	
	public List<Token> filterByLine(int line){
		return tokens.stream().filter(t -> t.line == line).collect(Collectors.toList());
	}
	
	public String getLexeme(Token token) {
		if(token.getClass() == Word.class) {
			Word word = (Word) token;
			return word.lexeme;
		}
		else if(token.getClass() == Number.class) {
			Number number = (Number) token;
			return Integer.toString(number.value);
		}else
			return " ";
	}
	
}
