package src.components.cruncher;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CounterCruncher implements Runnable {

	private BlockingQueue<Map<String, String>> inputBlockingQueue;

	public CounterCruncher() {
		this.inputBlockingQueue = new LinkedBlockingQueue<>();
	}

	@Override
	public void run() {

	}

	public BlockingQueue<Map<String, String>> getInputBlockingQueue() {
		return inputBlockingQueue;
	}
}
