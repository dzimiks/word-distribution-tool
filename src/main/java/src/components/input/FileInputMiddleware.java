package src.components.input;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.StageStyle;
import src.components.cruncher.CruncherView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FileInputMiddleware implements Runnable {

	private ExecutorService threadPool;
	private List<CruncherView> cruncherList;
	private BlockingQueue<String> filePathQueue;
	private Label lblIdle;
	private volatile boolean isWorking;

	public FileInputMiddleware(ExecutorService threadPool,
							   List<CruncherView> cruncherList,
							   BlockingQueue<String> filePathQueue,
							   Label lblIdle) {
		this.threadPool = threadPool;
		this.filePathQueue = filePathQueue;
		this.cruncherList = cruncherList;
		this.lblIdle = lblIdle;
		this.isWorking = true;

//		System.out.println("FileInputMiddleware init\n");
	}

	@Override
	public void run() {
		while (isWorking) {
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
					// Add read files to cruncher
					cruncher.getCruncher().getInputBlockingQueue().add(map);
					System.out.println("[Cruncher]: " + cruncher + " got " + map.keySet());

					for (Map.Entry<String, String> entry : map.entrySet()) {
						String absolutePath = entry.getKey();
						String[] parts = absolutePath.split(String.valueOf(File.separatorChar));
						String filePath = parts[parts.length - 1];

						Map<String, Multiset<Object>> reserve = new ConcurrentHashMap<>();
						Multiset<Object> multiset = HashMultiset.create();
						String newFile = "*" + filePath + "-arity" + cruncher.getCruncher().getArity();
						reserve.put(newFile, multiset);

						// TODO: Sent non-crunched file to output
						cruncher.getCruncher().getOutputBlockingQueue().add(reserve);

						System.out.println(cruncher + ": " + filePath + " size: " + entry.getValue().length());
						System.out.println();

						Label inputFile = new Label(filePath);
						Platform.runLater(() -> cruncher.getVbInputFiles().getChildren().add(inputFile));
					}
				});
			} catch (ExecutionException | InterruptedException | OutOfMemoryError e) {
				e.printStackTrace();

				Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.initStyle(StageStyle.UTILITY);
					alert.setTitle("File Input Error");
					alert.setHeaderText("Error");
					alert.setContentText("Out of memory error");
					alert.showAndWait();

					System.exit(0);
				});
			} catch (RejectedExecutionException ex) {

			}
		}

		System.out.println("File Input Middleware is closed!");
	}

	public Label getLblIdle() {
		return lblIdle;
	}

	public void setLblIdle(Label lblIdle) {
		this.lblIdle = lblIdle;
	}

	public void stop() {
		this.isWorking = false;
	}
}
