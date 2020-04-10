package src.components.output;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import src.main.Main;
import src.utils.Constants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OutputView extends VBox {

	private ListView<String> resultList;
	private Button btnSingleResult;
	private Button btnSumResult;
	private List<String> sumList;

	private ExecutorService threadPool;
	private CacheOutput cacheOutput;
	private Main app;

	private ProgressBar sortProgressBar;
	private ProgressBar sumProgressBar;

	private Label lblProgress;
	private ProgressBarTask currentProgressBarTask;

	public OutputView(ExecutorService threadPool, Main app) {
		this.threadPool = threadPool;
		this.app = app;

		// Config
		this.setSpacing(Constants.DEFAULT_PADDING);
		this.setPadding(new Insets(Constants.DEFAULT_PADDING));

		this.resultList = new ListView<>();
		this.resultList.setMaxSize(200, 500);
		this.resultList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		this.resultList.setOnMouseClicked(event -> {
			ObservableList<String> selectedItems = this.resultList.getSelectionModel().getSelectedItems();
			System.out.println("selectedItems: " + selectedItems);

			if (!selectedItems.isEmpty()) {
				this.btnSingleResult.setDisable(false);

				if (selectedItems.size() > 1) {
					this.btnSumResult.setDisable(false);
				} else {
					this.btnSumResult.setDisable(true);
				}
			} else {
				this.btnSingleResult.setDisable(true);
				this.btnSumResult.setDisable(true);
			}
		});

		this.sumList = new CopyOnWriteArrayList<>();

		this.btnSingleResult = new Button("Single Result");
		this.btnSingleResult.setDisable(true);

		this.btnSingleResult.setOnAction(event -> {
			ObservableList<String> selectedItems = this.resultList.getSelectionModel().getSelectedItems();
			System.out.println("SINGLE RESULT ITEM: " + selectedItems);

			if (selectedItems.size() > 1) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.initStyle(StageStyle.UTILITY);
				alert.setTitle("Output View Error");
				alert.setHeaderText("Error");
				alert.setContentText("You selected more than one file!");
				alert.showAndWait();
			} else {
				try {
					String selectedItem = selectedItems.get(0);
					ImmutableList<Multiset.Entry<Object>> output = poll(selectedItem);

					if (output != null) {
						List<XYChart.Data<Number, Number>> data = new CopyOnWriteArrayList<>();
						AtomicInteger counter = new AtomicInteger(0);

						for (int i = 0; i < output.size(); i++) {
							Multiset.Entry<Object> bow = output.get(i);
							XYChart.Data<Number, Number> newData = new XYChart.Data<>(counter.getAndIncrement(), bow.getCount());
							data.add(newData);

							if (i < 10) {
								System.out.println(i + ": " + bow);
							}
						}

//						progressBarTest(result.size());

						Platform.runLater(() -> {
							app.getSeries().getData().clear();
							app.getSeries().getData().addAll(data);
							app.getLineChart().setTitle("Word Distribution Tool - " + selectedItem);
						});

					} else {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.initStyle(StageStyle.UTILITY);
						alert.setTitle("Output View Error");
						alert.setHeaderText("Error");
						alert.setContentText("This job is not finished!");
						alert.showAndWait();
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		});

		this.btnSumResult = new Button("Sum Result");
		this.btnSumResult.setDisable(true);

		this.btnSumResult.setOnAction(event -> {
			ObservableList<String> selectedItems = resultList.getSelectionModel().getSelectedItems();
			System.out.println("[OutputView]: Selected items: " + selectedItems);

			if (selectedItems.size() > 1) {
				// TODO
				TextInputDialog textInputDialog = new TextInputDialog("sum");
				textInputDialog.setHeaderText("Enter unique sum name");

				if (textInputDialog.showAndWait().isPresent() && !textInputDialog.getEditor().getText().equals("")) {
					String sumName = textInputDialog.getEditor().getText();

					if (!sumList.contains(sumName)) {
						try {
							sumList.add(sumName);
							Map<String, Multiset<Object>> outputData = this.cacheOutput.getOutputMiddleware().getOutputData();

							OutputSumWorker outputSumWorker = new OutputSumWorker(outputData);
							Future<Multiset<Object>> futureResult = threadPool.submit(outputSumWorker);
							Multiset<Object> result = futureResult.get();

							Map<String, Multiset<Object>> output = new ConcurrentHashMap<>();
							output.put(sumName, result);

							this.cacheOutput.getOutputBlockingQueue().put(output);

							// TODO: Is line below necessary?
							this.cacheOutput.getOutputMiddleware().getOutputData().put(sumName, result);
							Platform.runLater(() -> resultList.getItems().add(sumName));
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					} else {
						Alert alert = new Alert(Alert.AlertType.ERROR);
						alert.initStyle(StageStyle.UTILITY);
						alert.setTitle("Output View Error");
						alert.setHeaderText("Error");
						alert.setContentText("Sum '" + sumName + "' already exists!");
						alert.showAndWait();
					}
				}
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.initStyle(StageStyle.UTILITY);
				alert.setTitle("Output View Error");
				alert.setHeaderText("Error");
				alert.setContentText("You've selected just one item!");
				alert.showAndWait();
			}
		});

		this.cacheOutput = new CacheOutput(threadPool, resultList);

		this.sortProgressBar = new ProgressBar();
		this.sumProgressBar = new ProgressBar();
		this.lblProgress = new Label("0%");

		this.getChildren().addAll(resultList, btnSingleResult, btnSumResult, sortProgressBar, sumProgressBar, lblProgress);

		Thread thread = new Thread(cacheOutput);
		thread.start();

//		System.out.println("OutputView init\n");
	}

	public ImmutableList<Multiset.Entry<Object>> poll(String selectedItem) throws ExecutionException, InterruptedException {
		if (selectedItem.charAt(0) != '*') {
			Map<String, Multiset<Object>> outputData = this.cacheOutput.getOutputMiddleware().getOutputData();
			Multiset<Object> result = outputData.get(selectedItem);
			OutputWorker outputWorker = new OutputWorker(result);
			Future<ImmutableList<Multiset.Entry<Object>>> futureResult = threadPool.submit(outputWorker);
			return futureResult.get();
		}

		return null;
	}

	private void progressBarTest(int resultSize) {
		ProgressBarTask progressBarTask = new ProgressBarTask(resultSize);
		currentProgressBarTask = progressBarTask;
		sortProgressBar.progressProperty().bind(progressBarTask.progressProperty());
		sumProgressBar.progressProperty().bind(progressBarTask.progressProperty());
		lblProgress.textProperty().bind(progressBarTask.messageProperty());

		EventHandler<WorkerStateEvent> jobDoneEvent = event -> {
			btnSingleResult.setDisable(false);
			btnSumResult.setDisable(false);
		};

		progressBarTask.setOnSucceeded(jobDoneEvent);
		progressBarTask.setOnCancelled(jobDoneEvent);

		Thread thread = new Thread(progressBarTask);
		thread.start();
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
