package src.components.cruncher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;

import java.io.File;
import java.util.Map;
import java.util.concurrent.*;

public class CruncherMiddleware implements Runnable {

	private ExecutorService threadPool;
	private BlockingQueue<Map<String, String>> inputBlockingQueue;
	private BlockingQueue<Map<String, ImmutableList<Multiset.Entry<Object>>>> outputBlockingQueue;
	private int arity;

	public CruncherMiddleware(ExecutorService threadPool,
							  int arity,
							  BlockingQueue<Map<String, String>> inputBlockingQueue,
							  BlockingQueue<Map<String, ImmutableList<Multiset.Entry<Object>>>> outputBlockingQueue) {
		this.threadPool = threadPool;
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

					// TODO: Send result to output
//					Map<String, ImmutableList<Multiset.Entry<Object>>> outputMap = new ConcurrentHashMap<>();
//					outputMap.put(fileAbsolutePath, null);
//					outputBlockingQueue.put(outputMap);

					System.out.println("Crunching " + filePath + "...");

					CruncherWorker cruncherWorker = new CruncherWorker(filePath, entry.getValue(), arity);
					Future<Map<String, ImmutableList<Multiset.Entry<Object>>>> result = threadPool.submit(cruncherWorker);
					Map<String, ImmutableList<Multiset.Entry<Object>>> output = result.get();

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
