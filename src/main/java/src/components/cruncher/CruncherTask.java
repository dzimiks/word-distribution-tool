package src.components.cruncher;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

public class CruncherTask extends Task<String> {

	private Label label;
	private String fileName;

	public CruncherTask(Label label, String fileName) {
		this.label = label;
		this.fileName = fileName;
	}

	@Override
	protected String call() throws Exception {
		return fileName;
	}

	@Override
	protected void done() {
		super.done();
		Platform.runLater(() -> {
			this.label.setText("File " + fileName);
		});
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		Alert newAlert = new Alert(Alert.AlertType.INFORMATION, "Done CruncherTask", ButtonType.OK);
		newAlert.showAndWait();
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		Alert newAlert = new Alert(Alert.AlertType.INFORMATION, "Stopped CruncherTask", ButtonType.OK);
		newAlert.showAndWait();
	}
}
