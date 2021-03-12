package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lexicAnalyser.LexicAnalyser;
import lexicAnalyser.Token;
import manager.Files;

public class Main { 

	public static void main(String[] args) {
		List<Token> tokens = new ArrayList<Token>();
		//Pega os arquivos no diretório raíz
		File[] files =  Files.getFiles("C:\\Users\\lsjsa\\OneDrive\\Documentos\\Java\\compilerII\\entradas");
		//List<String> infoSymbol = Files.getContentFile(new File("D:\\gabri\\Documents\\Projetos\\Java\\compiler\\util"));
		LexicAnalyser analyser = new LexicAnalyser(/*infoSymbol*/);
		for(File file : files) {
			List<String> contentFile = Files.getContentFile(file);
			tokens = analyser.analyseCode(contentFile);
			int numberFile = Files.numberFile(file.getName());
			Files.setContentFile(tokens, "C:\\Users\\lsjsa\\OneDrive\\Documentos\\Java\\compilerII\\saidas\\saida" + numberFile+ ".txt");
		}
	}

}
