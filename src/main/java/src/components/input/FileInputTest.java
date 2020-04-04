package src.components.input;

import src.components.cruncher.CounterCruncher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileInputTest {

	public static void main(String[] args) {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		CounterCruncher cruncher1 = new CounterCruncher();
		CounterCruncher cruncher2 = new CounterCruncher();
		CounterCruncher cruncher3 = new CounterCruncher();

		String rootDirectory = "data/dzimiks";
		String rootDirectory2 = "data/disk1";
		String rootDirectory3 = "data/disk2";

		FileInput fileInput = new FileInput(threadPool);

		fileInput.getDirectories().add(rootDirectory);
		fileInput.getDirectories().add(rootDirectory2);
		fileInput.getDirectories().add(rootDirectory3);

		fileInput.getCruncherList().add(cruncher1);
		fileInput.getCruncherList().add(cruncher2);
		fileInput.getCruncherList().add(cruncher3);

		Thread thread = new Thread(fileInput);
		thread.start();
	}
}
