package src.components.input;


import com.google.common.io.Files;
import javafx.application.Platform;
import javafx.scene.control.Label;
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
	private Label lblIdle;

	public FileInput(ExecutorService threadPool) {
		this.threadPool = threadPool;
		this.cruncherList = new CopyOnWriteArrayList<>();
		this.directories = new CopyOnWriteArrayList<>();
		this.filePathQueue = new LinkedBlockingQueue<>();
		this.seenFiles = new ConcurrentHashMap<>();
		this.lblIdle = new Label("Idle");
		this.fileInputMiddleware = new FileInputMiddleware(threadPool, cruncherList, filePathQueue, lblIdle);
		System.out.println("FileInput init\n");
	}

	@Override
	public void run() {
		// TODO: Poison pill pattern
		if (directories.contains("STOP")) {
			directories.remove("STOP");
		}

		try {
			Thread thread = new Thread(fileInputMiddleware);
			thread.start();
			traverseDirectories();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void traverseDirectories() throws InterruptedException {
		System.out.println("ALL DIRS: " + directories);

		while (!directories.contains("STOP")) {
			for (String directory : directories) {
				File rootDir = new File(directory);
				AtomicInteger fileNumber = new AtomicInteger(0);

				for (File file : Files.fileTraverser().depthFirstPreOrder(rootDir)) {
					String fileName = file.getName();
					String[] parts = fileName.split(String.valueOf(File.separatorChar));
					String filePath = parts[parts.length - 1];

					if (fileName.endsWith(".txt")) {
						String absolutePath = file.getAbsolutePath();
						long currentTime = file.lastModified();

						if (!seenFiles.containsKey(absolutePath)) {
							String lastModified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(currentTime));
							System.out.println("[FileInput] -> Found file: " + filePath + " | Last modified: " + lastModified);

							this.seenFiles.put(absolutePath, currentTime);

							if (!this.filePathQueue.contains(absolutePath)) {
								this.filePathQueue.add(absolutePath);
							}

							fileNumber.incrementAndGet();
							Platform.runLater(() -> lblIdle.setText("Reading: " + filePath));
						} else {
							Long oldTime = seenFiles.get(absolutePath);

							if (!oldTime.equals(currentTime)) {
								System.out.println("Seen: " + fileName);
								System.out.println("oldTime: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(oldTime)));
								System.out.println("currentTime: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(currentTime)));

								this.seenFiles.put(absolutePath, currentTime);
								this.filePathQueue.add(absolutePath);
								System.out.println(">>> File " + fileName + " is replaced!");
								Platform.runLater(() -> lblIdle.setText("Reading: " + filePath));
							} else {
								Platform.runLater(() -> lblIdle.setText("Idle"));
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

	public FileInputMiddleware getFileInputMiddleware() {
		return fileInputMiddleware;
	}

	public Label getLblIdle() {
		return lblIdle;
	}

	public void setLblIdle(Label lblIdle) {
		this.lblIdle = lblIdle;
	}

	public ConcurrentHashMap<String, Long> getSeenFiles() {
		return seenFiles;
	}
}
