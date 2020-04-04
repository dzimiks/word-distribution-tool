package src.components.input;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileInputTest {

	public static void main(String[] args) {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		String rootDirectory = "data/dzimiks";
		String rootDirectory2 = "data/disk1";
		FileInput fileInput = new FileInput(threadPool);
		fileInput.getDirectories().add(rootDirectory);
//		fileInput.getDirectories().add(rootDirectory2);

		Thread thread = new Thread(fileInput);
		thread.start();
	}
}
