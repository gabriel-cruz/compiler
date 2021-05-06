package syntacticAnalyser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//import com.sun.tools.javac.code.Attribute.Array;

import lexicAnalyser.Token;
import lexicAnalyser.Word;
import lexicAnalyser.Number;
import lexicAnalyser.Tag;

public class SyntacticAnalyser {

	//lista de tokens detectado no codigo fonte
	private List<Token> tokens = new ArrayList<Token>();
	// index que controla qual token será pego na lista
	private int index = 0;
	// armazena o token da frente
	private Token lookahead;
	
	
	public SyntacticAnalyser(List<Token> tokens) {
		this.tokens = tokens; //recebe a lista de tokens
		lookahead = tokens.get(0); // começa com o primeiro token
	}
	
	//método que realiza o filtro de tokens por linha
	public List<Token> filterByLine(int line){
		return tokens.stream().filter(t -> t.line == line).collect(Collectors.toList());
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
		}
		else if(waited == null && type == token.tag) {
			if(index < tokens.size() - 1) {
				//pega o token da frente
				index++;
				lookahead = tokens.get(index);
			}
		}
	}
	
	public void analyseCode(){
		//analise atras de bloco de const
		analyseConst();
		analyseVar();
		analyseFunctionAndProcedureDeclaration();
		analyseStructDecl();
		analyseFuncionStart();
		analyseTypedef();
	}
	
	public void analyseFuncionStart() {
		if(getLexeme(lookahead).equals("start")) {
			match(lookahead,"start",Tag.PRE);
			if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
				match(lookahead,"(",Tag.DEL);
				if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
					match(lookahead,")",Tag.DEL);
					if(getLexeme(lookahead).equals("{")) {
						System.out.println("testes");
						match(lookahead,"{",Tag.DEL);
						if(getLexeme(lookahead).equals("}")) {
							match(lookahead,"}",Tag.DEL);
							System.out.println("Start");
						}
					}
				}
			}
		}
	}
	
	public void analyseConst(){
		//verifica se o token da frente tem o lexema const
		if(getLexeme(lookahead).equals("const")) {
			//realiza a comparação do token esperado pelo recebido
			match(lookahead,"const",Tag.PRE);
			//verifica se o token da frente tem o lexema {
			if(getLexeme(lookahead).equals("{")) {
				match(lookahead,"{",Tag.DEL);
				//função que realiza o procedimento de verificação de instâncias de atributos
				attributeList("const");
				//verifica se o token da frente tem o lexema }
				if(getLexeme(lookahead).equals("}")) {
					match(lookahead,"}",Tag.DEL);
					System.out.println("Encontrou const");
				}
			}
		}
	}
	
	public void analyseVar() {
		if(getLexeme(lookahead).equals("var")) {
			match(lookahead, "var", Tag.PRE);
			
			if(getLexeme(lookahead).equals("{")) {
				match(lookahead, "{", Tag.DEL);
				
				attributeList("var");
				
				if(getLexeme(lookahead).equals("}")) {
					match(lookahead, "}", Tag.DEL);
					System.out.println("Encontrou var");
				}
			}
		}
	}
	
	public void analyseStructDecl() {
		while(true) {
			if(getLexeme(lookahead).equals("struct") && lookahead.tag == Tag.PRE) {
				match(lookahead, "struct", Tag.PRE);
				
				if(lookahead.tag == Tag.IDE) {
					match(lookahead, null, Tag.IDE);
					
					if(getLexeme(lookahead).equals("extends") && lookahead.tag == Tag.PRE) {
						match(lookahead, "extends", Tag.PRE);
						
						if(lookahead.tag == Tag.IDE) {
							match(lookahead, null, Tag.IDE);
							
							if(getLexeme(lookahead).equals("{") && lookahead.tag == Tag.DEL) {
								match(lookahead, "{", Tag.DEL);
								attributeList("var");
								
								if(getLexeme(lookahead).equals("}")) {
									match(lookahead, "}", Tag.DEL);
									System.out.println("Encontrou extends");
									break;
								}	
							}
						}
						
					}
					
					if(getLexeme(lookahead).equals("{") && lookahead.tag == Tag.DEL) {
						match(lookahead, "{", Tag.DEL);
						attributeList("var");
						
						if(getLexeme(lookahead).equals("}")) {
							match(lookahead, "}", Tag.DEL);
							System.out.println("Encontrou struct");
						}				
					}
					

				}
			}else break;
		}
	}
	
	public void analyseTypedef() {
		List<String> keyWords = Arrays.asList("int","string", "struct","real","boolean");
		
		while(true) {
			if(getLexeme(lookahead).equals("typedef") && lookahead.tag == Tag.PRE) {
				match(lookahead, "typedef", Tag.PRE);
				
				if(keyWords.contains(getLexeme(lookahead))){
					int indexKeyWord = keyWords.indexOf(getLexeme(lookahead));
					match(lookahead,keyWords.get(indexKeyWord),Tag.PRE);
					if(lookahead.tag == Tag.IDE) {
						match(lookahead,null,Tag.IDE);
						if(getLexeme(lookahead).equals(";")) {
							match(lookahead,";",Tag.DEL);
							System.out.println("Encontrou typedef");
						}
					}
				}
			}
			else break; 
		}
	}
	
	
	public void attributeList(String type) {
		//laço que permite a verificação de linhas que possam ser atributos
		while(true) {
			if(type == "const") {
				if(lookahead.tag == Tag.PRE) {
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
				match(lookahead,null,Tag.IDE);
				attributeValue();
				break;
		}
	}
	
	public void attributeVar() {
		switch(getLexeme(lookahead)) {
			case "struct":
				match(lookahead,"struct",Tag.PRE);
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
				if(getLexeme(lookahead).equals("=")) {
					match(lookahead, "=", Tag.REL);
					if(lookahead.tag == Tag.NRO) {
						match(lookahead,null,Tag.NRO);
						if(getLexeme(lookahead).equals(",")) {
							match(lookahead,",",Tag.DEL);
						}
						if(getLexeme(lookahead).equals(";")) {
							match(lookahead,";",Tag.DEL);
							return;
						}
					}
					if(lookahead.tag == Tag.PRE) {
						if(getLexeme(lookahead).equals("true")) match(lookahead,"true",Tag.PRE);
						if(getLexeme(lookahead).equals("false")) match(lookahead,"false",Tag.PRE);
						if(getLexeme(lookahead).equals(",")) {
							match(lookahead,",",Tag.DEL);
						}
						if(getLexeme(lookahead).equals(";")) {
							match(lookahead,";",Tag.DEL);
							return;
						}
					}
					if(lookahead.tag == Tag.CAD) {
						match(lookahead,null,Tag.CAD);
						if(getLexeme(lookahead).equals(",")) {
							match(lookahead,",",Tag.DEL);
						}
						if(getLexeme(lookahead).equals(";")) {
							match(lookahead,";",Tag.DEL);
							return;
						}
					}
					
				}
			}
		}
	}
	
	public void attributeValueVar() {
		while(true) {
			if(lookahead.tag == Tag.IDE){
				match(lookahead, null, Tag.IDE);
				if(getLexeme(lookahead).equals("[")) {
					while(true) {
						if(getLexeme(lookahead).equals("[")) {
							match(lookahead,"[",Tag.DEL);
							if(lookahead.tag == Tag.NRO) {
								match(lookahead, null, Tag.NRO);
								if(getLexeme(lookahead).equals("]")) {
									match(lookahead,"]",Tag.DEL);
								}else {
									//erro
								}
							}
							if(lookahead.tag == Tag.IDE){
								match(lookahead, null, Tag.IDE);
								if(getLexeme(lookahead).equals("]")) {
									match(lookahead,"]",Tag.DEL);
								}else {
									//erro
								}
							}
						}
						else break;
					}
					if(getLexeme(lookahead).equals("=")) {
						match(lookahead, "=", Tag.REL);
						if(getLexeme(lookahead).equals("{")) {
							match(lookahead,"{",Tag.DEL);
							while(true) {
								if(lookahead.tag == Tag.IDE) {
									match(lookahead, null, Tag.IDE);
									if(getLexeme(lookahead).equals(",")) {
										match(lookahead,",",Tag.DEL);
									}
								}
								else if(lookahead.tag == Tag.CAD) {
									match(lookahead, null, Tag.CAD);
									if(getLexeme(lookahead).equals(",")) {
										match(lookahead,",",Tag.DEL);
									}
								}
								else if(lookahead.tag == Tag.NRO) {
									match(lookahead, null, Tag.NRO);
									if(getLexeme(lookahead).equals(",")) {
										match(lookahead,",",Tag.DEL);
									}
								}
								else break;
							}
							if(getLexeme(lookahead).equals("}")) {
								match(lookahead,"}",Tag.DEL);
								if(getLexeme(lookahead).equals(";")) {
									match(lookahead,";",Tag.DEL);
									return;
								}
							}
						}
					}
					if(getLexeme(lookahead).equals(",")) {
						match(lookahead,",",Tag.DEL);
					}
					if(getLexeme(lookahead).equals(";")) {
						match(lookahead,";",Tag.DEL);
						return;
					}
				}
				if(getLexeme(lookahead).equals("=")) {
					match(lookahead, "=", Tag.REL);
					if(lookahead.tag == Tag.NRO) {
						match(lookahead,null,Tag.NRO);
						if(getLexeme(lookahead).equals(",")) {
							match(lookahead,",",Tag.DEL);
						}
						if(getLexeme(lookahead).equals(";")) {
							match(lookahead,";",Tag.DEL);
							return;
						}
					}
					if(lookahead.tag == Tag.PRE) {
						if(getLexeme(lookahead).equals("true")) match(lookahead,"true",Tag.PRE);
						if(getLexeme(lookahead).equals("false")) match(lookahead,"false",Tag.PRE);
						if(getLexeme(lookahead).equals(",")) {
							match(lookahead,",",Tag.DEL);
						}
						if(getLexeme(lookahead).equals(";")) {
							match(lookahead,";",Tag.DEL);
							return;
						}
					}
					if(lookahead.tag == Tag.CAD) {
						match(lookahead,null,Tag.CAD);
						if(getLexeme(lookahead).equals(",")) {
							match(lookahead,",",Tag.DEL);
						}
						if(getLexeme(lookahead).equals(";")) {
							match(lookahead,";",Tag.DEL);
							return;
						}
					}
					
				}
				if(getLexeme(lookahead).equals(",")) {
					match(lookahead,",",Tag.DEL);
				}
				if(getLexeme(lookahead).equals(";")) {
					match(lookahead,";",Tag.DEL);
					return;
				}
			}
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
					if(lookahead.tag == Tag.IDE) {
						match(lookahead,null,Tag.IDE);
						if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
							match(lookahead,"(",Tag.DEL);
							paramsList();
							if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
								match(lookahead,")",Tag.DEL);
								if(getLexeme(lookahead).equals("{") && lookahead.tag == Tag.DEL) {
									match(lookahead,"{",Tag.DEL);
									if(getLexeme(lookahead).equals("}") && lookahead.tag == Tag.DEL) {
										match(lookahead,"}",Tag.DEL);
										System.out.println("Function");
									}
								}
							}
						}
					}
				}
			}
			else if(getLexeme(lookahead).equals("procedure") && lookahead.tag == Tag.PRE) {
				match(lookahead,"procedure",Tag.PRE);
				if(lookahead.tag == Tag.IDE) {
					match(lookahead,null,Tag.IDE);
					if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
						match(lookahead,"(",Tag.DEL);
						paramsList();
						if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
							match(lookahead,")",Tag.DEL);
							if(getLexeme(lookahead).equals("{") && lookahead.tag == Tag.DEL) {
								match(lookahead,"{",Tag.DEL);
								if(getLexeme(lookahead).equals("}") && lookahead.tag == Tag.DEL) {
									match(lookahead,"}",Tag.DEL);
									System.out.println("Procedure");
								}
							}
						}
					}
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
				if(lookahead.tag == Tag.IDE) {
					match(lookahead,null,Tag.IDE);
					if(getLexeme(lookahead).equals(",") && lookahead.tag == Tag.DEL) {
						match(lookahead,",",Tag.DEL);
					}
					else if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL){
						return;
					}
				}else return;
			}
		}
	}
	
}
