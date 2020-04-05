package src.components.input;


import com.google.common.io.Files;
import src.components.cruncher.CruncherView;
import src.main.Main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FileInput implements Runnable {

	private List<String> directories;
	private BlockingQueue<String> filePathQueue;
	private List<CruncherView> cruncherList;
	private ConcurrentHashMap<String, Long> seenFiles;
	private ExecutorService threadPool;
	private FileInputMiddleware fileInputMiddleware;

	public FileInput(ExecutorService threadPool) {
		this.threadPool = threadPool;
		this.cruncherList = new CopyOnWriteArrayList<>();
		this.directories = new CopyOnWriteArrayList<>();
		this.filePathQueue = new LinkedBlockingQueue<>();
		this.seenFiles = new ConcurrentHashMap<>();
		this.fileInputMiddleware = new FileInputMiddleware(threadPool, cruncherList, filePathQueue);
		System.out.println("FileInput init\n");
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
		if (directories.contains("STOP")) {
			directories.remove("STOP");
		}

		while (!directories.contains("STOP")) {
			for (String directory : directories) {
				File rootDir = new File(directory);
				AtomicInteger fileNumber = new AtomicInteger(0);

				for (File file : Files.fileTraverser().depthFirstPreOrder(rootDir)) {
					String fileName = file.getName();

					if (fileName.endsWith(".txt")) {
						String absolutePath = file.getAbsolutePath();
						long currentTime = file.lastModified();

						if (!seenFiles.containsKey(absolutePath)) {
							String lastModified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(currentTime));
							System.out.println("[FileInput] -> Found file: " + file.getPath() + " | Last modified: " + lastModified);

							this.seenFiles.put(absolutePath, currentTime);
							this.filePathQueue.add(absolutePath);
							fileNumber.incrementAndGet();
						} else {
							Long oldTime = seenFiles.get(absolutePath);

							if (!oldTime.equals(currentTime)) {
								System.out.println("Seen: " + fileName);
								System.out.println("oldTime: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(oldTime)));
								System.out.println("currentTime: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(currentTime)));

								seenFiles.put(absolutePath, currentTime);
								System.out.println(">>> File " + fileName + " is replaced!");
							}
						}
					}
				}

				System.out.println("Found " + fileNumber.get() + " files so far...");
			}

			System.out.println("FileInput -> Waiting " + (Main.FILE_INPUT_SLEEP_TIME / 1000) + " seconds...");
			Thread.sleep(Main.FILE_INPUT_SLEEP_TIME);
		}
	}

	public List<String> getDirectories() {
		return directories;
	}

	public List<CruncherView> getCruncherList() {
		return cruncherList;
	}
}
