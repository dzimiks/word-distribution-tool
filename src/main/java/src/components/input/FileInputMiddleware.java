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
	private Label lblIdle;

	public FileInputMiddleware(ExecutorService threadPool,
							   List<CruncherView> cruncherList,
							   BlockingQueue<String> filePathQueue,
							   Label lblIdle) {
		this.threadPool = threadPool;
		this.filePathQueue = filePathQueue;
		this.cruncherList = cruncherList;
		this.lblIdle = lblIdle;
		System.out.println("FileInputMiddleware init\n");
	}

	@Override
	public void run() {
		while (true) {
			try {
				String fileName = filePathQueue.take();

				System.out.println("FileInputMiddleware take: " + fileName);

				File file = new File(fileName);
				FileInputWorker fileInputWorker = new FileInputWorker(file);
				Future<Map<String, String>> result = threadPool.submit(fileInputWorker);
				Map<String, String> map = result.get();

				Platform.runLater(() -> {
					String[] parts = fileName.split(String.valueOf(File.separatorChar));
					String filePath = parts[parts.length - 1];
					lblIdle.setText("Reading: " + filePath);
				});

				cruncherList.forEach(cruncher -> {
					// TODO: Add read files to cruncher
					cruncher.getCruncher().getInputBlockingQueue().add(map);
					System.out.println("[Cruncher]: " + cruncher + " got map: " + map.keySet());

					for (Map.Entry<String, String> entry : map.entrySet()) {
						String absolutePath = entry.getKey();
						String[] parts = absolutePath.split(String.valueOf(File.separatorChar));
						String filePath = parts[parts.length - 1];

						System.out.println("[Future]: " + filePath + " => " + entry.getValue().length());
						System.out.println();

						Label inputFile = new Label(filePath);
						cruncher.getFileNamesList().add(inputFile);

						Platform.runLater(() -> cruncher.getVbInputFiles().getChildren().add(inputFile));
					}
				});
			} catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public Label getLblIdle() {
		return lblIdle;
	}

	public void setLblIdle(Label lblIdle) {
		this.lblIdle = lblIdle;
	}
}
