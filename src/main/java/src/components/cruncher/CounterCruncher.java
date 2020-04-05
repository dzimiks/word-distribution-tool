package src.components.cruncher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;

import java.util.Map;
import java.util.concurrent.*;

public class CounterCruncher implements Runnable {

	private BlockingQueue<Map<String, String>> inputBlockingQueue;
	private ExecutorService threadPool;
	private CruncherMiddleware cruncherMiddleware;
	private int arity;

	public CounterCruncher(ExecutorService threadPool, int arity) {
		this.threadPool = threadPool;
		this.arity = arity;
		this.inputBlockingQueue = new LinkedBlockingQueue<>();
		this.cruncherMiddleware = new CruncherMiddleware(threadPool, arity, inputBlockingQueue);
	}

	@Override
	public void run() {
		Thread thread = new Thread(cruncherMiddleware);
		thread.start();
	}

	public BlockingQueue<Map<String, String>> getInputBlockingQueue() {
		return inputBlockingQueue;
	}
}
