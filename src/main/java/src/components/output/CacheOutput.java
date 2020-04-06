package src.components.output;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import javafx.scene.control.ListView;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class CacheOutput implements Runnable {

	private ExecutorService threadPool;
	private BlockingQueue<Map<String, ImmutableList<Multiset.Entry<Object>>>> outputBlockingQueue;
	private OutputMiddleware outputMiddleware;
	private ListView<String> outputList;

	public CacheOutput(ExecutorService threadPool, ListView<String> outputList) {
		this.threadPool = threadPool;
		this.outputList = outputList;
		this.outputBlockingQueue = new LinkedBlockingQueue<>();
		this.outputMiddleware = new OutputMiddleware(threadPool, outputBlockingQueue, outputList);

		System.out.println("CacheOutput init\n");
	}

	@Override
	public void run() {
		Thread thread = new Thread(outputMiddleware);
		thread.start();
	}

	public OutputMiddleware getOutputMiddleware() {
		return outputMiddleware;
	}

	public BlockingQueue<Map<String, ImmutableList<Multiset.Entry<Object>>>> getOutputBlockingQueue() {
		return outputBlockingQueue;
	}
}
