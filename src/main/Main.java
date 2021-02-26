package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import manager.Files;

public class Main {

	public static void main(String[] args) {
		File[] files =  Files.getFiles("C:\\Users\\lsjsa\\Downloads\\Testes");
		for(File file : files) {
			System.out.println(file.getName());
			try (BufferedReader br = new BufferedReader(new FileReader(file))){
				String line = br.readLine();
				while(line != null) {
					System.out.println(line);
					line = br.readLine();					
				}
				br.close();
				System.out.println("----------------------------------------------------------------------");
			}catch(IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

}
