package src.components.cruncher;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class CruncherMiddleware implements Runnable {

	private ExecutorService threadPool;
	private BlockingQueue<Map<String, String>> inputBlockingQueue;
	private BlockingQueue<Map<String, Multiset<Object>>> outputBlockingQueue;
	private CruncherView cruncherView;
	private int arity;

	public CruncherMiddleware(ExecutorService threadPool,
							  CruncherView cruncherView,
							  int arity,
							  BlockingQueue<Map<String, String>> inputBlockingQueue,
							  BlockingQueue<Map<String, Multiset<Object>>> outputBlockingQueue) {
		this.threadPool = threadPool;
		this.cruncherView = cruncherView;
		this.arity = arity;
		this.inputBlockingQueue = inputBlockingQueue;
		this.outputBlockingQueue = outputBlockingQueue;

//		System.out.println("CruncherMiddleware init\n");
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
					Map<String, Multiset<Object>> reserve = new ConcurrentHashMap<>();
					Multiset<Object> multiset = HashMultiset.create();
					String fileName = "*" + filePath + "-arity" + arity;
					reserve.put(fileName, multiset);

					System.out.println("[CruncherMiddleware]: Crunching " + filePath + "...");

					// TODO: Where the magic happens!?
//					outputBlockingQueue.put(reserve);
//					System.out.println("[CruncherMiddleware]: Sent file " + fileName + " to the output queue");

					// TODO: Fork Join Pool - send result future to the output!
					CruncherWorker cruncherWorker = new CruncherWorker(filePath, entry.getValue(), arity, 0, entry.getValue().length() - 1);
					Future<Map<String, Multiset<Object>>> result = ((ForkJoinPool) threadPool).submit(cruncherWorker);
					Map<String, Multiset<Object>> output = result.get();
//					OldCruncherWorker cruncherWorker = new OldCruncherWorker(filePath, entry.getValue(), arity);
//					Future<Map<String, Multiset<Object>>> result = threadPool.submit(cruncherWorker);
//					Map<String, Multiset<Object>> output = result.get();

					List<Node> whatToRemove = new CopyOnWriteArrayList<>();
					ObservableList<Node> children = cruncherView.getVbInputFiles().getChildren();

					// Remove file from cruncher
					for (Node node : children) {
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

//					for (Map.Entry<String, Multiset<Object>> value : output.entrySet()) {
//						String key = value.getKey();
//						Multiset<Object> val = value.getValue();
//
//						ImmutableList<Multiset.Entry<Object>> list = Multisets.copyHighestCountFirst(val).entrySet().asList();
//						ImmutableList<Multiset.Entry<Object>> out = list.subList(0, Math.min(list.size(), 100));
//
////						System.out.println(">>> " + key);
//						System.out.println(">>> " + key + " ===> " + out);
//					}
				}
			} catch (InterruptedException | ExecutionException | OutOfMemoryError e) {
				e.printStackTrace();

				Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.initStyle(StageStyle.UTILITY);
					alert.setTitle("Cruncher Error");
					alert.setHeaderText("Error");
					alert.setContentText("Out of memory error");
					alert.showAndWait();

					System.exit(0);
				});
			} catch (RejectedExecutionException ex) {

			}
		}
	}
}
