package src.components.output;

import com.google.common.collect.Multiset;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.io.File;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class OutputMiddleware implements Runnable {

	private ExecutorService threadPool;
	private BlockingQueue<Map<String, Multiset<Object>>> outputBlockingQueue;
	private ListView<String> outputList;
	private Map<String, Multiset<Object>> outputData;
	private volatile boolean isWorking;

	public OutputMiddleware(ExecutorService threadPool,
							BlockingQueue<Map<String, Multiset<Object>>> outputBlockingQueue,
							ListView<String> outputList) {
		this.threadPool = threadPool;
		this.outputBlockingQueue = outputBlockingQueue;
		this.outputList = outputList;
		this.outputData = new ConcurrentHashMap<>();
		this.isWorking = true;

//		System.out.println("OutputMiddleware init\n");
	}

	@Override
	public void run() {
		while (isWorking) {
			try {
				Map<String, Multiset<Object>> output = outputBlockingQueue.take();

				for (Map.Entry<String, Multiset<Object>> entry : output.entrySet()) {
					String fileAbsolutePath = entry.getKey();
					String[] parts = fileAbsolutePath.split(String.valueOf(File.separatorChar));
					String filePath = parts[parts.length - 1];

					System.out.println("[Output]: " + filePath);

					// TODO: Put data
					this.outputData.put(entry.getKey(), entry.getValue());

					Platform.runLater(() -> {
						// Add to output view list
						if (filePath.charAt(0) == '*' && !outputList.getItems().contains(filePath.substring(1))) {
							outputList.getItems().add(filePath);
							System.out.println(filePath);
						} else {
							ObservableList<String> items = outputList.getItems();
							int itemsLength = items.size();

							for (int i = 0; i < itemsLength; i++) {
								String item = items.get(i);

								if (item.contains(filePath)) {
									outputList.getItems().remove(item);
									outputList.getItems().add(i, filePath);
								}
							}
						}
					});
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Output Middleware is closed!");
	}

	public Map<String, Multiset<Object>> getOutputData() {
		return outputData;
	}

	public void stop() {
		this.isWorking = false;
	}
}
