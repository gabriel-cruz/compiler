package lexicAnalyser;

public class Real extends Token {
	public final float value;
	
	public Real(float v, int l) { 
		super(Tag.NRO, l); 
		value = v; 
	
	}
	public String toString() { 
		return "" + value; 
	}
}
