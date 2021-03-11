package lexicAnalyser;

import java.util.LinkedList;

public class ReservedWord {
	public static LinkedList<String> words = new LinkedList<String>();
	
	
	public ReservedWord() {
		words.add("var");
		words.add("const");
		words.add("typedef");
		words.add("struct");
		words.add("extends");
		words.add("procedure");
		words.add("function");
		words.add("start");
		words.add("return");
		words.add("then");
		words.add("int");
		words.add("real");
		words.add("boolean");
		words.add("string");
		words.add("true");
		words.add("false");
		words.add("global");
		words.add("local");
		words.add("if");
		words.add("false");
		words.add("while");
		words.add("print");
		words.add("read");
	}
}
