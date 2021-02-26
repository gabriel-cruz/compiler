package manager;

import java.io.File;

public class Files {

	
	static public File[] getFiles(String path) {
		File parentPath = new File(path);
		File[] files = parentPath.listFiles(File::isFile);
		return files;
	}
	
	/*static public File[] inThePatern(String patern) {
		
	}*/
}
