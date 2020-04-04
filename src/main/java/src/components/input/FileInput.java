package src.components.input;


import com.google.common.io.Files;
import src.components.cruncher.CounterCruncher;
import src.components.cruncher.CruncherView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FileInput implements Runnable {

	private static int FILE_INPUT_SLEEP_TIME = 5000;

	private List<String> directories;
	private BlockingQueue<String> filePathQueue;
	private List<CruncherView> cruncherList;
	private List<FileRef> files;
	private ExecutorService threadPool;
	private FileInputMiddleware fileInputMiddleware;

	public FileInput(ExecutorService threadPool) {
		this.threadPool = threadPool;
		this.cruncherList = new CopyOnWriteArrayList<>();
		this.directories = new CopyOnWriteArrayList<>();
		this.filePathQueue = new LinkedBlockingQueue<>();
		this.files = new CopyOnWriteArrayList<>();
		this.fileInputMiddleware = new FileInputMiddleware(threadPool, cruncherList, filePathQueue);
		System.out.println("FileInput init");
	}

	@Override
	public void run() {
		try {
			Thread thread = new Thread(fileInputMiddleware);
			thread.start();

			traverseDirectories();

			// TODO: When to shutdown?
//			threadPool.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void traverseDirectories() throws InterruptedException {
		while (true) {
			for (String directory : directories) {
				File rootDir = new File(directory);
				AtomicInteger fileNumber = new AtomicInteger(0);

				for (File file : Files.fileTraverser().depthFirstPreOrder(rootDir)) {
					if (file.getName().endsWith(".txt")) {
						if (!contains(file)) {
							String lastModified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified()));
							System.out.println("FileInput -> Found file: " + file.getPath() + " | Last modified: " + lastModified);

							this.files.add(new FileRef(file.getAbsolutePath(), file.lastModified()));
							this.filePathQueue.add(file.getAbsolutePath());
							fileNumber.incrementAndGet();
						} else {
							// TODO: Last modified is not working
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
			}

			System.out.println("FileInput -> Waiting " + (FILE_INPUT_SLEEP_TIME / 1000) + " seconds...");
			Thread.sleep(FILE_INPUT_SLEEP_TIME);
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

	public List<CruncherView> getCruncherList() {
		return cruncherList;
	}
}
