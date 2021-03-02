package manager;

import java.io.File;

public class Files {

	/***
	 * 
	 * @param path: O caminho do diret�rio ra�z
	 * @return A lista de arquivos que seguem a determina��o do regex nos seus nomes
	 */
	static public File[] getFiles(String path) {
		File parentPath = new File(path);
		File[] files = parentPath.listFiles(file -> file.getName().matches("entrada\\d.txt$"));
		return files;
	}
	
}
