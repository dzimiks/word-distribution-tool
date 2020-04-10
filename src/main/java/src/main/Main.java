package src.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import src.components.cruncher.CruncherView;
import src.components.input.FileInputView;
import src.components.output.OutputView;
import src.utils.Constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Main extends Application {

	public static int FILE_INPUT_SLEEP_TIME;
	public static int COUNTER_DATA_LIMIT;
	public static int SORT_PROGRESS_LIMIT;
	public static String[] DISKS;

	// Setup
	private InputStream inputStream;
	private BorderPane mainView;

	// Utils
	private HBox hBoxInputAndCruncher;
	private VBox vBoxFileInput;
	private ScrollPane fileInputScrollPane;
	private VBox vBoxCrunchers;
	private ScrollPane crunchersScrollPane;

	// Chart
	private LineChart<Number, Number> lineChart;
	private Series<Number, Number> series;
	private List<Data<Number, Number>> data;
	private NumberAxis xAxis;
	private NumberAxis yAxis;

	// FileInput
	private Label fileInputLabel;
	private ComboBox<String> comboBoxFileInput;
	private Button addFileInputButton;
	private List<FileInputView> fileInputs;

	// Crunchers
	private Label crunchersLabel;
	private Button addCruncherButton;
	private int cruncherNameCounter;
	private List<CruncherView> cruncherViews;
	private List<ComboBox<String>> allCrunchersList;

	// Output
	private OutputView outputView;
	private List<OutputView> outputViews;

	private ExecutorService inputThreadPool;
	private ExecutorService cruncherThreadPool;
	private ExecutorService outputThreadPool;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		this.inputThreadPool = Executors.newCachedThreadPool();
//		this.cruncherThreadPool = Executors.newCachedThreadPool();
		this.cruncherThreadPool = new ForkJoinPool();
		this.outputThreadPool = Executors.newCachedThreadPool();

		// Init view
		initView();

		// Load data
		loadData();

		// Print
		System.out.println("=== CONFIG ===");
		System.out.println("FILE_INPUT_SLEEP_TIME: " + FILE_INPUT_SLEEP_TIME);
		System.out.println("COUNTER_DATA_LIMIT: " + COUNTER_DATA_LIMIT);
		System.out.println("SORT_PROGRESS_LIMIT: " + SORT_PROGRESS_LIMIT);
		System.out.println("DISKS: " + Arrays.toString(DISKS));

		Alert alert = new Alert(Alert.AlertType.NONE);
		alert.initStyle(StageStyle.UTILITY);
		alert.setTitle("Info");
		alert.setHeaderText("Close App");
		alert.setContentText("Terminating app...");
		stage.setOnHidden(e -> alert.show());

		Scene scene = new Scene(mainView, Constants.SCREEN_SIZE_WIDTH, Constants.SCREEN_SIZE_HEIGHT);
		stage.setTitle(Constants.PROJECT_NAME);
		stage.setScene(scene);
		stage.show();

		stage.setOnCloseRequest(event -> {
			System.out.println("Closing app...");
			alert.show();

			inputThreadPool.shutdown();
			cruncherThreadPool.shutdown();
			outputThreadPool.shutdown();

			cruncherViews.forEach(cruncher -> cruncher.getCruncher().getCruncherMiddleware().stop());
			outputViews.forEach(output -> output.getCacheOutput().getOutputMiddleware().stop());

			try {
				inputThreadPool.awaitTermination(10, TimeUnit.SECONDS);
				cruncherThreadPool.awaitTermination(10, TimeUnit.SECONDS);
				outputThreadPool.awaitTermination(10, TimeUnit.SECONDS);

//				fileInputs.forEach(input -> {
//					System.out.println("CLOSED INPUT");
//					input.getFileInput().getDirectories().add("STOP");
//					input.getFileInput().getFileInputMiddleware().stop();
//				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Platform.exit();
			System.exit(0);
		});
	}

	private void initView() {
		this.mainView = new BorderPane();

		this.fileInputs = new CopyOnWriteArrayList<>();
		this.cruncherViews = new CopyOnWriteArrayList<>();
		this.outputViews = new CopyOnWriteArrayList<>();

		this.hBoxInputAndCruncher = new HBox();
		this.hBoxInputAndCruncher.setSpacing(Constants.DEFAULT_PADDING);

		this.allCrunchersList = new ArrayList<>();
		this.cruncherNameCounter = 0;

		this.vBoxFileInput = new VBox();
		this.vBoxFileInput.setSpacing(Constants.DEFAULT_PADDING);
		this.vBoxFileInput.setPadding(new Insets(Constants.DEFAULT_PADDING));

		this.fileInputLabel = new Label("File inputs");
		this.comboBoxFileInput = new ComboBox<>();
		this.addFileInputButton = new Button("Add File Input");

		this.addFileInputButton.setOnAction(event -> {
			FileInputView fileInputView = new FileInputView(inputThreadPool, this);
			this.fileInputs.add(fileInputView);
			this.addFileInputButton.setDisable(true);
		});

		this.comboBoxFileInput.valueProperty().addListener(event -> {
			String value = comboBoxFileInput.getValue();
			System.out.println("CB changed to " + value);

			// Check if at least one file input view contains selected combo box value
			List<FileInputView> fileInputViewList = fileInputs.stream()
					.filter(fileInputView -> fileInputView.getLblFileInput().getText().contains(value))
					.collect(Collectors.toList());

			if (!fileInputViewList.isEmpty()) {
				this.addFileInputButton.setDisable(true);
			} else {
				this.addFileInputButton.setDisable(false);
			}
		});

		this.vBoxFileInput.getChildren().add(fileInputLabel);
		this.vBoxFileInput.getChildren().add(comboBoxFileInput);
		this.vBoxFileInput.getChildren().add(addFileInputButton);

		this.fileInputScrollPane = new ScrollPane();
		this.fileInputScrollPane.setContent(vBoxFileInput);

		this.vBoxCrunchers = new VBox();
		this.vBoxCrunchers.setSpacing(Constants.DEFAULT_PADDING);
		this.vBoxCrunchers.setPadding(new Insets(Constants.DEFAULT_PADDING));

		this.crunchersLabel = new Label("Crunchers");
		this.addCruncherButton = new Button("Add Cruncher");

		this.addCruncherButton.setOnAction(event -> {
			TextInputDialog textInputDialog = new TextInputDialog("1");
			textInputDialog.setHeaderText("Enter cruncher arity");

			if (textInputDialog.showAndWait().isPresent() && !textInputDialog.getEditor().getText().equals("")) {
				String cruncherName = "Cruncher " + (cruncherNameCounter++);
				CruncherView cruncherView = new CruncherView(
						cruncherThreadPool,
						this,
						outputView.getCacheOutput().getOutputBlockingQueue(),
						cruncherName,
						textInputDialog.getEditor().getText()
				);

				for (ComboBox<String> comboBox : allCrunchersList) {
					comboBox.getItems().add(cruncherName);
					comboBox.getSelectionModel().select(0);
				}

				this.vBoxCrunchers.getChildren().add(cruncherView);
				this.cruncherViews.add(cruncherView);
			}
		});

		this.vBoxCrunchers.getChildren().add(crunchersLabel);
		this.vBoxCrunchers.getChildren().add(addCruncherButton);

		this.crunchersScrollPane = new ScrollPane();
		this.crunchersScrollPane.setContent(vBoxCrunchers);

		this.hBoxInputAndCruncher.getChildren().add(fileInputScrollPane);
		this.hBoxInputAndCruncher.getChildren().add(crunchersScrollPane);

		this.xAxis = new NumberAxis();
		this.yAxis = new NumberAxis();

		this.xAxis.setLabel("Number of Words");
		this.yAxis.setLabel("Occurrence Count");

		this.lineChart = new LineChart<>(xAxis, yAxis);
		this.lineChart.setTitle(Constants.PROJECT_NAME);

		this.series = new Series<>();
		this.data = new ArrayList<>();
		this.series.setName("Words");

		// Populate the series with data
		this.series.getData().addAll(data);
		this.lineChart.getData().add(series);

		// Set views
		this.mainView.setLeft(hBoxInputAndCruncher);
		this.mainView.setCenter(lineChart);

		this.outputView = new OutputView(outputThreadPool, this);
		this.outputViews.add(outputView);
		this.mainView.setRight(outputView);
	}

	public void loadData() throws IOException {
		Properties prop = new Properties();
		this.inputStream = new FileInputStream(Constants.CONFIG_FILE_PATH);
		prop.load(inputStream);

		this.FILE_INPUT_SLEEP_TIME = Integer.parseInt(prop.getProperty(Constants.FILE_INPUT_SLEEP_TIME));
		this.COUNTER_DATA_LIMIT = Integer.parseInt(prop.getProperty(Constants.COUNTER_DATA_LIMIT));
		this.SORT_PROGRESS_LIMIT = Integer.parseInt(prop.getProperty(Constants.SORT_PROGRESS_LIMIT));

		String disksProperty = prop.getProperty(Constants.DISKS);
		this.DISKS = disksProperty.split(";");

		this.comboBoxFileInput.getItems().addAll(DISKS);
		this.comboBoxFileInput.getSelectionModel().select(0);
		this.inputStream.close();
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public HBox gethBoxInputAndCruncher() {
		return hBoxInputAndCruncher;
	}

	public void sethBoxInputAndCruncher(HBox hBoxInputAndCruncher) {
		this.hBoxInputAndCruncher = hBoxInputAndCruncher;
	}

	public VBox getvBoxFileInput() {
		return vBoxFileInput;
	}

	public void setvBoxFileInput(VBox vBoxFileInput) {
		this.vBoxFileInput = vBoxFileInput;
	}

	public ScrollPane getFileInputScrollPane() {
		return fileInputScrollPane;
	}

	public void setFileInputScrollPane(ScrollPane fileInputScrollPane) {
		this.fileInputScrollPane = fileInputScrollPane;
	}

	public VBox getvBoxCrunchers() {
		return vBoxCrunchers;
	}

	public void setvBoxCrunchers(VBox vBoxCrunchers) {
		this.vBoxCrunchers = vBoxCrunchers;
	}

	public ScrollPane getCrunchersScrollPane() {
		return crunchersScrollPane;
	}

	public void setCrunchersScrollPane(ScrollPane crunchersScrollPane) {
		this.crunchersScrollPane = crunchersScrollPane;
	}

	public LineChart<Number, Number> getLineChart() {
		return lineChart;
	}

	public void setLineChart(LineChart<Number, Number> lineChart) {
		this.lineChart = lineChart;
	}

	public Series<Number, Number> getSeries() {
		return series;
	}

	public void setSeries(Series<Number, Number> series) {
		this.series = series;
	}

	public List<Data<Number, Number>> getData() {
		return data;
	}

	public void setData(List<Data<Number, Number>> data) {
		this.data = data;
	}

	public NumberAxis getxAxis() {
		return xAxis;
	}

	public void setxAxis(NumberAxis xAxis) {
		this.xAxis = xAxis;
	}

	public NumberAxis getyAxis() {
		return yAxis;
	}

	public void setyAxis(NumberAxis yAxis) {
		this.yAxis = yAxis;
	}

	public Label getFileInputLabel() {
		return fileInputLabel;
	}

	public void setFileInputLabel(Label fileInputLabel) {
		this.fileInputLabel = fileInputLabel;
	}

	public ComboBox<String> getComboBoxFileInput() {
		return comboBoxFileInput;
	}

	public void setComboBoxFileInput(ComboBox<String> comboBoxFileInput) {
		this.comboBoxFileInput = comboBoxFileInput;
	}

	public Button getAddFileInputButton() {
		return addFileInputButton;
	}

	public void setAddFileInputButton(Button addFileInputButton) {
		this.addFileInputButton = addFileInputButton;
	}

	public Label getCrunchersLabel() {
		return crunchersLabel;
	}

	public void setCrunchersLabel(Label crunchersLabel) {
		this.crunchersLabel = crunchersLabel;
	}

	public Button getAddCruncherButton() {
		return addCruncherButton;
	}

	public void setAddCruncherButton(Button addCruncherButton) {
		this.addCruncherButton = addCruncherButton;
	}

	public BorderPane getMainView() {
		return mainView;
	}

	public void setMainView(BorderPane mainView) {
		this.mainView = mainView;
	}

	public List<ComboBox<String>> getAllCrunchersList() {
		return allCrunchersList;
	}

	public void setAllCrunchersList(List<ComboBox<String>> allCrunchersList) {
		this.allCrunchersList = allCrunchersList;
	}

	public List<FileInputView> getFileInputs() {
		return fileInputs;
	}

	public void setFileInputs(List<FileInputView> fileInputs) {
		this.fileInputs = fileInputs;
	}

	public List<CruncherView> getCruncherViews() {
		return cruncherViews;
	}

	public OutputView getOutputView() {
		return outputView;
	}

	public List<OutputView> getOutputViews() {
		return outputViews;
	}
}
