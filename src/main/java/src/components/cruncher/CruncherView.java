package src.components.cruncher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import src.main.Main;
import src.utils.Constants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class CruncherView extends VBox {

	private String cruncherName;
	private Label lblCruncherName;
	private Label lblCruncherArity;
	private Button btnRemoveCruncher;
	private CounterCruncher cruncher;
	private VBox vbInputFiles;

	private ExecutorService threadPool;
	private Main app;

	public CruncherView(ExecutorService threadPool,
						Main app,
						BlockingQueue<Map<String, Multiset<Object>>> outputBlockingQueue,
						String cruncherName,
						String arity) {
		this.threadPool = threadPool;
		this.app = app;
		this.cruncherName = cruncherName;
		this.cruncher = new CounterCruncher(threadPool, this, outputBlockingQueue, Integer.parseInt(arity));

		this.lblCruncherName = new Label("Name: " + cruncherName);
		this.lblCruncherArity = new Label("Arity: " + arity);
		this.btnRemoveCruncher = new Button("Remove Cruncher");

		this.btnRemoveCruncher.setOnAction(event -> {
			app.getCruncherViews().remove(this);
			app.getvBoxCrunchers().getChildren().remove(this);
			app.getFileInputs().forEach(fileInputView -> {
				fileInputView.getFileInput().getCruncherList().remove(this);
				fileInputView.getCrunchersList().getItems().remove(cruncherName);
				fileInputView.getCbCruncherNames().getItems().remove(cruncherName);

				if (fileInputView.getCbCruncherNames().getItems().isEmpty()) {
					fileInputView.getBtnLinkCruncher().setDisable(true);
				}

				fileInputView.getBtnUnlinkCruncher().setDisable(true);
			});
			System.out.println("Cruncher " + this + " is removed!");
		});

		this.vbInputFiles = new VBox();
		this.vbInputFiles.setSpacing(Constants.DEFAULT_PADDING);
		this.vbInputFiles.getChildren().add(new Label("Crunching:"));

		// Config
		this.setSpacing(Constants.DEFAULT_PADDING);
		this.setPadding(new Insets(Constants.DEFAULT_PADDING, 0, 0, 0));

		this.getChildren().addAll(lblCruncherName, lblCruncherArity, btnRemoveCruncher, vbInputFiles);

		Thread thread = new Thread(cruncher);
		thread.start();
	}

	public String getCruncherName() {
		return cruncherName;
	}

	public Label getLblCruncherName() {
		return lblCruncherName;
	}

	public Label getLblCruncherArity() {
		return lblCruncherArity;
	}

	public Button getBtnRemoveCruncher() {
		return btnRemoveCruncher;
	}

	public CounterCruncher getCruncher() {
		return cruncher;
	}

	public VBox getVbInputFiles() {
		return vbInputFiles;
	}

	@Override
	public String toString() {
		return cruncherName;
	}
}
