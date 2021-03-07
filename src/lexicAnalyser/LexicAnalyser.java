package lexicAnalyser;

import java.util.ArrayList;
import java.util.List;

public class LexicAnalyser {
	
	public LexicAnalyser(){
		
	}
	
	public void analyseCode(List<String> code){
		int lineNumber = 0; //variavel que controla a linha do codigo no arquivo de entrada
		int initialPointer,finalPointer; //variavel que auxilia na analise dos lexemas
		code = removeSpacesAndComments(code); //remove os comentarios e os espa�os em brancos
		for(String line : code){
			//no inicio do bloco 
			initialPointer = 0;
			finalPointer = 0; 
			for(int i = 0; i < line.length(); i++) {
				//caracter achado no index com o n�mero igual ao da variavel finalPointer
				char peek = line.charAt(finalPointer); 
				if(Character.isLetter(peek)||Character.isDigit(peek)||peek == '_'){
					//se o caracter for uma letra ou numero avan�a o finalPointer
					if(i == line.length() - 1) {
						//o metodo substring no segundo argumento � o index - 1
						String lexeme = line.substring(initialPointer, finalPointer+1);
						System.out.println("lexeme: " + lexeme);
					}
				}
				else {
					//n�o achando letra ou n�mero, realiza o algoritmo da analise do lexema
					if(initialPointer !=  finalPointer) {
						//o metodo substring no segundo argumento � o index - 1
						String lexeme = line.substring(initialPointer, finalPointer);
						System.out.println("lexeme: " + lexeme);
					}
					//n�o achando letra ou n�mero, realiza o algoritmo da analise do lexema
					//realizar a compara��o entre os regex
					if(Character.toString(peek).matches("[+/*-]")){ //detecta simbolos de opera��es aritmetricas
						System.out.println("notNumberOrLetter: " + peek);
					}//
					else if(Character.toString(peek).matches("[=><]")) {//detecta simbolos de opera��es relacionais
						System.out.println("notNumberOrLetter: " + peek);
					}
					else if(Character.toString(peek).matches("[&!|]")){ //detecta simbolos de opera��es logicos
						System.out.println("notNumberOrLetter: " + peek);
					}else if(Character.toString(peek).matches("[\"\"]")) {
						System.out.println("string");
					}
					//o initialPointer avan�a para o pr�ximo caracter ap�s a detec��o de um simbolo
					
					initialPointer = finalPointer + 1;
				}
				finalPointer+=1;
			}
			lineNumber++;
		}
	}
	
	private List<String> removeSpacesAndComments(List<String> code){
		List<String> result = new ArrayList<String>();
		boolean isLineComment = false;
		boolean isBlockComment = false;
		//boolean isBlockFinal = false;
		
		for(String line : code){
			String[] notWithSpace = line.split("[ \t]");
			for(String check : notWithSpace){
				if(check.matches("^[/]{2}[\\s\\S]*"))isLineComment = true;			
				else if(check.matches("^[/][*][\\s\\S]*")) isBlockComment = true;
				else if(check.matches("^[\\s\\S]+[*][/]")) isBlockComment = false;
				if(!isLineComment && !isBlockComment)	result.add(check);
			}
		}
		return result;
	}
	
}
