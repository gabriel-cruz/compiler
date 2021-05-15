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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((lexeme == null) ? 0 : lexeme.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Word other = (Word) obj;
		if (lexeme == null) {
			if (other.lexeme != null)
				return false;
		} else if (!lexeme.equals(other.lexeme))
			return false;
		return true;
	}
	
	/*public final Word and = new Word ("&&", Tag.LOG),
			or = new Word("||", Tag.LOG), eq = new Word("==", Tag.LOG),
			dif = new Word("!=", Tag.LOG), le = new Word("<=", Tag.LOG),
			ge = new Word(">=", Tag.LOG); */
	
}
