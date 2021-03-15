package lexicAnalyser;

public class Word extends Token{
	public String lexeme = "";
	
	public Word(String s, int tag, int l) {
		super(tag, l);
		lexeme = s;
	}
	
	public String toString() {
		return "<" + super.line + " " + super.getSymbol() + " " + lexeme + ">\n"; 
	}
	
	/*public final Word and = new Word ("&&", Tag.LOG),
			or = new Word("||", Tag.LOG), eq = new Word("==", Tag.LOG),
			dif = new Word("!=", Tag.LOG), le = new Word("<=", Tag.LOG),
			ge = new Word(">=", Tag.LOG); */
}
