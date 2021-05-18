package syntacticAnalyser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lexicAnalyser.Token;
import lexicAnalyser.Word;
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
	
	
	public SyntacticAnalyser(List<Token> tokens) {
		this.tokens = tokens; //recebe a lista de tokens
		lookahead = tokens.get(0); // começa com o primeiro token
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
	
	
	public void analyseCode(){
		//analise atras de bloco de const
		analyseConst();
		analyseVar();
		analyseFunctionAndProcedureDeclaration();
		analyseStructDecl();
		analyseTypedef();
		analyseFuncionStart();
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
		body(true);
		if(lookahead == null) return;
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead,"}",Tag.DEL);
			System.out.println("Start");
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
		attributeList("const");
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
	
	public void analyseVar() {
		if(getLexeme(lookahead).equals("var")) {
			match(lookahead, "var", Tag.PRE);
		} else return;
		if(getLexeme(lookahead).equals("{")) {
			System.out.println(getLexeme(lookahead));
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
		
		attributeList("var");
		
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead, "}", Tag.DEL);
			System.out.println("Encontrou var");
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
				attributeList("var");				
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
				System.out.println("Encontrou struct");
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
				System.out.println("Encontrou read");
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
					System.out.println("entrou");
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
					error("Esperava encontrar uma string ou uma expressão na linha " + lookahead.line, symbSyncronization);
				}
					
				if(getLexeme(lookahead).equals(",")) {
					match(lookahead, ",", Tag.DEL);
					continue;
				}
				
				else if(getLexeme(lookahead).equals(")")) {
					match(lookahead, ")", Tag.DEL);
					System.out.println("alguma coisa diferente " + getLexeme(lookahead));
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
	
	public void attributeList(String type) {
		//laço que permite a verificação de linhas que possam ser atributos
		List<String> keyWords = Arrays.asList("int","string","real","boolean","struct");
		while(true) {
			if(type == "const") {
				if(lookahead.tag == Tag.IDE || keyWords.contains(getLexeme(lookahead))) {
					attribute();
				}
				else return;
			}
			else if (type == "var"){
				if(lookahead.tag == Tag.PRE) {
					attributeVar();
				}
				else return;
			}
		}	
	}
	
	public void attribute() {
		switch(getLexeme(lookahead)) {
			case "struct":
				match(lookahead,"struct",Tag.PRE);
				attributeValue();
				break;
			case "int":
				match(lookahead,"int",Tag.PRE);
				attributeValue();
				break;
			case "real":
				match(lookahead,"real",Tag.PRE);
				attributeValue();
				break;
			case "boolean":
				match(lookahead,"boolean",Tag.PRE);
				attributeValue();
				break;
			case "string":
				match(lookahead,"string",Tag.PRE);
				attributeValue();
				break;	
			default:
				if(lookahead.tag == Tag.IDE) {
					match(lookahead,null,Tag.IDE);
					attributeValue();
				}
		}
	}
	
	public void attributeVar() {
		switch(getLexeme(lookahead)) {
			case "struct":
				match(lookahead,"struct",Tag.PRE);
				if(lookahead.tag == Tag.IDE)
					match(lookahead,null,Tag.IDE);
				attributeValueVar();
				break;
			case "int":
				match(lookahead,"int",Tag.PRE);
				attributeValueVar();
				break;
			case "real":
				match(lookahead,"real",Tag.PRE);
				attributeValueVar();
				break;
			case "boolean":
				match(lookahead,"boolean",Tag.PRE);
				attributeValueVar();
				break;
			case "string":
				match(lookahead,"string",Tag.PRE);
				attributeValueVar();
				break;
			default:
				match(lookahead,null,Tag.PRE);
				attributeValueVar();
				break;
		}
	}
	
	public void attributeValue() {
		while(true) {
			if(lookahead.tag == Tag.IDE){
				match(lookahead, null, Tag.IDE);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word("=",Tag.REL,-1),
						new Word(",",Tag.DEL,-1),
						new Word(";",Tag.DEL,-1));
				error("Esperava encontrar um identificador no processo de atribuição, na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals("=")) {
				match(lookahead, "=", Tag.REL);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.NRO,-1),
						new Word(" ",Tag.CAD,-1),
						new Word("true",Tag.PRE,-1),
						new Word("false",Tag.PRE,-1));
				error("Esperava encontrar o símbolo de = no processo de atribuição, na linha " + lookahead.line, symbSyncronization);
			}
			if(lookahead.tag == Tag.NRO || lookahead.tag == Tag.IDE || lookahead.tag == Tag.PRE || getLexeme(lookahead).equals("-")) {
				expression();	
			}
			else if(lookahead.tag == Tag.CAD) {
				match(lookahead,null,Tag.CAD);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.NRO,-1),
						new Word(";",Tag.DEL,-1),
						new Word(",",Tag.DEL,-1));
				error("Esperava encontrar um número,identificador,true,false ou uma cadeia de caracteres no processo de atribuição, na linha " + lookahead.line, symbSyncronization);
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
				error("Esperava encontrar o símbolo de , ou ; no processo de atribuição, na linha " + lookahead.line, symbSyncronization);
				if(lookahead.tag == Tag.IDE) continue; 
				else break;
			}
		}
	}
	
	public void attributeValueVar() {
		while(true) {
			//System.out.println(getLexeme(lookahead));
			if(lookahead.tag == Tag.IDE){
				match(lookahead, null, Tag.IDE);
				
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word (" ", Tag.IDE, -1),
						new Word ("=", Tag.REL, -1),
						new Word (",", Tag.DEL, -1),
						new Word ("[", Tag.DEL, -1),
						new Word ("]", Tag.DEL, -1),
						new Word (";", Tag.DEL, -1)
						);
				error("Esperava encontrar um identificador no processo de atribuição, na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals("[")) {
				int count = 0;
				while(count < 2) {
					if(getLexeme(lookahead).equals("[")) {
						match(lookahead, "[", Tag.DEL);
						if(lookahead.tag == Tag.NRO) {
							match(lookahead, null, Tag.NRO);
							
						}
						if(getLexeme(lookahead).equals("]")) {
							match(lookahead, "]", Tag.DEL);
							count++;
						}
						else if(lookahead.tag == Tag.IDE) {
							match(lookahead, null, Tag.IDE);
							if(getLexeme(lookahead).equals("]")) {
								match(lookahead, "]", Tag.DEL);
								count++;
							}
						}
						else {
							List<Token> symbSyncronization = Arrays.asList(
									new Word (",", Tag.DEL, -1),
									new Word ("[", Tag.DEL, -1),
									new Word ("int",Tag.PRE,-1),
									new Word ("boolean",Tag.PRE,-1),
									new Word ("real",Tag.PRE,-1),
									new Word ("string",Tag.PRE,-1),
									new Word ("struct",Tag.PRE,-1),
									new Word ("}",Tag.DEL,-1), 
									new Word (";", Tag.DEL, -1)
									);
							error("Esperava encontrar um ], identificador ou um número no processo de atribuição de vetor ou matriz, na linha " + lookahead.line, symbSyncronization);
						}
					}
					else count = 2;
				}
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
				
				if(lookahead.tag == Tag.NRO || lookahead.tag == Tag.IDE || lookahead.tag == Tag.PRE || getLexeme(lookahead).equals("-")) {
					expression();	
				}
				else if(lookahead.tag == Tag.CAD) {
					match(lookahead,null,Tag.CAD);
				}
				else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(" ",Tag.NRO,-1),
							new Word(";",Tag.DEL,-1),
							new Word(",",Tag.DEL,-1));
					error("Esperava encontrar um número, identificador, true, false ou uma cadeia de caracteres no processo de atribuição, na linha " + lookahead.line, symbSyncronization);
					System.out.println("errou aqui");
				}
			}
			/*else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.NRO,-1),
						new Word(" ",Tag.CAD,-1),
						new Word("[", Tag.DEL, -1),
						new Word("]", Tag.DEL, -1),
						new Word("true",Tag.PRE,-1),
						new Word("false",Tag.PRE,-1));
				error("Esperava encontrar o símbolo de = no processo de atribuição, na linha " + lookahead.line, symbSyncronization);
			}*/
							
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
				error("Esperava encontrar o símbolo de , ou ; no processo de atribuição, na linha " + lookahead.line, symbSyncronization);
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
				if(keyWords.contains(getLexeme(lookahead))){
					int indexKeyWord = keyWords.indexOf(getLexeme(lookahead));
					match(lookahead,keyWords.get(indexKeyWord),Tag.PRE);
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(" ",Tag.IDE,-1));
					error("Esperava encontrar int,real,string,boolean no processo de atribuição, na linha " + lookahead.line, symbSyncronization);
				}
				if(lookahead.tag == Tag.IDE) {
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
				paramsList();
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
				body(false);
				if(getLexeme(lookahead).equals("}") && lookahead.tag == Tag.DEL) {
					match(lookahead,"}",Tag.DEL);
				}
			}
			else if(getLexeme(lookahead).equals("procedure") && lookahead.tag == Tag.PRE) {
				match(lookahead,"procedure",Tag.PRE);
				if(lookahead.tag == Tag.IDE) {
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
				paramsList();
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
				body(true);
				if(getLexeme(lookahead).equals("}") && lookahead.tag == Tag.DEL) {
					match(lookahead,"}",Tag.DEL);
				}
			}
			else return;
		}
	}
	
	
	public void paramsList() {
		List<String> keyWords = Arrays.asList("int","string","real","boolean");
		while(true) {
			if(keyWords.contains(getLexeme(lookahead))){
				int indexKeyWord = keyWords.indexOf(getLexeme(lookahead));
				match(lookahead,keyWords.get(indexKeyWord),Tag.PRE);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1));
				error("Esperava encontrar int,real,string,boolean  na linha " + lookahead.line, symbSyncronization);
			}
			if(lookahead.tag == Tag.IDE) {
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
	
	
	public void body(boolean isProcedure) {
		while(true) {
			//System.out.println(getLexeme(lookahead) + " l: " + lookahead.line);
			if(lookahead.tag == Tag.END) {
				error("Esperava encontrar uma } para finalizar o start", null);
				break;
			}
			if(getLexeme(lookahead).equals("var")) {
				analyseVar();
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
					error("Procedure não pode ter return ( linha " + lookahead.line + " )", symbSyncronization);
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
			System.out.println("l: " + getLexeme(lookahead));
		}
		body(isProcedure);
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
			body(isProcedure);
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
			logicalExpression();
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
			body(isProcedure);
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
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead, "}", Tag.DEL);
			System.out.println("while");
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
			//System.out.println("a: " + getLexeme(lookahead) + " t: " + Tag.ART);
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
			//System.out.println("t: " + getLexeme(lookahead));
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
		else if(lookahead.tag == Tag.NRO) {
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
		else if(getLexeme(lookahead).equals("true")) match(lookahead,"true",Tag.PRE);
		else if(getLexeme(lookahead).equals("false")) match(lookahead,"false",Tag.PRE);
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
			error("Esperava encontrar um identificador,número,cadeia de caractéres,true,false,global., local. ou - como valor dentro de uma expressão na linha " + lookahead.line, symbSyncronization);
			//System.out.println("t: " + getLexeme(lookahead));
		}
	}
	
	public void multExp() {
		if(getLexeme(lookahead).equals("*")) {
			match(lookahead,"*",Tag.ART);
			
		}
		else if(getLexeme(lookahead).equals("/")) {
			match(lookahead,"/",Tag.ART);
		}
	}
	
	public void addExp(){
		if(getLexeme(lookahead).equals("+")) {
			match(lookahead,"+",Tag.ART);
			
		}
		else if(getLexeme(lookahead).equals("-")) {
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
					error("Esperava encontrar uma cadeia de caractéres " + lookahead.line, symbSyncronization);	
				}
			}
			else if(getLexeme(lookahead).equals("!")) {
				match(lookahead,"!", Tag.LOG);
				if(getLexeme(lookahead).equals("=")) {
					match(lookahead,"=", Tag.REL);
					if(lookahead.tag == Tag.CAD) {
						match(lookahead, null, Tag.CAD);
					}else {
						System.out.println("teste");
						List<Token> symbSyncronization = Arrays.asList(
								new Word(")",Tag.DEL,-1),
								new Word(";",Tag.DEL,-1));
						error("Esperava encontrar uma cadeia de caractéres " + lookahead.line, symbSyncronization);	
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
