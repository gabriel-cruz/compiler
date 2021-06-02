package syntacticAnalyser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import lexicAnalyser.Token;
import lexicAnalyser.Word;
import symbolTable.VariableSymbolRow;
import symbolTable.FunctionTableSymbol;
import lexicAnalyser.Number;
import lexicAnalyser.Tag;

public class SyntacticAnalyser {

	//lista de tokens detectado no codigo fonte
	private List<Token> tokens = new ArrayList<Token>();
	//lista de erros sintaticos
	private List<String> erros = new ArrayList<String>();
	// index que controla qual token será pego na lista
	private int index = 0;
	// armazena o token da frente
	private Token lookahead;
	//HashTable para a tabela de s�mbolos
	Hashtable<String, VariableSymbolRow> symbolTable = new Hashtable<String, VariableSymbolRow>();
	//Hashtable para a tabela de simbolos para funcao
	Hashtable<String, FunctionTableSymbol> functionTableSymbol = new Hashtable<String, FunctionTableSymbol>();
	//Hashtable para a tabela de simbolos dos tipos dos dados
	Hashtable<String, Integer> typeDataTableSymbol = new Hashtable<String, Integer>();
	//lista para verifica��o semantica da equa��o
	List<Token> lookExpression = new ArrayList<Token>();
	
	public SyntacticAnalyser(List<Token> tokens) {
		this.tokens = tokens; //recebe a lista de tokens
		lookahead = tokens.get(0); // começa com o primeiro token
		typeDataTableSymbol.put("int", Tag.NRO);
		typeDataTableSymbol.put("string", Tag.CAD);
		typeDataTableSymbol.put("boolean", Tag.PRE);
		typeDataTableSymbol.put("real",Tag.NRO);
	}
	
	public List<String> getErros() {
		return erros;
	}
	
	//método que pega o lexema do token passado como argumento
	public String getLexeme(Token token) {
		// verifica qual sub-classe pertence o token
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
	
	//método que realiza a comparação do que recebeu com o que era esperado
	//obs: quando quiser comparar numero passa o waited como null que ai ele faz a comparação só pela TAG
	public void match(Token token, String waited, int type) {
		if(getLexeme(token).equals(waited) && type == token.tag) {
			if(index < tokens.size() - 1) {
				//pega o token da frente
				index++;
				lookahead = tokens.get(index);
			}
			else lookahead = new Word(" ",Tag.END,-1);
		}
		else if(waited == null && type == token.tag) {
			if(index < tokens.size() - 1) {
				//pega o token da frente
				index++;
				lookahead = tokens.get(index);
			}
			else lookahead = new Word(" ",Tag.END,-1);
		}
	}
	
	public void error(String msgError, List<Token> symbSyncronization) {
		erros.add(msgError);
		//procura os tokens de sincronização para poder continuar a verificação
		if(symbSyncronization == null) return;
		while(true) {
			if(index == tokens.size() - 1) break;
			if(lookahead.tag == Tag.IDE) {
				if(symbSyncronization.contains(new Word(" ", Tag.IDE, -1)))break;
			}
			if(lookahead.tag == Tag.NRO) {
				if(symbSyncronization.contains(new Word(" ", Tag.NRO, -1)))break;
			}
			if(lookahead.tag == Tag.CAD){
				if(symbSyncronization.contains(new Word(" ", Tag.CAD, -1)))break;
			}
			if(symbSyncronization.contains(lookahead)) {
				//achar o token com base somente na sua tag
				int aux  = symbSyncronization.indexOf(lookahead);
				if(getLexeme(symbSyncronization.get(aux)).equals(" "))break;
				//achar o token com base somente na sua tag e lexeme
				else {
					if(getLexeme(lookahead).equals(getLexeme(symbSyncronization.get(aux)))) {
						break;
					}
				}
			}
			index++;
			lookahead = tokens.get(index);
		}
	}
	
	//m�todo para verificar se todos os valores s�o do mesmo tipo
	public boolean lookExpression(int type, String scope) {
		//System.out.println("t: " + lookExpression.size());
		//lookExpression.forEach(element->System.out.println(getLexeme(element)));
		boolean isEqualType = lookExpression.stream().allMatch(element -> element.tag == type);
		lookExpression.clear();
		return isEqualType;
	}
	
	public void analyseCode(){
		while(true) {
			//analise atras de bloco de const
			if(getLexeme(lookahead).equals("const")) {
				analyseConst();
				continue;
			}
			else if(getLexeme(lookahead).equals("var")) {
				analyseVar("global");
				continue;
			}
			else if(getLexeme(lookahead).equals("function") || getLexeme(lookahead).equals("procedure")) {
				analyseFunctionAndProcedureDeclaration();
				continue;
			}
			else if(getLexeme(lookahead).equals("struct")) {
				analyseStructDecl();
				continue;
			}
			else if(getLexeme(lookahead).equals("typedef")) {
				analyseTypedef();
				continue;
			}
			if(getLexeme(lookahead).equals("start")) {
				analyseFuncionStart();
				// Iterating using enhanced for loop
		        for (Map.Entry<String, VariableSymbolRow> e : symbolTable.entrySet())
		            System.out.println(e.getKey() + " "
		                               + e.getValue().toString());
		        for (Map.Entry<String, FunctionTableSymbol> e : functionTableSymbol.entrySet())
		            System.out.println(e.getKey() + " "
		                               + e.getValue().toString());
				break;
			}
			else{
				erros.add("N�o encontrou a fun��o principal start");
				break;
			}
		}
	}
	
	public void analyseFuncionStart() {
		if(getLexeme(lookahead).equals("start")) {
			match(lookahead,"start",Tag.PRE);
		}
		if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
			match(lookahead,"(",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(")",Tag.DEL,-1),
					new Word("{",Tag.DEL,-1)
					);
			error("Esperava encontrar um ( na linha " + lookahead.line, symbSyncronization);
		}
		if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
			match(lookahead,")",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(")",Tag.DEL,-1),
					new Word("{",Tag.DEL,-1),
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
					);
			error("Esperava encontrar um ) na linha " + lookahead.line, symbSyncronization);
		}
		if(getLexeme(lookahead).equals("{")) {
			match(lookahead,"{",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(")",Tag.DEL,-1),
					new Word("{",Tag.DEL,-1),
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
					);
			error("Esperava encontrar um { na linha " + lookahead.line, symbSyncronization);
		}
		body(true,"start");
		if(lookahead == null) return;
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead,"}",Tag.DEL);
		}
	}
	
	public void analyseConst(){
		//verifica se o token da frente tem o lexema const
		if(getLexeme(lookahead).equals("const")) {
			//realiza a comparação do token esperado pelo recebido
			match(lookahead,"const",Tag.PRE);
			//verifica se o token da frente tem o lexema {
		} else return;
		if(getLexeme(lookahead).equals("{")) {
			match(lookahead,"{",Tag.DEL);
		}else {
			// a lista de sincronização são os tokens que tem a tag como PRE e lexeme (int,boolean,string,struct,real) e os first do não terminal pai
			List<Token> symbSyncronization = Arrays.asList(
					new Word("int",Tag.PRE,-1),
					new Word("boolean",Tag.PRE,-1),
					new Word("real",Tag.PRE,-1),
					new Word("string",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1),
					new Word("function",Tag.PRE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("procedure",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("start",Tag.PRE,-1));
			error("Esperava encontrar uma { depois do const na linha " + lookahead.line, symbSyncronization);
		}
		//função que realiza o procedimento de verificação de instâncias de atributos
		attributeList("const","global");
		//verifica se o token da frente tem o lexema }
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead,"}",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("function",Tag.PRE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("procedure",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("start",Tag.PRE,-1));
			error("Esperava encontrar uma } depois do const na linha " + lookahead.line, symbSyncronization);
		}
	}
	
	public void analyseVar(String scope) {
		if(getLexeme(lookahead).equals("var")) {
			match(lookahead, "var", Tag.PRE);
		} else return;
		if(getLexeme(lookahead).equals("{")) {
			match(lookahead, "{", Tag.DEL);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("int",Tag.PRE,-1),
					new Word("boolean",Tag.PRE,-1),
					new Word("real",Tag.PRE,-1),
					new Word("string",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1),
					new Word("function",Tag.PRE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("procedure",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("start",Tag.PRE,-1));
			error("Esperava encontrar uma { depois do var na linha " + lookahead.line, symbSyncronization);
		}
		
		attributeList("var",scope);
		
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead, "}", Tag.DEL);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("function",Tag.PRE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("procedure",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("start",Tag.PRE,-1));
			error("Esperava encontrar uma } depois do var na linha " + lookahead.line, symbSyncronization);
		}
	}
	
	public void analyseStructDecl() {
		while(true) {
			if(getLexeme(lookahead).equals("struct") && lookahead.tag == Tag.PRE) {
				match(lookahead, "struct", Tag.PRE);
				
			}else break;
			if(lookahead.tag == Tag.IDE) {
				match(lookahead, null, Tag.IDE);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word("extends", Tag.PRE, -1),
						new Word("{", Tag.DEL, -1),
						new Word("int",Tag.PRE,-1),
						new Word("boolean",Tag.PRE,-1),
						new Word("real",Tag.PRE,-1),
						new Word("string",Tag.PRE,-1)
						);
				error("Esperava encontrar um identificador depois do struct na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals("extends") && lookahead.tag == Tag.PRE) {
				match(lookahead, "extends", Tag.PRE);
				if(lookahead.tag == Tag.IDE) {
					match(lookahead, null, Tag.IDE);
					
				}
				else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("{", Tag.DEL, -1),
							new Word("int",Tag.PRE,-1),
							new Word("boolean",Tag.PRE,-1),
							new Word("real",Tag.PRE,-1),
							new Word("string",Tag.PRE,-1)
							);
					error("Esperava encontrar um identificador depois do extends na linha " + lookahead.line, symbSyncronization);
				}
				
			}
			if(getLexeme(lookahead).equals("{") && lookahead.tag == Tag.DEL) {
				match(lookahead, "{", Tag.DEL);
				attributeList("var","todo");				
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
					new Word("int",Tag.PRE,-1),
					new Word("boolean",Tag.PRE,-1),
					new Word("real",Tag.PRE,-1),
					new Word("string",Tag.PRE,-1),
					new Word("struct", Tag.PRE, -1),
					new Word("}",Tag.DEL,-1),
					new Word("function",Tag.PRE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("procedure",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("start",Tag.PRE,-1));
			error("Esperava encontrar um { na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals("}")) {
				match(lookahead, "}", Tag.DEL);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word("function", Tag.PRE, -1),
						new Word("var", Tag.PRE, -1),
						new Word("procedure", Tag.PRE, -1),
						new Word("struct", Tag.PRE, -1),
						new Word("start", Tag.PRE, -1)
						);
				error("Esperava encontrar um tipo de dado linha " + lookahead.line, symbSyncronization);
				return;
			}
		}
	}
	
	public void analyseTypedef() {
		List<String> keyWords = Arrays.asList("int","string", "struct","real","boolean");
		
		while(true) {
			if(getLexeme(lookahead).equals("typedef") && lookahead.tag == Tag.PRE) {
				match(lookahead, "typedef", Tag.PRE);
				
			}
			else break;
			
			if(keyWords.contains(getLexeme(lookahead))){
				int indexKeyWord = keyWords.indexOf(getLexeme(lookahead));
				match(lookahead,keyWords.get(indexKeyWord),Tag.PRE);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ", Tag.IDE, -1),
						new Word(";", Tag.DEL, -1)
						);
				error("Esperava encontrar um tipo de dado linha " + lookahead.line, symbSyncronization);
			}
			
			if(lookahead.tag == Tag.IDE) {
				match(lookahead,null,Tag.IDE);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(";", Tag.DEL, -1),
						new Word("function", Tag.PRE, -1),
						new Word("var", Tag.PRE, -1),
						new Word("procedure", Tag.PRE, -1),
						new Word("struct", Tag.PRE, -1),
						new Word("start", Tag.PRE, -1)
						);
				error("Esperava encontrar um identificador na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals(";")) {
				match(lookahead,";",Tag.DEL);	
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(";", Tag.DEL, -1),
						new Word("function", Tag.PRE, -1),
						new Word("var", Tag.PRE, -1),
						new Word("procedure", Tag.PRE, -1),
						new Word("struct", Tag.PRE, -1),
						new Word("start", Tag.PRE, -1)
						);
				error("Esperava encontrar um ; na linha " + lookahead.line, symbSyncronization);
			}
		}
	}
	
	public void analyseRead() {
		if(getLexeme(lookahead).equals("read") && lookahead.tag == Tag.PRE) {
			match(lookahead, "read", Tag.PRE);
			
		}
		
		if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
			match(lookahead, "(", Tag.DEL);	
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ", Tag.IDE, -1),
					new Word(",", Tag.DEL, -1),
					new Word(")", Tag.DEL, -1)
					);
			error("Esperava encontrar um ( na linha " + lookahead.line, symbSyncronization);
		}
		
		while(true) {
			if(lookahead.tag == Tag.IDE) {
				match(lookahead, null, Tag.IDE);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(";", Tag.DEL, -1),
						new Word(")", Tag.DEL, -1),
						new Word(",", Tag.DEL, -1),
						new Word("if", Tag.PRE, -1),
						new Word("while", Tag.PRE, -1),
						new Word("print", Tag.PRE, -1),
						new Word("}", Tag.DEL, -1)
						);
				error("Esperava encontrar um identificador na linha " + lookahead.line, symbSyncronization);
			}
			
			if(getLexeme(lookahead).equals(",") && lookahead.tag == Tag.DEL) {
				match(lookahead,",",Tag.DEL);
				continue;
			}
			
			else if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
				match(lookahead,")",Tag.DEL);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(";", Tag.DEL, -1),
						new Word("if", Tag.PRE, -1),
						new Word("while", Tag.PRE, -1),
						new Word("print", Tag.PRE, -1),
						new Word("}", Tag.DEL, -1)
						);
				error("Esperava encontrar um ) na linha " + lookahead.line, symbSyncronization);
			}
			
			if(getLexeme(lookahead).equals(";")) {
				match(lookahead,";",Tag.DEL);
				break;
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word("if", Tag.PRE, -1),
						new Word("while", Tag.PRE, -1),
						new Word("print", Tag.PRE, -1),
						new Word("}", Tag.DEL, -1)
						);
				error("Esperava encontrar ; na linha " + lookahead.line, symbSyncronization);
				break;
			}
		}
}
	
	public void analysePrint() {
			if(getLexeme(lookahead).equals("print") && lookahead.tag == Tag.PRE) {
				match(lookahead, "print", Tag.PRE);
				
			}
			if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
				match(lookahead, "(", Tag.DEL);
				
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ", Tag.IDE, -1),
						new Word(" ", Tag.CAD, -1),
						new Word(",", Tag.DEL, -1),
						new Word(")", Tag.DEL, -1)
						);
				error("Esperava encontrar um ( na linha " + lookahead.line, symbSyncronization);
			}
			
				
			while(true) {
				if(lookahead.tag == Tag.CAD) {
					match(lookahead,null,Tag.CAD);
				}
				else if (lookahead.tag == Tag.IDE || lookahead.tag == Tag.NRO) {
					expression();
				}
				else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(";", Tag.DEL, -1),
							new Word(")", Tag.DEL, -1),
							new Word(",", Tag.DEL, -1),
							new Word("if", Tag.PRE, -1),
							new Word("var", Tag.PRE, -1),
							new Word(" ", Tag.IDE, -1),
							new Word("while", Tag.PRE, -1),
							new Word("print", Tag.PRE, -1),
							new Word("}", Tag.DEL, -1)
							);
					error("Esperava encontrar uma string ou uma express�o na linha " + lookahead.line, symbSyncronization);
				}
					
				if(getLexeme(lookahead).equals(",")) {
					match(lookahead, ",", Tag.DEL);
					continue;
				}
				
				else if(getLexeme(lookahead).equals(")")) {
					match(lookahead, ")", Tag.DEL);
				}
				
				else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(";", Tag.DEL, -1),
							new Word("if", Tag.PRE, -1),
							new Word("while", Tag.PRE, -1),
							new Word("print", Tag.PRE, -1),
							new Word("}", Tag.DEL, -1)
							);
					error("Esperava encontrar um ) na linha " + lookahead.line, symbSyncronization);
					
				}
				
				if(getLexeme(lookahead).equals(";")) {
					match(lookahead, ";", Tag.DEL);
					break;
						
				}
				
				else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("if", Tag.PRE, -1),
							new Word("while", Tag.PRE, -1),
							new Word("print", Tag.PRE, -1),
							new Word("}", Tag.DEL, -1)
							);
					error("Esperava encontrar ; na linha " + lookahead.line, symbSyncronization);
					break;
				}
			}

	}
	
	public void attributeList(String type,String scope) {
		//la�o que permite a verifica��o de linhas que possam ser atributos
		List<String> keyWords = Arrays.asList("int","string","real","boolean","struct");
		while(true) {
			if(type == "const") {
				if(lookahead.tag == Tag.IDE || keyWords.contains(getLexeme(lookahead))) {
					attribute("global");
				}
				else break;

			}
			else if (type == "var"){
				if(lookahead.tag == Tag.IDE || keyWords.contains(getLexeme(lookahead))) {
					attributeVar(scope);
				}
				else break;
			}
		}	
	}
	
	public void attribute(String scope) {
		switch(getLexeme(lookahead)) {
			case "struct":
				match(lookahead,"struct",Tag.PRE);
				attributeValue("struct",scope);
				break;
			case "int":
				match(lookahead,"int",Tag.PRE);
				attributeValue("int",scope);
				break;
			case "real":
				match(lookahead,"real",Tag.PRE);
				attributeValue("real", scope);
				break;
			case "boolean":
				match(lookahead,"boolean",Tag.PRE);
				attributeValue("boolean",scope);
				break;
			case "string":
				match(lookahead,"string",Tag.PRE);
				attributeValue("string",scope);
				break;	
			default:
				if(lookahead.tag == Tag.IDE) {
					String type = getLexeme(lookahead);
					match(lookahead,null,Tag.IDE);
					attributeValue(type,scope);
				}	
		}
	}
	
	public void attributeVar(String scope) {
		switch(getLexeme(lookahead)) {
			case "struct":
				match(lookahead,"struct",Tag.PRE);
				if(lookahead.tag == Tag.IDE)
					match(lookahead,null,Tag.IDE);
				attributeValueVar("struct",scope);
				break;
			case "int":
				match(lookahead,"int",Tag.PRE);
				attributeValueVar("int",scope);
				break;
			case "real":
				match(lookahead,"real",Tag.PRE);
				attributeValueVar("real",scope);
				break;
			case "boolean":
				match(lookahead,"boolean",Tag.PRE);
				attributeValueVar("boolean",scope);
				break;
			case "string":
				match(lookahead,"string",Tag.PRE);
				attributeValueVar("string",scope);
				break;
			default:
				if(lookahead.tag == Tag.IDE) {
					String type = getLexeme(lookahead);
					match(lookahead,null,Tag.IDE);
					attributeValueVar(type,scope);
				}
				else{
					
				}
		}
	}
	
	public void attributeValue(String type, String scope) {
		VariableSymbolRow symbol;
		while(true){
			symbol = new VariableSymbolRow();
			if(lookahead.tag == Tag.IDE){
				if(symbolTable.contains(new VariableSymbolRow("", getLexeme(lookahead), "const",false, scope, type)))
					System.out.println("N�o Salvar: " + getLexeme(lookahead));
				else 
					symbol.setName(getLexeme(lookahead));
				match(lookahead, null, Tag.IDE);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word("=",Tag.REL,-1),
						new Word(",",Tag.DEL,-1),
						new Word(";",Tag.DEL,-1));
				error("Esperava encontrar um identificador no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals("=")) {
				match(lookahead, "=", Tag.REL);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.NRO,-1),
						new Word(" ",Tag.CAD,-1),
						new Word("true",Tag.PRE,-1),
						new Word("false",Tag.PRE,-1));
				error("Esperava encontrar o s�mbolo de = no processo de atribuia��o, na linha " + lookahead.line, symbSyncronization);
			}
			if(lookahead.tag == Tag.NRO || lookahead.tag == Tag.IDE || lookahead.tag == Tag.PRE || getLexeme(lookahead).equals("-")) {
				expression();
				boolean isOnlyType = lookExpression(typeDataTableSymbol.get(type),scope);
				if(symbol.getName() != null && isOnlyType)
					symbolTable.put("id" + index, new VariableSymbolRow("id" + index, symbol.getName(), "const", true, scope, type));
			}
			else if(lookahead.tag == Tag.CAD) {
				if(symbol.getName() != null && typeDataTableSymbol.get(type) == Tag.CAD)
					symbolTable.put("id" + index, new VariableSymbolRow("id" + index, symbol.getName(), "const", true, scope, type));
				match(lookahead,null,Tag.CAD);
			}
			else {
				symbolTable.put("id" + index, new VariableSymbolRow("id" + index, symbol.getName(), "const", false, scope, type));
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.NRO,-1),
						new Word(";",Tag.DEL,-1),
						new Word(",",Tag.DEL,-1));
				error("Esperava encontrar um número,identificador,true,false ou uma cadeia de caracteres no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals(",")) {
				match(lookahead,",",Tag.DEL);
			}
			else if(getLexeme(lookahead).equals(";")) {
				match(lookahead,";",Tag.DEL);
				return;
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word("int",Tag.PRE,-1),
						new Word("boolean",Tag.PRE,-1),
						new Word("real",Tag.PRE,-1),
						new Word("string",Tag.PRE,-1),
						new Word("struct",Tag.PRE,-1),
						new Word("}",Tag.DEL,-1),
						new Word("function",Tag.PRE,-1),
						new Word("var",Tag.PRE,-1),
						new Word("procedure",Tag.PRE,-1),
						new Word("struct",Tag.PRE,-1),
						new Word("start",Tag.PRE,-1)
						);
				error("Esperava encontrar o s�mbolo de , ou ; no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
				if(lookahead.tag == Tag.IDE) continue; 
				else break;
			}
		}
	}
	
	public void attributeValueVar(String type, String scope) {
		VariableSymbolRow symbol;
		boolean findError = false;
		while(true) {
			symbol = new VariableSymbolRow();
			symbol.setInitialized(false);
			if(lookahead.tag == Tag.IDE){
				//serve para verificar se o var � global e que n�o exista uma const com o mesmo nome
				boolean verify =  scope.equals("global") ? symbolTable.contains(new VariableSymbolRow("",getLexeme(lookahead),"const", false, "global", type)) : false;
				if(symbolTable.contains(new VariableSymbolRow("",getLexeme(lookahead),"var", false, scope, type)) || verify) {
					//System.out.println(getLexeme(lookahead)+ "-" + scope + "-" + type + "-" +symbolTable.contains(new SymbolRow("",getLexeme(lookahead),"var", false, scope, type)) + "-" + symbolTable.contains(new SymbolRow("",getLexeme(lookahead),"const", false, "global", type)));
					findError = true;
				}
				else {
					symbol.setName(getLexeme(lookahead));
				}
				match(lookahead, null, Tag.IDE);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						//new Word (" ", Tag.IDE, -1),
						new Word ("=", Tag.REL, -1),
						new Word (",", Tag.DEL, -1),
						new Word ("[", Tag.DEL, -1),
						//new Word ("]", Tag.DEL, -1),
						new Word (";", Tag.DEL, -1)
						);
				error("Esperava encontrar um identificador no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
			}
			
			if(getLexeme(lookahead).equals("[")) {
				int count = 0;
				while(count < 2) {
					if(getLexeme(lookahead).equals("[")) {
						match(lookahead, "[", Tag.DEL);
						if(lookahead.tag == Tag.NRO) {
							match(lookahead, null, Tag.NRO);

						}
						else if(lookahead.tag == Tag.IDE) {
							match(lookahead, null, Tag.IDE);
						}
						else {
							List<Token> symbSyncronization = Arrays.asList(
									new Word (",", Tag.DEL, -1),
									new Word ("]", Tag.DEL, -1)
									);
							error("Esperava encontrar um identificador ou um n�mero no processo de atribuição de vetor ou matriz, na linha " + lookahead.line, symbSyncronization);
						}
						if(getLexeme(lookahead).equals("]")) {
							match(lookahead, "]", Tag.DEL);
							count++;
						}
						else {
							List<Token> symbSyncronization = Arrays.asList(
									new Word (",", Tag.DEL, -1),
									new Word ("[", Tag.DEL, -1)
									);
							error("Esperava encontrar um ] processo de atribui��o de vetor ou matriz, na linha " + lookahead.line, symbSyncronization);
						}
					}
					else count = 2;
				}
				if(getLexeme(lookahead).equals("=")) {
					match(lookahead, "=", Tag.REL);
					if(getLexeme(lookahead).equals("{")) {
						match(lookahead, "{", Tag.DEL);
						if(getLexeme(lookahead).equals("{")) {
							attributeMatrix();
						}else {
							attributeVector();
						}
					}
				}
			}
			else if(getLexeme(lookahead).equals("=")) {
				match(lookahead, "=", Tag.REL);
				if(lookahead.tag == Tag.NRO || lookahead.tag == Tag.IDE || getLexeme(lookahead).equals("true") || getLexeme(lookahead).equals("false")) {
					lookExpression.clear();
					expression();
					symbol.setInitialized(true);
					boolean isOnlyType = lookExpression(typeDataTableSymbol.get(type),scope);
					System.out.println("analyse type: " + isOnlyType);
					if(symbol.getName() != null && isOnlyType)
						symbolTable.put("id" + index, new VariableSymbolRow("id" + index, symbol.getName(), "const", true, scope, type));
				}
				else if(lookahead.tag == Tag.CAD) {
					if(symbol.getName() != null && typeDataTableSymbol.get(type) == Tag.CAD)
						symbolTable.put("id" + index, new VariableSymbolRow("id" + index, symbol.getName(), "const", true, scope, type));
					match(lookahead,null,Tag.CAD);
				}
				else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word (",", Tag.DEL, -1),
							new Word (";", Tag.DEL, -1)
							);
					error("Esperava encontrar um número, identificador, string ou um boolean no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
				}
				
			}
			if(!findError)symbolTable.put("id" + index, new VariableSymbolRow("id" + index,symbol.getName(),"var",symbol.isInitialized(),scope,type));
			if(getLexeme(lookahead).equals(",")) {
				match(lookahead,",",Tag.DEL);
			}
			else if(getLexeme(lookahead).equals(";")) {
				match(lookahead,";",Tag.DEL);
				return;
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word("int",Tag.PRE,-1),
						new Word("boolean",Tag.PRE,-1),
						new Word("real",Tag.PRE,-1),
						new Word("string",Tag.PRE,-1),
						new Word("struct",Tag.PRE,-1),
						new Word("}",Tag.DEL,-1),
						new Word("function",Tag.PRE,-1),
						new Word("var",Tag.PRE,-1),
						new Word("procedure",Tag.PRE,-1),
						new Word("struct",Tag.PRE,-1),
						new Word("start",Tag.PRE,-1),
						new Word("if",Tag.PRE,-1),
						new Word("while",Tag.PRE,-1),
						new Word("print",Tag.PRE,-1),
						new Word("read",Tag.PRE,-1),
						new Word("global",Tag.PRE,-1),
						new Word("local",Tag.PRE,-1)
						);
				error("Esperava encontrar o s�mbolo de , ou ; no processo de atribuição, na linha " + lookahead.line, symbSyncronization);
				if(lookahead.tag == Tag.IDE) continue; 
				else break;
			}
			
		}
	}
	
	
	public void attributeVector() {
		while(true) {
			if(lookahead.tag == Tag.CAD) {
				match(lookahead, null, Tag.CAD);
				if(getLexeme(lookahead).equals(",")) {
					match(lookahead,",",Tag.DEL);
				}
			}
			else {
				expression();	
			}
			if(getLexeme(lookahead).equals(",")) {
				match(lookahead,",",Tag.DEL);
			}
			else break;
		}
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead,"}",Tag.DEL);
		}
	}
	
	public void attributeMatrix() {
		while(true) {
			if(getLexeme(lookahead).equals("{")) {
				match(lookahead,"{",Tag.DEL);
				attributeVector();
			}
			if(getLexeme(lookahead).equals(",")) {
				match(lookahead,",",Tag.DEL);
			}
			else break;
		}
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead,"}",Tag.DEL);
		}
	}
	
	public void analyseFunctionAndProcedureDeclaration() {
		List<String> keyWords = Arrays.asList("int","string","real","boolean");
		while(true) {
			if(getLexeme(lookahead).equals("function") && lookahead.tag == Tag.PRE) {
				match(lookahead,"function",Tag.PRE);
				FunctionTableSymbol function = new FunctionTableSymbol();
				function.setProcedure(false);
				if(keyWords.contains(getLexeme(lookahead))){
					int indexKeyWord = keyWords.indexOf(getLexeme(lookahead));
					function.setType(keyWords.get(indexKeyWord));
					match(lookahead,keyWords.get(indexKeyWord),Tag.PRE);
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(" ",Tag.IDE,-1));
					error("Esperava encontrar int,real,string,boolean no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
				}
				if(lookahead.tag == Tag.IDE) {
					function.setName(getLexeme(lookahead));
					functionTableSymbol.put("func" + index, new FunctionTableSymbol("func" + index, function.getName(), function.getType(), function.isProcedure()));
					match(lookahead,null,Tag.IDE);
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("(",Tag.DEL,-1),
							new Word(" ",Tag.IDE,-1)
							);
					error("Esperava encontrar um identificador no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
				}
				if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
					match(lookahead,"(",Tag.DEL);
				}
				paramsList(function);
				function.getParameters().forEach((key,value)-> System.out.println("key: " + key + " " + "v: " + value));
				if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
					match(lookahead,")",Tag.DEL);
				}
				if(getLexeme(lookahead).equals("{") && lookahead.tag == Tag.DEL) {
					match(lookahead,"{",Tag.DEL);
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("var",Tag.PRE,-1),
							new Word("if",Tag.PRE,-1),
							new Word("while",Tag.PRE,-1),
							new Word("global",Tag.PRE,-1),
							new Word("local",Tag.PRE,-1),
							new Word("read",Tag.PRE,-1),
							new Word("print",Tag.PRE,-1),
							new Word("return",Tag.PRE,-1)
							);
					error("Esperava encontrar { na linha " + lookahead.line, symbSyncronization);
				}
				body(false,function.getName());
				if(getLexeme(lookahead).equals("}") && lookahead.tag == Tag.DEL) {
					match(lookahead,"}",Tag.DEL);
				}
			}
			else if(getLexeme(lookahead).equals("procedure") && lookahead.tag == Tag.PRE) {
				FunctionTableSymbol procedure = new FunctionTableSymbol();
				match(lookahead,"procedure",Tag.PRE);
				procedure.setProcedure(true);
				procedure.setType("void");
				if(lookahead.tag == Tag.IDE) {
					procedure.setName(getLexeme(lookahead));
					functionTableSymbol.put("func" + index, new FunctionTableSymbol("func" + index, procedure.getName(), procedure.getType(), procedure.isProcedure()));
					match(lookahead,null,Tag.IDE);
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("(",Tag.DEL,-1),
							new Word(" ",Tag.IDE,-1)
							);
					error("Esperava encontrar um identificador no processo de atribuição, na linha " + lookahead.line, symbSyncronization);
				}
				if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
					match(lookahead,"(",Tag.DEL);
				}
				paramsList(procedure);
				procedure.getParameters().forEach((key,value)-> System.out.println("key: " + key + " " + "v: " + value));
				if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
					match(lookahead,")",Tag.DEL);
				}
				if(getLexeme(lookahead).equals("{") && lookahead.tag == Tag.DEL) {
					match(lookahead,"{",Tag.DEL);
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("var",Tag.PRE,-1),
							new Word("if",Tag.PRE,-1),
							new Word("while",Tag.PRE,-1),
							new Word("global",Tag.PRE,-1),
							new Word("local",Tag.PRE,-1),
							new Word("read",Tag.PRE,-1),
							new Word("print",Tag.PRE,-1),
							new Word("return",Tag.PRE,-1)
							);
					error("Esperava encontrar { na linha " + lookahead.line, symbSyncronization);
				}
				body(true,procedure.getName());
				if(getLexeme(lookahead).equals("}") && lookahead.tag == Tag.DEL) {
					match(lookahead,"}",Tag.DEL);
				}
			}
			else return;
		}
	}
	
	
	public void paramsList(FunctionTableSymbol function) {
		List<String> keyWords = Arrays.asList("int","string","real","boolean");
		String type="", name="";
		boolean error = false;
		while(true) {
			if(keyWords.contains(getLexeme(lookahead))){
				int indexKeyWord = keyWords.indexOf(getLexeme(lookahead));
				type = keyWords.get(indexKeyWord);
				match(lookahead,keyWords.get(indexKeyWord),Tag.PRE);
			}else {
				error = true;
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1));
				error("Esperava encontrar int,real,string,boolean  na linha " + lookahead.line, symbSyncronization);
			}
			if(lookahead.tag == Tag.IDE) {
				name = getLexeme(lookahead);
				if(!function.getParameters().containsKey(name))
					function.addParameter(type, name);
				else
					System.out.println("Ja salvou");
				match(lookahead,null,Tag.IDE);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word(",",Tag.DEL,-1),
						new Word(")",Tag.DEL,-1),
						new Word("{",Tag.DEL,-1)
						);
				error("Esperava encontrar int,real,string,boolean na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals(",") && lookahead.tag == Tag.DEL) {
				match(lookahead,",",Tag.DEL);
			}
			else if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL){
				return;
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word("var",Tag.PRE,-1),
						new Word("if",Tag.PRE,-1),
						new Word("while",Tag.PRE,-1),
						new Word("global",Tag.PRE,-1),
						new Word("local",Tag.PRE,-1),
						new Word("read",Tag.PRE,-1),
						new Word("print",Tag.PRE,-1),
						new Word("return",Tag.PRE,-1),
						new Word("{",Tag.DEL,-1)
						);
				error("Esperava encontrar int,real,string,boolean na linha " + lookahead.line, symbSyncronization);
				break;
			}
		}
	}
	
	
	public void body(boolean isProcedure, String nameFunction) {
		while(true) {
			if(lookahead.tag == Tag.END) {
				error("Esperava encontrar uma } para finalizar o start", null);
				break;
			}
			if(getLexeme(lookahead).equals("var")) {
				analyseVar(nameFunction);
			}
			else if(getLexeme(lookahead).equals("read")) {
				analyseRead();
			}
			else if(getLexeme(lookahead).equals("print")) {
				analysePrint();
			}
			else if(getLexeme(lookahead).equals("procedure")) {
				match(lookahead,"procedure",Tag.PRE);
				if(getLexeme(lookahead).equals(".")) {
					match(lookahead,".",Tag.DEL);
					if(lookahead.tag == Tag.IDE) {
						if(getLexeme(lookahead).equals("(")) {
							functionCall();
						}
					}else {
						List<Token> symbSyncronization = Arrays.asList(
								new Word(";",Tag.IDE,-1)
						);
						error("Esperava encontrar um identificador na linha " + lookahead.line, symbSyncronization);
					}
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(" ",Tag.IDE,-1)
					);
					error("Esperava encontrar um . na linha " + lookahead.line, symbSyncronization);
				}
			}
			else if(lookahead.tag == Tag.IDE ||getLexeme(lookahead).equals("global") || getLexeme(lookahead).equals("local")) {
				assign();
			}
			else if(getLexeme(lookahead).equals("if")) {
				analyseIfElse(isProcedure);
			}
			else if(getLexeme(lookahead).equals("while")) {
				analyseWhile(isProcedure);
			}
			else if(getLexeme(lookahead).equals("return")) {
				if(!isProcedure) {
					match(lookahead,"return", Tag.PRE);
					expression();
					if(getLexeme(lookahead).equals(";")) {
						match(lookahead,";",Tag.DEL);
					}
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(";",Tag.DEL,-1)
					);
					error("Procedure n�o pode ter return ( linha " + lookahead.line + " )", symbSyncronization);
					if(getLexeme(lookahead).equals(";")) {
						match(lookahead,";",Tag.DEL);
					}
				}
			}
			else if(getLexeme(lookahead).equals("}")) {
				break;
			}else {
				if(index == tokens.size() - 1) {
					error("Esperava encontrar uma } na linha " + lookahead.line, null);
					break;
				}
				else if(getLexeme(lookahead).equals("else")) {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("then",Tag.PRE,-1),
							new Word(" ",Tag.NRO,-1),
							new Word(" ",Tag.IDE,-1),
							new Word(" ",Tag.CAD,-1),
							new Word("var",Tag.PRE,-1),
							new Word("if",Tag.PRE,-1),
							new Word("while",Tag.PRE,-1),
							new Word("global",Tag.PRE,-1),
							new Word("local",Tag.PRE,-1),
							new Word("read",Tag.PRE,-1),
							new Word("print",Tag.PRE,-1),
							new Word("return",Tag.PRE,-1),
							new Word("}",Tag.DEL,-1),
							new Word("int",Tag.PRE,-1),
							new Word("boolean",Tag.PRE,-1),
							new Word("real",Tag.PRE,-1),
							new Word("string",Tag.PRE,-1),
							new Word("struct",Tag.PRE,-1),
							new Word("function",Tag.PRE,-1),
							new Word("var",Tag.PRE,-1),
							new Word("procedure",Tag.PRE,-1),
							new Word("struct",Tag.PRE,-1),
							new Word("start",Tag.PRE,-1));
					error("Esperava encontrar uma } depois na linha " + lookahead.line, symbSyncronization);	
				}
				else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("int",Tag.PRE,-1),
							new Word("boolean",Tag.PRE,-1),
							new Word("real",Tag.PRE,-1),
							new Word("string",Tag.PRE,-1),
							new Word("struct",Tag.PRE,-1),
							new Word("function",Tag.PRE,-1),
							new Word("var",Tag.PRE,-1),
							new Word("procedure",Tag.PRE,-1),
							new Word("struct",Tag.PRE,-1),
							new Word("start",Tag.PRE,-1));
					error("Esperava encontrar uma } depois na linha " + lookahead.line, symbSyncronization);
					break;
				}
			}
		}
	}
	
	
	public void analyseIfElse(boolean isProcedure) {
		if(getLexeme(lookahead).equals("if")) {
			match(lookahead,"if",Tag.PRE);
		}
		if(getLexeme(lookahead).equals("(")) {
			match(lookahead,"(",Tag.DEL);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("!",Tag.LOG,-1),
					new Word(" ",Tag.NRO,-1),
					new Word(" ",Tag.IDE,-1),
					new Word(" ",Tag.CAD,-1)
			);
			error("Esperava encontrar um ( na linha " + lookahead.line, symbSyncronization);
		}
		logicalExpression();
		if(getLexeme(lookahead).equals(")")) {
			match(lookahead,")",Tag.DEL);
		}else {
			//verificar
			List<Token> symbSyncronization = Arrays.asList(
					new Word("then",Tag.PRE,-1),
					new Word("{",Tag.DEL,-1),
					new Word(" ",Tag.IDE,-1),
					new Word(" ",Tag.CAD,-1),
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
			);
			error("Esperava encontrar um ) na linha " + lookahead.line, symbSyncronization);
		}
		if(getLexeme(lookahead).equals("then")) {
			match(lookahead,"then",Tag.PRE);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ",Tag.IDE,-1),
					new Word("{",Tag.DEL,-1),
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
			);
			error("Esperava encontrar um then na linha " + lookahead.line, symbSyncronization);
		}
		if(getLexeme(lookahead).equals("{")) {
			match(lookahead,"{",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ",Tag.IDE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
			);
			error("Esperava encontrar um { na linha " + lookahead.line, symbSyncronization);
		}
		body(isProcedure,"if");
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead,"}",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ",Tag.IDE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("else",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
			);
			error("Esperava encontrar um } na linha " + lookahead.line, symbSyncronization);
		}
		if(getLexeme(lookahead).equals("else")){
			match(lookahead,"else",Tag.PRE);
			if(getLexeme(lookahead).equals("{")) {
				match(lookahead,"{",Tag.DEL);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word("var",Tag.PRE,-1),
						new Word("if",Tag.PRE,-1),
						new Word("while",Tag.PRE,-1),
						new Word("global",Tag.PRE,-1),
						new Word("local",Tag.PRE,-1),
						new Word("read",Tag.PRE,-1),
						new Word("print",Tag.PRE,-1),
						new Word("return",Tag.PRE,-1),
						new Word("}",Tag.DEL,-1)
				);
				error("Esperava encontrar um { na linha " + lookahead.line, symbSyncronization);
			}
			body(isProcedure,"else");
			if(getLexeme(lookahead).equals("}")) {
				match(lookahead,"}",Tag.DEL);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word("var",Tag.PRE,-1),
						new Word("if",Tag.PRE,-1),
						new Word("while",Tag.PRE,-1),
						new Word("global",Tag.PRE,-1),
						new Word("local",Tag.PRE,-1),
						new Word("read",Tag.PRE,-1),
						new Word("print",Tag.PRE,-1),
						new Word("return",Tag.PRE,-1),
						new Word("else",Tag.PRE,-1),
						new Word("}",Tag.DEL,-1)
				);
				error("Esperava encontrar um } na linha " + lookahead.line, symbSyncronization);
			}
		}
	}
	
	public void analyseWhile(boolean isProcedure) {
		if(getLexeme(lookahead).equals("while")) {
			match(lookahead, "while", Tag.PRE);
		}
		if(getLexeme(lookahead).equals("(")) {
			match(lookahead, "(", Tag.DEL);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ", Tag.IDE, -1),
					new Word(" ", Tag.NRO, -1),
					new Word(")", Tag.DEL, -1),
					new Word("{", Tag.DEL, -1)
					);
			error("Esperava encontrar um ( na linha " + lookahead.line, symbSyncronization);
		}
		
		logicalExpression();
		
		if(getLexeme(lookahead).equals(")")) {
			match(lookahead, ")", Tag.DEL);
			
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("{", Tag.DEL, -1),
					new Word(" ", Tag.IDE, -1),
					new Word("}", Tag.DEL, -1)
					);
			error("Esperava encontrar um ) na linha " + lookahead.line, symbSyncronization);
		}
		if(getLexeme(lookahead).equals("{")) {
			match(lookahead, "{", Tag.DEL);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ", Tag.IDE, -1),
					new Word("if", Tag.PRE, -1),
					new Word("while", Tag.PRE, -1),
					new Word("print", Tag.PRE, -1),
					new Word("read", Tag.PRE, -1),
					new Word("}", Tag.DEL, -1)
					);
			error("Esperava encontrar um { na linha " + lookahead.line, symbSyncronization);
		}
		
		body(isProcedure,"while");
		
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead, "}", Tag.DEL);
		}
	}
	
	public void assign() {
		if(getLexeme(lookahead).equals("global") || getLexeme(lookahead).equals("local")) {
			prefixGlobalOrLocal();
		}
		else if(lookahead.tag == Tag.IDE) {
			match(lookahead,null,Tag.IDE);
			if(getLexeme(lookahead).equals("(")) functionCall();
			else if(getLexeme(lookahead).equals(".")) prefixStruct();
			else if(getLexeme(lookahead).equals("[")){
				int count = 0;
				while(count < 2) {
					if(getLexeme(lookahead).equals("[")) {
						match(lookahead, "[", Tag.DEL);
						if(lookahead.tag == Tag.NRO || lookahead.tag == Tag.IDE) expression();
						else {
							List<Token> symbSyncronization = Arrays.asList(
									new Word("]",Tag.DEL,-1),
									new Word("[",Tag.DEL,-1),
									new Word("=",Tag.REL,-1),
									new Word(" ",Tag.IDE,-1),
									new Word(" ",Tag.NRO,-1),
									new Word("]",Tag.CAD,-1)
							);
							error("Esperava encontrar um identificador ou número na linha " + lookahead.line, symbSyncronization);
						}
						if(getLexeme(lookahead).equals("]")) {
							match(lookahead, "]", Tag.DEL);
							count++;
						}else {
							List<Token> symbSyncronization = Arrays.asList(
									new Word("[",Tag.DEL,-1),
									new Word("=",Tag.REL,-1),
									new Word(" ",Tag.IDE,-1),
									new Word(" ",Tag.NRO,-1),
									new Word("]",Tag.CAD,-1)
							);
							error("Esperava encontrar um [ na linha " + lookahead.line, symbSyncronization);
						}
					}
					else count = 2;
				}
			}
		}
		if(getLexeme(lookahead).equals("=") && lookahead.tag == Tag.REL) {
			match(lookahead,"=",Tag.REL);
			expression();
			if(lookahead.tag == Tag.REL) {
				relational();
			}
		}
		if(getLexeme(lookahead).equals(";")) {
			match(lookahead,";",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
			);
			error("Esperava encontrar um ; na linha " + lookahead.line, symbSyncronization);
		}
	}
	
	
	public void functionCall(){
		if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
			match(lookahead,"(",Tag.DEL);
			if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
				match(lookahead,")",Tag.DEL);
			}else {
				while(true){
					expression();
					if(getLexeme(lookahead).equals(",") && lookahead.tag == Tag.DEL) {
						match(lookahead,",",Tag.DEL);
					}
					else if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
						match(lookahead,")",Tag.DEL);
						break;
					}else {
						List<Token> symbSyncronization = Arrays.asList(
								new Word("=",Tag.REL,-1),
								new Word(";",Tag.DEL,-1),
								new Word(",",Tag.DEL,-1),
								new Word("+",Tag.ART,-1),
								new Word("-",Tag.ART,-1),
								new Word("/",Tag.ART,-1),
								new Word("*",Tag.ART,-1),
								new Word(">",Tag.REL,-1),
								new Word("<",Tag.REL,-1),
								new Word(">=",Tag.REL,-1),
								new Word("<=",Tag.REL,-1),
								new Word("==",Tag.REL,-1),
								new Word("!=",Tag.REL,-1),
								new Word("&&",Tag.LOG,-1),
								new Word("||",Tag.LOG,-1));
						error("Esperava encontrar um , ou ) na linha " + lookahead.line, symbSyncronization);
						break;
					}
				}
			}
		}
	}
	
	public void prefixGlobalOrLocal() {
		if(getLexeme(lookahead).equals("global") && lookahead.tag == Tag.PRE) {
			match(lookahead,"global",Tag.PRE);
		}
		else if(getLexeme(lookahead).equals("local") && lookahead.tag == Tag.PRE) {
			match(lookahead,"local",Tag.PRE);
		}
		if(getLexeme(lookahead).equals(".") && lookahead.tag == Tag.DEL) {
			match(lookahead,".",Tag.DEL);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ",Tag.IDE,-1));
			error("Esperava encontrar um . depois do global ou local na linha " + lookahead.line, symbSyncronization);
		}
		if(lookahead.tag == Tag.IDE) {
			match(lookahead,null,Tag.IDE);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("=",Tag.REL,-1),
					new Word(";",Tag.DEL,-1),
					new Word(")",Tag.DEL,-1),
					new Word("+",Tag.ART,-1),
					new Word("-",Tag.ART,-1),
					new Word("/",Tag.ART,-1),
					new Word("*",Tag.ART,-1),
					new Word(">",Tag.REL,-1),
					new Word("<",Tag.REL,-1),
					new Word(">=",Tag.REL,-1),
					new Word("<=",Tag.REL,-1),
					new Word("==",Tag.REL,-1),
					new Word("!=",Tag.REL,-1)
					);
			error("Esperava encontrar um identificador depois do . na linha " + lookahead.line, symbSyncronization);
		}
	}
	
	public void prefixStruct() {
		match(lookahead,".",Tag.DEL);
		if(lookahead.tag == Tag.IDE) {
			match(lookahead,null,Tag.IDE);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("=",Tag.REL,-1),
					new Word(";",Tag.DEL,-1),
					new Word(",",Tag.DEL,-1),
					new Word(")",Tag.DEL,-1),
					new Word("+",Tag.ART,-1),
					new Word("-",Tag.ART,-1),
					new Word("/",Tag.ART,-1),
					new Word("*",Tag.ART,-1),
					new Word(">",Tag.REL,-1),
					new Word("<",Tag.REL,-1),
					new Word(">=",Tag.REL,-1),
					new Word("<=",Tag.REL,-1),
					new Word("==",Tag.REL,-1),
					new Word("!=",Tag.REL,-1),
					new Word("&&",Tag.LOG,-1),
					new Word("||",Tag.LOG,-1)
					);
			error("Esperava encontrar um identificador na linha " + lookahead.line, symbSyncronization);
		}
	}
	
	public void value() {
		if(getLexeme(lookahead).equals("global") || getLexeme(lookahead).equals("local")) {
			prefixGlobalOrLocal();
		}
		else if(lookahead.tag == Tag.NRO){
			lookExpression.add(lookahead);
			match(lookahead,null,Tag.NRO);
		}
		else if(lookahead.tag == Tag.IDE) {
			match(lookahead,null,Tag.IDE);
			if(getLexeme(lookahead).equals("(")) {
				functionCall();
			}
			else if(getLexeme(lookahead).equals(".")) {
				prefixStruct();
			}
		}
		else if(getLexeme(lookahead).equals("(")) {
			match(lookahead,"(",Tag.DEL);
			expression();
			if(getLexeme(lookahead).equals(")")) {
				match(lookahead,")",Tag.DEL);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(";",Tag.DEL,-1));
				error("Esperava encontrar um )b na linha " + lookahead.line, symbSyncronization);
			}
		}
		else if(getLexeme(lookahead).equals("true")) {
			lookExpression.add(lookahead);
			match(lookahead,"true",Tag.PRE);
		}
		else if(getLexeme(lookahead).equals("false")) {
			lookExpression.add(lookahead);
			match(lookahead,"false",Tag.PRE);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("=",Tag.REL,-1),
					new Word(";",Tag.DEL,-1),
					new Word(",",Tag.DEL,-1),
					new Word(")",Tag.DEL,-1),
					new Word("+",Tag.ART,-1),
					new Word("-",Tag.ART,-1),
					new Word("/",Tag.ART,-1),
					new Word("*",Tag.ART,-1),
					new Word(">",Tag.REL,-1),
					new Word("<",Tag.REL,-1),
					new Word(">=",Tag.REL,-1),
					new Word("<=",Tag.REL,-1),
					new Word("==",Tag.REL,-1),
					new Word("!=",Tag.REL,-1),
					new Word("&&",Tag.LOG,-1),
					new Word("||",Tag.LOG,-1)
					);
			error("Esperava encontrar um identificador,n�mero,cadeia de caract�res,true,false,global., local. ou - como valor dentro de uma express�o na linha " + lookahead.line, symbSyncronization);
		}
	}
	
	public void multExp() {
		if(getLexeme(lookahead).equals("*")) {
			//lookExpression.add(lookahead);
			match(lookahead,"*",Tag.ART);
		}
		else if(getLexeme(lookahead).equals("/")) {
			//lookExpression.add(lookahead);
			match(lookahead,"/",Tag.ART);
		}
	}
	
	public void addExp(){
		if(getLexeme(lookahead).equals("+")) {
			//stack.addElement(lookahead);
			match(lookahead,"+",Tag.ART);
		}
		else if(getLexeme(lookahead).equals("-")){
			//stack.addElement(lookahead);
			match(lookahead,"-",Tag.ART);
		}
	}
	
	public void term() {
		if(getLexeme(lookahead).equals("/") ||  getLexeme(lookahead).equals("*")) {
			multExp();
		}
		else if(getLexeme(lookahead).equals("+") ||  getLexeme(lookahead).equals("-")) {
			addExp();
		}
	}
	
	public void relationalExpression() {
		if(lookahead.tag == Tag.CAD) {
			match(lookahead, null, Tag.CAD);
			if(getLexeme(lookahead).equals("==")) {
				match(lookahead,"==", Tag.REL);
				if(lookahead.tag == Tag.CAD) {
					match(lookahead, null, Tag.CAD);
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(")",Tag.DEL,-1),
							new Word(";",Tag.DEL,-1));
					error("Esperava encontrar uma cadeia de caract�res " + lookahead.line, symbSyncronization);	
				}
			}
			else if(getLexeme(lookahead).equals("!")) {
				match(lookahead,"!", Tag.LOG);
				if(getLexeme(lookahead).equals("=")) {
					match(lookahead,"=", Tag.REL);
					if(lookahead.tag == Tag.CAD) {
						match(lookahead, null, Tag.CAD);
					}else {
						List<Token> symbSyncronization = Arrays.asList(
								new Word(")",Tag.DEL,-1),
								new Word(";",Tag.DEL,-1));
						error("Esperava encontrar uma cadeia de caract�res " + lookahead.line, symbSyncronization);	
					}
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(" ",Tag.IDE,-1),
							new Word(" ",Tag.NRO,-1));
					error("Esperava encontrar um = " + lookahead.line, symbSyncronization);
				}
			}
		}else {
			relational();
			expression();
		}
	}
	
	public void relational() {
		switch(getLexeme(lookahead)){
			case ">":
			case ">=":
			case "<":
			case "<=":
			case "==":
				match(lookahead,null, Tag.REL);
				break;
			default:
				if(getLexeme(lookahead).equals("!")) {
					match(lookahead,"!", Tag.LOG);
					if(getLexeme(lookahead).equals("=")) {
						match(lookahead,"=", Tag.REL);
					}else {
						List<Token> symbSyncronization = Arrays.asList(
								new Word(" ",Tag.IDE,-1),
								new Word(" ",Tag.NRO,-1));
						error("Esperava encontrar um = " + lookahead.line, symbSyncronization);
					}
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(" ",Tag.IDE,-1),
							new Word(" ",Tag.NRO,-1),
							new Word(")",Tag.DEL,-1));
					error("Esperava encontrar um == ou expressão logica na linha " + lookahead.line, symbSyncronization);
				}
		}
		expression();
	}
	
	public void expression() {
		while(true) {
			if(getLexeme(lookahead).equals("&&") || getLexeme(lookahead).equals("||") || getLexeme(lookahead).equals(";")|| getLexeme(lookahead).equals(")"))break;
			value();
			if(getLexeme(lookahead).equals("/") ||  getLexeme(lookahead).equals("*") || getLexeme(lookahead).equals("+") ||  getLexeme(lookahead).equals("-")) {
				term();
			}else break;
		}
	}
	
	public void logicalExpression() {
		if(getLexeme(lookahead).equals("!")) {
			match(lookahead,"!", Tag.LOG);
		}
		if(getLexeme(lookahead).equals("true")) match(lookahead,"true",Tag.PRE);
		else if(getLexeme(lookahead).equals("false")) match(lookahead,"false",Tag.PRE);
		else if(getLexeme(lookahead).equals("global") || getLexeme(lookahead).equals("local")) {
			prefixGlobalOrLocal();
		}
		else if(lookahead.tag == Tag.IDE){
			expression();
			if(lookahead.tag == Tag.REL  || getLexeme(lookahead).equals("!")){
				relationalExpression();
			}
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(")",Tag.DEL,-1),
					new Word("{",Tag.DEL,-1),
					new Word("then",Tag.PRE,-1));
			error("Esperava encontrar um identificador,true,false,global. ou local. na linha" + lookahead.line, symbSyncronization);
		}
		logicalOperator();
	}
	
	public void logicalOperator() {
		if(getLexeme(lookahead).equals("&&")){
			match(lookahead,"&&",Tag.LOG);
			if(lookahead.tag == Tag.CAD) relationalExpression();
			else {
				expression();
				relationalExpression();
			}
		}
		else if(getLexeme(lookahead).equals("||")){
			match(lookahead,"||",Tag.LOG);
			if(lookahead.tag == Tag.CAD) relationalExpression();
			else {
				expression();
				relationalExpression();
			}
		}
	}
	
}
