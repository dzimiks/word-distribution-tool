package src.components.input;


import com.google.common.io.Files;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FileInput implements Runnable {

	private static int FILE_INPUT_SLEEP_TIME = 5000;

	private List<String> directories;
	private BlockingQueue<File> blockingQueue;
	private List<BlockingQueue<Map<String, String>>> crunchersBlockingQueues;
	private List<FileRef> files;
	private ExecutorService threadPool;

	public FileInput(ExecutorService threadPool) {
		this.threadPool = threadPool;
		this.crunchersBlockingQueues = new CopyOnWriteArrayList<>();
		this.directories = new CopyOnWriteArrayList<>();
		this.blockingQueue = new LinkedBlockingQueue<>();
		this.files = new CopyOnWriteArrayList<>();
		System.out.println("FileInput init");
	}

	@Override
	public void run() {
		try {
			traverseDirectories();
			threadPool.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void traverseDirectories() throws InterruptedException {
		for (String directory : directories) {
			File rootDir = new File(directory);
			AtomicInteger fileNumber = new AtomicInteger(0);
			this.blockingQueue.add(rootDir);

			//		while (true) {
			for (File file : Files.fileTraverser().depthFirstPreOrder(rootDir)) {
				if (file.getName().endsWith(".txt")) {
					if (!contains(file)) {
						String lastModified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified()));
						System.out.println("FileInput -> Found file: " + file.getPath() + " | Last modified: " + lastModified);
						files.add(new FileRef(file.getAbsolutePath(), file.lastModified()));
						fileNumber.incrementAndGet();

						// TODO: Add file to worker
						FileInputWorker fileInputWorker = new FileInputWorker(file);
						Future<Map<String, String>> result =  threadPool.submit(fileInputWorker);

						try {
							for (Map.Entry<String, String> res : result.get().entrySet()) {
								System.out.println("Future file path: " + res.getKey());
								System.out.println("Future file length: " + res.getValue().length());
								System.out.println();
							}
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
					} else {
						for (int i = 0; i < files.size(); i++) {
							FileRef currFile = files.get(i);
							System.out.println("Seen: " + currFile.getPath());

							if (currFile.getPath().equals(file.getAbsolutePath())) {
								long currLastModified = currFile.getLastModified();
								long fileLastModified = file.lastModified();

								System.out.println("currLastModified: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(currLastModified)));
								System.out.println("fileLastModified: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(fileLastModified)));

								if (currLastModified != fileLastModified) {
									files.set(i, new FileRef(file.getAbsolutePath(), file.lastModified()));
									System.out.println(">>> File " + file.getName() + " is replaced!");
								}
							}
						}
					}
				}
			}

			System.out.println("Found " + fileNumber.get() + " files so far...");
//		System.out.println("FileInput -> Waiting " + (FILE_INPUT_SLEEP_TIME / 1000) + " seconds...");
//		Thread.sleep(FILE_INPUT_SLEEP_TIME);
//		}
		}
	}

	public boolean contains(File file) {
		for (FileRef f : files) {
			if (f.getPath().equals(file.getAbsolutePath())) {
				return true;
			}
		}

		return false;
	}

	public List<String> getDirectories() {
		return directories;
	}
}
