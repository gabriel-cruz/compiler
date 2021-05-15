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
		//Pega os arquivos no diret�rio ra�z
		File[] files =  Files.getFiles("./entradas");
		LexicAnalyser analyser = new LexicAnalyser();
		for(File file : files) {
			List<String> contentFile = Files.getContentFile(file);
			tokens = analyser.analyseCode(contentFile);
			SyntacticAnalyser syntatic = new SyntacticAnalyser(tokens);
			syntatic.analyseCode();
			System.out.println("z:" + syntatic.erros.size());
			if(analyser.getQntErrors() == 0) System.out.println("O arquivo " + file.getName() + " n�o possui erros l�xicos");
			int numberFile = Files.numberFile(file.getName());
			Files.setContentFile(tokens, "./saidas/saida" + numberFile+ ".txt");
			analyser.resetQntErrors();
		}
	}

}
