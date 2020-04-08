package src.components.output;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import src.main.Main;
import src.utils.Constants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class OutputView extends VBox {

	private ListView<String> resultList;
	private Button btnSingleResult;
	private Button btnSumResult;

	private ExecutorService threadPool;
	private CacheOutput cacheOutput;
	private Main app;

	public OutputView(ExecutorService threadPool, Main app) {
		this.threadPool = threadPool;
		this.app = app;

		// Config
		this.setSpacing(Constants.DEFAULT_PADDING);
		this.setPadding(new Insets(Constants.DEFAULT_PADDING));

		this.resultList = new ListView<>();
		this.resultList.setMaxSize(200, 500);
		this.resultList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		this.btnSingleResult = new Button("Single Result");

		this.btnSingleResult.setOnAction(event -> {
			String selectedItem = this.resultList.getSelectionModel().getSelectedItem();
			System.out.println("SINGLE RESULT ITEM: " + selectedItem);

			Map<String, Multiset<Object>> outputData = this.cacheOutput.getOutputMiddleware().getOutputData();
			Multiset<Object> result = outputData.get(selectedItem);
			List<XYChart.Data<Number, Number>> data = new CopyOnWriteArrayList<>();
			AtomicInteger counter = new AtomicInteger(0);

			// First 100 results
			ImmutableList<Multiset.Entry<Object>> fullResult = Multisets.copyHighestCountFirst(result).entrySet().asList();
			ImmutableList<Multiset.Entry<Object>> finalResult = fullResult.subList(0, Math.min(fullResult.size(), 100));

			for (int i = 0; i < finalResult.size(); i++) {
				Multiset.Entry<Object> bow = finalResult.get(i);
				XYChart.Data<Number, Number> newData = new XYChart.Data<>(counter.getAndIncrement(), bow.getCount());
				data.add(newData);

				if (i < 10) {
					System.out.println(i + ": " + bow);
				}
			}

			Platform.runLater(() -> {
				app.getSeries().getData().clear();
				app.getSeries().getData().addAll(data);
				app.getLineChart().setTitle("Word Distribution Tool - " + selectedItem);
			});
		});

		this.btnSumResult = new Button("Sum Result");

		this.cacheOutput = new CacheOutput(threadPool, resultList);
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
