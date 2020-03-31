package src.components;


import com.google.common.io.Files;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FileInput {

	private String rootDirectory;
	private boolean isBlocked;
	private BlockingQueue<File> blockingQueue;
	private List<FileRef> files;
	private int FILE_INPUT_SLEEP_TIME = 5000;

	public static void main(String[] args) {
		FileInput fileInput = new FileInput("data/dzimiks/");

		try {
			fileInput.traverseDirectory();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public FileInput(String rootDirectory) {
		this.rootDirectory = rootDirectory;
		this.isBlocked = false;
		this.blockingQueue = new LinkedBlockingQueue<>();
		this.files = new CopyOnWriteArrayList<>();
		System.out.println("FileInput init");
	}

	public void traverseDirectory() throws InterruptedException {
		File rootDir = new File(rootDirectory);

		while (!isBlocked) {
			AtomicInteger fileNumber = new AtomicInteger(0);

			for (File file : Files.fileTraverser().depthFirstPreOrder(rootDir)) {
				if (file.getName().endsWith(".txt")) {
					if (!contains(file)) {
						String lastModified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified()));
						System.out.println("FileInput -> Found file: " + file.getPath() + " | Last modified: " + lastModified);
//						blockingQueue.put(file);
						files.add(new FileRef(file.getAbsolutePath(), file.lastModified()));
						fileNumber.incrementAndGet();
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

	public String getRootDirectory() {
		return rootDirectory;
	}

	public void setRootDirectory(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	public boolean isBlocked() {
		return isBlocked;
	}

	public void setBlocked(boolean blocked) {
		isBlocked = blocked;
	}

	public BlockingQueue<File> getBlockingQueue() {
		return blockingQueue;
	}

	public void setBlockingQueue(BlockingQueue<File> blockingQueue) {
		this.blockingQueue = blockingQueue;
	}
}
