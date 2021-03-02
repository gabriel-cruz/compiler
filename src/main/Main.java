package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import manager.Files;

public class Main {

	public static void main(String[] args) {
		//Pega os arquivos no diretório raíz
		File[] files =  Files.getFiles("C:\\Users\\lsjsa\\Downloads\\Testes");
		for(File file : files) {
			System.out.println(file.getName()); //printa o nome do arquivo
			try (BufferedReader br = new BufferedReader(new FileReader(file))){ //try-catch com iniciação
				String line = br.readLine(); //ler a linha do arquivo
				while(line != null) {
					System.out.println(line); // printa a linha
					line = br.readLine();					
				}
				br.close(); //fecha o arquivo
				System.out.println("----------------------------------------------------------------------");
			}catch(IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

}
