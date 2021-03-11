package lexicAnalyser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LexicAnalyser {
	public ReservedWord keyWords;
	
	public LexicAnalyser(){
		keyWords = new ReservedWord();
	}
	
	
	public List<Token> analyseCode(List<String> code){
		
		int lineNumber = 0; //variavel que controla a linha do codigo no arquivo de entrada
		int initialPointer,finalPointer; //variavel que auxilia na analise dos lexemas
		boolean isLineComment;
		boolean isBlockComment = false;
		List<Token> tokens = new ArrayList<Token>();
		
		for(String line : code){
			//no inicio do bloco 
			initialPointer = 0;
			finalPointer = 0;
			isLineComment = false;			
			while(finalPointer < line.length()) {
				//desconsidera o resto do loop, devido ao fato do restante do c�digo ser coment�rio em linha
				if(isLineComment) break;
				//se o bloco de coment�rio foi iniciado entra nessa condi��o
				if(isBlockComment) {
					//pega o pr�ximo char
					char check = line.charAt(finalPointer);
					//verifica se � o char *
					if(check == '*'){
						//verifica se � a representa��o do fechamento do coment�rio em bloco
						if(blockComment(finalPointer, line)) {
							isBlockComment = false;
							//vai para o index fora do coment�rio em bloco
							finalPointer += 2;
							//se chegar no final da linha para o la�o de repeti��o
							if(finalPointer == line.length()) break;
							else initialPointer = finalPointer;
						}
					}else finalPointer++; //incrementa o index
					continue;
				}
				//caracter achado no index com o n�mero igual ao da variavel finalPointer
				char peek = line.charAt(finalPointer);
				//realiza uma determinada a��o se detectar uma letra como inicio no bloco
				if(peek == ' ' || peek == '\t') { //trata o espa�o
					finalPointer+=1;
					initialPointer = finalPointer;
				}
				else if(Character.isLetter(peek)){ //automato para identificadores e palavras-chaves
					//chama o metodo que executa o automato e retorna o index que determinou a parada do automato
					finalPointer = automateIdentiferOrKeyWord(finalPointer, line);
					String lexeme = line.substring(initialPointer, finalPointer); //lexeme � a variavel a ser analisada
					System.out.println("lexeme: " + lexeme);
					initialPointer = finalPointer; //atualiza o index de come�o
					
					if(keyWords.words.contains(lexeme)) {
						tokens.add(new Word(lexeme, Tag.PRE, lineNumber));
					}
					else {
						tokens.add(new Word(lexeme, Tag.IDE, lineNumber));
					}
				}
				else if(Character.isDigit(peek)){//automato para n�meros
					//chama o metodo que executa o automato e retorna o index que determinou a parada do automato
					finalPointer = automateNumber(finalPointer, line);
					String lexeme = line.substring(initialPointer, finalPointer); //lexeme � a variavel a ser analisada
					System.out.println("lexeme: " + lexeme);
					initialPointer = finalPointer;//atualiza o index de come�o
					
					tokens.add(new Number(Integer.parseInt(lexeme), lineNumber));
				}
				else {
					//n�o achando identificador ou palavra-chave
					//se detectou um simbolo aritmetrico executa o automato responsavel por essa parte
					if(Character.toString(peek).matches("[+/*-]")){ //detecta simbolos de opera��es aritmetricas
						//chama o metodo que executa o automato e retorna o index que determinou a parada do automato
						finalPointer = automateAritOperator(finalPointer, line);
						String lexeme = line.substring(initialPointer, finalPointer); //lexeme � a variavel a ser analisada
						if(lexeme.equals("/")){
							//verifica se � o coment�rio em linha, recebe da fun��o true ou false
							isLineComment = lineComment(finalPointer-1, line);
							if(isLineComment) continue; //executa o pr�ximo loop
							//verifica se � um coment�rio em bloco, recebe da fun��o true ou false
							isBlockComment = blockComment(finalPointer-1, line);
							if(isBlockComment) {
								finalPointer += 1;//incrementa o index
								continue;//executa o pr�ximo loop
							}
							//pega a / como operador aritm�trico
							System.out.println("lexeme: " + lexeme);
							tokens.add(new Word(lexeme, Tag.ART, lineNumber));
						}else{
							//pega o lexema do operador aritm�trico
							System.out.println("lexeme: " + lexeme);
							tokens.add(new Word(lexeme, Tag.ART, lineNumber));
						}
						initialPointer = finalPointer;//atualiza o index de come�o
					}
					else if(Character.toString(peek).matches("[;,{}.()[\\\\]]")){ //detecta simbolos de operadores delimitadores
						System.out.println("lexeme: " + peek); //detecta��o dos simbolos de delimitadores
						finalPointer+=1;
						initialPointer = finalPointer;//atualiza o index de come�o
						tokens.add(new Word(Character.toString(peek), Tag.DEL, lineNumber));
					}
					else if(Character.toString(peek).matches("[=><]")) {//detecta simbolos de opera��es relacionais
						//chama o metodo que executa o automato e retorna o index que determinou a parada do automato
						finalPointer = automateRelatOperator(finalPointer, line);
						String lexeme = line.substring(initialPointer, finalPointer);
						System.out.println("lexeme: " + lexeme);
						tokens.add(new Word(lexeme, Tag.REL, lineNumber));
						initialPointer = finalPointer;//atualiza o index de come�o
					}
					else if(Character.toString(peek).matches("[&!|]")){ //detecta simbolos de opera��es logicos
						//chama o metodo que executa o automato e retorna o index que determinou a parada do automato
						/*int aux = automateLogicOperator(finalPointer, line);
						if(aux == -1) {
							tokens.add(new Word("Erro de operador: " + Character.toString(peek), Tag.OpMF, lineNumber));
							finalPointer++;
							continue;
						}*/
						String lexeme = line.substring(initialPointer, finalPointer);
						System.out.println("lexeme: " + lexeme);
						tokens.add(new Word(lexeme, Tag.LOG, lineNumber));
						initialPointer = finalPointer; //atualiza o index de come�o
					}else if(Character.toString(peek).matches("[\"]")){
						//chama o metodo que executa o automato e retorna o index que determinou a parada do automato
						//finalPointer+1, pois se passa o primeiro caracter ap�s as "
						finalPointer = automateString(finalPointer+1, line);
						if(finalPointer == -1) { //se detectou erro na string
							String lexeme = line.substring(initialPointer, line.length());
							System.out.println("Erro na string, n�o finalizou a cadeia com \" ");
							tokens.add(new Word(lexeme, Tag.CMF, lineNumber));
							break;
						}else {//caso a cadeia de string esteja correta
							String lexeme = line.substring(initialPointer, finalPointer);
							System.out.println("lexeme: " + lexeme);
							tokens.add(new Word(lexeme, Tag.CAD, lineNumber));
							initialPointer = finalPointer; //atualiza o index de come�o
						}
					}
					
					else {
						tokens.add(new Word("Caractere Inv�lido: " + Character.toString(peek), Tag.SIB, lineNumber));
						finalPointer++;
					}
				}
			}
			lineNumber++;
		}
		if(isBlockComment) {
			tokens.add(new Word("Erro de coment�rio", Tag.CoMF, lineNumber));
		}
		
		return tokens;
	}
	
	private int automateIdentiferOrKeyWord(int pointer, String analyse){
		char peek;
		boolean finishedArray = false;
		//la�o responsavel por detectar a cadeia de caracaters at� achar um simbolo indevido
		do {
			if(pointer >= analyse.length()) { //verifica se chegou ao fim do buffer
				finishedArray = true;
				break;
			}
			peek = analyse.charAt(pointer);
			pointer++;
		}while(Character.isLetter(peek)||Character.isDigit(peek)||peek == '_');
		if(finishedArray) return pointer;
		return pointer - 1; // devido ao p�s incremento no final do la�o do
	}
	
	private int automateNumber(int pointer, String analyse){
		char peek;
		boolean finishedArray = false;
		int foundDot = 0;
		//la�o responsavel por detectar a cadeia de caracaters at� achar um simbolo indevido ou letra
		do {
			if(pointer >= analyse.length()) {//verifica se chegou ao fim do buffer
				finishedArray = true;
				break;
			}
			peek = analyse.charAt(pointer);
			if(peek == '.') foundDot++;
			pointer++;
		}while(Character.isDigit(peek));
		if(finishedArray) return pointer;
		return pointer - 1; // devido ao p�s incremento no final do la�o do
	}
	
	private int automateString(int pointer, String analyse){
		char peek;
		boolean finishedString = false;
		//la�o responsavel por detectar a cadeia que representa uma string, condi��o de parada � achar "
		do {
			if(pointer >= analyse.length()) break; //verifica se chegou ao fim do buffer
			peek = analyse.charAt(pointer);
			//condi��o que trata a quest�o de aceitar \" na string
			if(Character.toString(peek).matches("\\\\")){
				char ahead = analyse.charAt(pointer+1);
				if(Character.toString(ahead).matches("[\"]")) {
					 pointer+=2;
					 continue;
				}else {
					 pointer++;
					 continue;
				}
			}else if(Character.toString(peek).matches("[\"]")) {
				finishedString = true;
			}
			pointer++;
		}while( !finishedString);
		if(finishedString)return pointer;
		return -1; // caso a string esteja formatada errada retorna -1
	}
	
	private int automateAritOperator(int pointer, String analyse){
		char peek = analyse.charAt(pointer);
		if(peek == '+' || peek == '-'){
			if(pointer + 1 < analyse.length()) { //verifica se chegou ao fim do buffer
				char ahead = analyse.charAt(pointer+1);
				if(ahead == peek) { //verifica questao ++ --
					return pointer+2;
				}else {
					return pointer+1;
				}
			}
		}
		return pointer+1;
	}
	
	private int automateRelatOperator(int pointer, String analyse){
		char peek = analyse.charAt(pointer);
		if(peek == '>' || peek == '=' || peek == '<' || peek == '!'){
			//verifica >= <= == !=
			if(pointer + 1 < analyse.length()) { //verifica se chegou ao fim do buffer
				char ahead = analyse.charAt(pointer+1);
				if(ahead == '=') {
					return pointer+2;
				}else {
					return pointer+1;
				}
			}
		}
		return pointer+1;
	}
	
	private int automateLogicOperator(int pointer, String analyse){
		char peek = analyse.charAt(pointer);
		//verifica && ||
		if(peek == '&' || peek == '|'){
			if(pointer + 1 < analyse.length()) {
				char ahead = analyse.charAt(pointer+1);
				
				return ahead == peek ? pointer+=2 : -1;
			}
		}
		return pointer + 1;
	}
	
	
	private boolean lineComment(int pointer, String analyse) {
		char peek = analyse.charAt(pointer);
		//verifica a ocorr�ncia de //
		if(pointer + 1 < analyse.length()) {
			char ahead = analyse.charAt(pointer+1);
			if(ahead == peek) {
				return true;
			}else {
				return false;
			}
		}
		return false;
	}
	
	
	private boolean blockComment(int pointer, String analyse) {
		char peek = analyse.charAt(pointer);
		//verifica a ocor�ncia de /*
		if(pointer + 1 < analyse.length()) {
			char ahead = analyse.charAt(pointer+1);
			if(peek == '/') {
				if(ahead == '*') return true;
				else return false;
			}
			if(peek == '*'){
				if(ahead == '/') return true;
				else return false;
			}
		}
		return false;
	}
	
}
