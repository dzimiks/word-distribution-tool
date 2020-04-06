package src.components.output;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import src.utils.Constants;

import java.util.concurrent.ExecutorService;

public class OutputView extends VBox {

	private ListView<String> resultList;
	private Button btnSingleResult;
	private Button btnSumResult;

	private ExecutorService threadPool;
	private CacheOutput cacheOutput;

	public OutputView(ExecutorService threadPool) {
		this.threadPool = threadPool;
		this.cacheOutput = new CacheOutput(threadPool);

		// Config
		this.setSpacing(Constants.DEFAULT_PADDING);
		this.setPadding(new Insets(Constants.DEFAULT_PADDING));

		this.resultList = new ListView<>();
		this.resultList.setMaxSize(200, 500);
		this.resultList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		this.btnSingleResult = new Button("Single Result");
		this.btnSumResult = new Button("Sum Result");

		this.getChildren().addAll(resultList, btnSingleResult, btnSumResult);

		Thread thread = new Thread(cacheOutput);
		thread.start();

		System.out.println("OutputView init\n");
	}

	public ListView<String> getResultList() {
		return resultList;
	}

	public void setResultList(ListView<String> resultList) {
		this.resultList = resultList;
	}

	public Button getBtnSingleResult() {
		return btnSingleResult;
	}

	public void setBtnSingleResult(Button btnSingleResult) {
		this.btnSingleResult = btnSingleResult;
	}

	public Button getBtnSumResult() {
		return btnSumResult;
	}

	public void setBtnSumResult(Button btnSumResult) {
		this.btnSumResult = btnSumResult;
	}

	public CacheOutput getCacheOutput() {
		return cacheOutput;
	}

	public void setCacheOutput(CacheOutput cacheOutput) {
		this.cacheOutput = cacheOutput;
	}
}
