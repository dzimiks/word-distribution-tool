package src.components.input;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class FileInputWorker implements Callable<Map<String, String>> {

	private File inputFile;

	public FileInputWorker(File inputFile) {
		this.inputFile = inputFile;
		System.out.println("FileInputWorker init\n");
	}

	@Override
	public Map<String, String> call() throws Exception {
		Map<String, String> map = new ConcurrentHashMap<>();
		String fileContent = Files.asCharSource(inputFile, Charsets.UTF_8).read();
		map.put(inputFile.getAbsolutePath(), fileContent);
		return map;
	}
}
