package src.components.input;

public class FileRef {

	private String path;
	private long lastModified;

	public FileRef(String path, long lastModified) {
		this.path = path;
		this.lastModified = lastModified;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
}
