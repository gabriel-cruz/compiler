package lexicAnalyser;

public class Token {
	public final int tag;
	public final int line;
	
	public Token (int t, int l) {
		tag = t;
		line = l;
	}
	
	public String toString(){
		return "" + (char)tag;
	}
}
