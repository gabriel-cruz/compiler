package lexicAnalyser;

public class Number extends Token{
	public final int value;
	
	public Number(int v, int l) { 
		super(Tag.NRO, l); 
		value = v;
	}
	
	public String toString() { 
		return "" + super.line + "-" + super.tag + "-" + value + "\n"; 
	}
}
