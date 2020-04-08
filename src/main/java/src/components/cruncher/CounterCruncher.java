package src.components.cruncher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;

import java.util.Map;
import java.util.concurrent.*;

public class CounterCruncher implements Runnable {

	private BlockingQueue<Map<String, String>> inputBlockingQueue;
	private BlockingQueue<Map<String, Multiset<Object>>> outputBlockingQueue;
	private ExecutorService threadPool;
	private CruncherMiddleware cruncherMiddleware;
	private int arity;

	public CounterCruncher(ExecutorService threadPool,
						   CruncherView cruncherView,
						   BlockingQueue<Map<String, Multiset<Object>>> outputBlockingQueue,
						   int arity) {
		this.threadPool = threadPool;
		this.arity = arity;
		this.inputBlockingQueue = new LinkedBlockingQueue<>();
		this.outputBlockingQueue = outputBlockingQueue;
		this.cruncherMiddleware = new CruncherMiddleware(threadPool, cruncherView, arity, inputBlockingQueue, outputBlockingQueue);
	}

	@Override
	public void run() {
		Thread thread = new Thread(cruncherMiddleware);
		thread.start();
	}

	public int getArity() {
		return arity;
	}

	public BlockingQueue<Map<String, String>> getInputBlockingQueue() {
		return inputBlockingQueue;
	}

	public BlockingQueue<Map<String, Multiset<Object>>> getOutputBlockingQueue() {
		return outputBlockingQueue;
	}
}
