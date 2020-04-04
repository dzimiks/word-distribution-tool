package src.components.input;

import src.components.cruncher.CounterCruncher;
import src.components.cruncher.CruncherView;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FileInputMiddleware implements Runnable {

	private ExecutorService threadPool;
	private List<CruncherView> cruncherList;
	private BlockingQueue<String> filePathQueue;

	public FileInputMiddleware(ExecutorService threadPool, List<CruncherView> cruncherList, BlockingQueue<String> filePathQueue) {
		this.threadPool = threadPool;
		this.filePathQueue = filePathQueue;
		this.cruncherList = cruncherList;
		System.out.println("FileInputMiddleware init");
	}

	@Override
	public void run() {
		// TODO: Add file to worker
		while (true) {
			try {
				String fileName = filePathQueue.take();
				File file = new File(fileName);
				FileInputWorker fileInputWorker = new FileInputWorker(file);
				Future<Map<String, String>> result = threadPool.submit(fileInputWorker);
				Map<String, String> map = result.get();

				for (Map.Entry<String, String> entry : map.entrySet()) {
					System.out.println("Future file path: " + entry.getKey());
					System.out.println("Future file length: " + entry.getValue().length());
					System.out.println();
				}

				cruncherList.forEach(cruncher -> {
					cruncher.getCruncher().getInputBlockingQueue().add(map);
					System.out.println("[Cruncher] " + cruncher + " size: " + cruncher.getCruncher().getInputBlockingQueue().size());
				});
			} catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
