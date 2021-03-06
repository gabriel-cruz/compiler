package main;

import java.io.File;
import java.util.List;

import lexicAnalyser.LexicAnalyser;
import manager.Files;

public class Main {

	public static void main(String[] args) {
		//Pega os arquivos no diretório raíz
		File[] files =  Files.getFiles("D:\\gabri\\Documents\\Projetos\\Java\\compiler\\entradas");
		//List<String> infoSymbol = Files.getContentFile(new File("D:\\gabri\\Documents\\Projetos\\Java\\compiler\\util"));
		LexicAnalyser analyser = new LexicAnalyser(/*infoSymbol*/);
		for(File file : files) {
			List<String> contentFile = Files.getContentFile(file);
			analyser.analyseCode(contentFile);
		}
	}

}
