package src.components.input;

import javafx.application.Platform;
import javafx.scene.control.Label;
import src.components.cruncher.CruncherView;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class FileInputMiddleware implements Runnable {

	private ExecutorService threadPool;
	private List<CruncherView> cruncherList;
	private BlockingQueue<String> filePathQueue;

	public FileInputMiddleware(ExecutorService threadPool, List<CruncherView> cruncherList, BlockingQueue<String> filePathQueue) {
		this.threadPool = threadPool;
		this.filePathQueue = filePathQueue;
		this.cruncherList = cruncherList;
		System.out.println("FileInputMiddleware init");
	}

	@Override
	public void run() {
		// TODO: Add file to worker
		while (true) {
			try {
				String fileName = filePathQueue.take();
				File file = new File(fileName);
				FileInputWorker fileInputWorker = new FileInputWorker(file);
				Future<Map<String, String>> result = threadPool.submit(fileInputWorker);
				Map<String, String> map = result.get();

				cruncherList.forEach(cruncher -> {
					cruncher.getCruncher().getInputBlockingQueue().add(map);
					System.out.println("[Cruncher]: " + cruncher + " size: " + cruncher.getCruncher().getInputBlockingQueue().size());

					for (Map.Entry<String, String> entry : map.entrySet()) {
						String absolutePath = entry.getKey();
						String[] parts = absolutePath.split(String.valueOf(File.separatorChar));
						String filePath = parts[parts.length - 1];

						System.out.println("[Future]: " + filePath + " => " + entry.getValue().length());
						System.out.println();

						Label inputFile = new Label(filePath);
						cruncher.getFileNamesList().add(inputFile);

						Platform.runLater(() -> {
							cruncher.getChildren().add(inputFile);
						});
					}
				});
			} catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
