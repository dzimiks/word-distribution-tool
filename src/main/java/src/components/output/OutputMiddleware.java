package src.components.output;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListView;
import src.main.Main;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OutputMiddleware implements Runnable {

	private ExecutorService threadPool;
	private BlockingQueue<Map<String, ImmutableList<Multiset.Entry<Object>>>> outputBlockingQueue;
	private ListView<String> outputList;
	private Map<String, ImmutableList<Multiset.Entry<Object>>> outputData;

	public OutputMiddleware(ExecutorService threadPool,
							BlockingQueue<Map<String, ImmutableList<Multiset.Entry<Object>>>> outputBlockingQueue,
							ListView<String> outputList) {
		this.threadPool = threadPool;
		this.outputBlockingQueue = outputBlockingQueue;
		this.outputList = outputList;
		this.outputData = new ConcurrentHashMap<>();

		System.out.println("OutputMiddleware init\n");
	}

	@Override
	public void run() {
		while (true) {
			try {
				Map<String, ImmutableList<Multiset.Entry<Object>>> output = outputBlockingQueue.take();

				for (Map.Entry<String, ImmutableList<Multiset.Entry<Object>>> entry : output.entrySet()) {
					String fileAbsolutePath = entry.getKey();
					String[] parts = fileAbsolutePath.split(String.valueOf(File.separatorChar));
					String filePath = parts[parts.length - 1];

					System.out.println("[Output]: " + filePath);
//					System.out.println("Output value: " + entry.getValue());

					// TODO: Thread pool?
//					Future<?> result = threadPool.submit(this);
//					System.out.println(result.get());

//					ImmutableList<Multiset.Entry<Object>> result = entry.getValue();
//					AtomicInteger counter = new AtomicInteger(0);
//					List<XYChart.Data<Number, Number>> data = new CopyOnWriteArrayList<>();

					// TODO: Put data
					this.outputData.putIfAbsent(entry.getKey(), entry.getValue());

					Platform.runLater(() -> outputList.getItems().add(filePath));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public Map<String, ImmutableList<Multiset.Entry<Object>>> getOutputData() {
		return outputData;
	}
}
