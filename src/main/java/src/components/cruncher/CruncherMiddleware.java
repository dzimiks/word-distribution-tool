package src.components.cruncher;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class CruncherMiddleware implements Runnable {

	private ExecutorService threadPool;
	private BlockingQueue<Map<String, String>> inputBlockingQueue;
	private BlockingQueue<Map<String, ImmutableList<Multiset.Entry<Object>>>> outputBlockingQueue;
	private CruncherView cruncherView;
	private int arity;

	public CruncherMiddleware(ExecutorService threadPool,
							  CruncherView cruncherView,
							  int arity,
							  BlockingQueue<Map<String, String>> inputBlockingQueue,
							  BlockingQueue<Map<String, ImmutableList<Multiset.Entry<Object>>>> outputBlockingQueue) {
		this.threadPool = threadPool;
		this.cruncherView = cruncherView;
		this.arity = arity;
		this.inputBlockingQueue = inputBlockingQueue;
		this.outputBlockingQueue = outputBlockingQueue;

		System.out.println("CruncherMiddleware init\n");
	}

	@Override
	public void run() {
		while (true) {
			try {
				Map<String, String> input = inputBlockingQueue.take();

				for (Map.Entry<String, String> entry : input.entrySet()) {
					String fileAbsolutePath = entry.getKey();
					String[] parts = fileAbsolutePath.split(String.valueOf(File.separatorChar));
					String filePath = parts[parts.length - 1];

					// TODO: Sent to output queue here???
					Map<String, ImmutableList<Multiset.Entry<Object>>> reserve = new ConcurrentHashMap<>();
					ImmutableList<Multiset.Entry<Object>> immutableList = ImmutableList.copyOf(new ArrayList<>());
					String fileName = "*" + filePath + "-arity" + arity;
					reserve.put(fileName, immutableList);

					System.out.println("[CruncherMiddleware]: Crunching " + filePath + "...");

					// TODO: Where the magic happens!?
//					outputBlockingQueue.put(reserve);
//					System.out.println("[CruncherMiddleware]: Sent file " + fileName + " to the output queue");

					CruncherWorker cruncherWorker = new CruncherWorker(filePath, entry.getValue(), arity);
					Future<Map<String, ImmutableList<Multiset.Entry<Object>>>> result = threadPool.submit(cruncherWorker);
					Map<String, ImmutableList<Multiset.Entry<Object>>> output = result.get();
					List<Node> whatToRemove = new CopyOnWriteArrayList<>();

					// Remove file from cruncher
					for (Node node : cruncherView.getVbInputFiles().getChildren()) {
						if (node instanceof Label) {
							Label label = (Label) node;

							if (label.getText().equals(filePath)) {
								whatToRemove.add(node);
							}
						}
					}

					if (!whatToRemove.isEmpty()) {
						for (Node node : whatToRemove) {
							Platform.runLater(() -> cruncherView.getVbInputFiles().getChildren().remove(node));
							System.out.println("[CruncherMiddleware]: Removed " + filePath + " from " + cruncherView);
						}

						whatToRemove.clear();
					}

					// TODO: Send result to output
					outputBlockingQueue.put(output);
					System.out.println("[CruncherMiddleware]: Sent crunched " + fileName + " to the output queue");

					for (Map.Entry<String, ImmutableList<Multiset.Entry<Object>>> value : output.entrySet()) {
						String key = value.getKey();
						ImmutableList<Multiset.Entry<Object>> val = value.getValue();

						System.out.println(">>> " + key);
//						System.out.println(">>> " + key + " ===> " + val);
					}
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
}
