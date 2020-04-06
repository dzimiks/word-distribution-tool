package src.components.output;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class CacheOutput implements Runnable {

	private ExecutorService threadPool;
	private BlockingQueue<Map<String, ImmutableList<Multiset.Entry<Object>>>> outputBlockingQueue;

	public CacheOutput(ExecutorService threadPool) {
		this.threadPool = threadPool;
		this.outputBlockingQueue = new LinkedBlockingQueue<>();

		System.out.println("CacheOutput init\n");
	}

	@Override
	public void run() {

	}
}
