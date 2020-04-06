package src.components.cruncher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import src.utils.Constants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class CruncherView extends VBox {

	private String cruncherName;
	private Label lblCruncherName;
	private Label lblCruncherArity;
	private Button btnRemoveCruncher;
	private List<Label> fileNamesList;
	private CounterCruncher cruncher;

	private ExecutorService threadPool;

	public CruncherView(ExecutorService threadPool,
						BlockingQueue<Map<String, ImmutableList<Multiset.Entry<Object>>>> outputBlockingQueue,
						String cruncherName,
						String arity) {
		this.threadPool = threadPool;
		this.cruncherName = cruncherName;
		this.cruncher = new CounterCruncher(threadPool, outputBlockingQueue, Integer.parseInt(arity));

		this.lblCruncherName = new Label("Name: " + cruncherName);
		this.lblCruncherArity = new Label("Arity: " + arity);
		this.btnRemoveCruncher = new Button("Remove Cruncher");
		this.fileNamesList = new CopyOnWriteArrayList<>();
		this.fileNamesList.add(new Label("Crunching:"));

		// Config
		this.setSpacing(Constants.DEFAULT_PADDING);
		this.setPadding(new Insets(Constants.DEFAULT_PADDING, 0, 0, 0));

		this.getChildren().addAll(lblCruncherName, lblCruncherArity, btnRemoveCruncher);

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

	public List<Label> getFileNamesList() {
		return fileNamesList;
	}

	public CounterCruncher getCruncher() {
		return cruncher;
	}

	@Override
	public String toString() {
		return cruncherName;
	}
}
