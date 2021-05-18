package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lexicAnalyser.LexicAnalyser;
import lexicAnalyser.Token;
import manager.Files;
import syntacticAnalyser.SyntacticAnalyser;

public class Main { 

	public static void main(String[] args) {
		List<Token> tokens = new ArrayList<Token>();
		//Pega os arquivos no diretório raíz
		File[] files =  Files.getFiles("./entradas");
		LexicAnalyser analyser = new LexicAnalyser();
		for(File file : files) {
			int numberFile = Files.numberFile(file.getName());
			List<String> contentFile = Files.getContentFile(file);
			tokens = analyser.analyseCode(contentFile);
			if(analyser.getQntErrors() == 0) {
				System.out.println("O arquivo " + file.getName() + " não possui erros léxicos");
				SyntacticAnalyser syntatic = new SyntacticAnalyser(tokens);
				syntatic.analyseCode();
				if(syntatic.getErros().isEmpty()) System.out.println("Sem erros sintáticos");
				Files.setContentFile(tokens, syntatic.getErros(), "./saidas/saida" + numberFile+ ".txt");
			}else {
				Files.setContentFile(tokens,null, "./saidas/saida" + numberFile+ ".txt");
				analyser.resetQntErrors();
			}
		}
	}

}
