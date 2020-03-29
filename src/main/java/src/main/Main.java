package src.main;

import javafx.application.Application;
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
import src.events.AddFileInputEvent;
import src.utils.Constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Main extends Application {

	private int FILE_INPUT_SLEEP_TIME;
	private int COUNTER_DATA_LIMIT;
	private int SORT_PROGRESS_LIMIT;
	private String[] DISKS;

	// Setup
	private InputStream inputStream;

	// Utils
	private HBox hBoxInputAndCruncher;
	private VBox vBoxFileInput;
	private ScrollPane fileInputScrollPane;
	private VBox vBoxCrunchers;
	private ScrollPane crunchersScrollPane;

	// Chart
	private LineChart<Number, Number> lineChart;
	private Series<Number, Number> series;
	private Data<Number, Number>[] data;
	private NumberAxis xAxis;
	private NumberAxis yAxis;

	private List<ComboBox<String>> allCrunchersList;

	// Left view
	private Label lblFileInput;
	private Label lblCrunchersLabel;
	private ListView<String> crunchersList;
	private ComboBox<String> cbCruncherNames;
	private HBox hbFileInputRow;
	private Button btnLinkCruncher;
	private Button btnUnlinkCruncher;

	private Label directoriesLabel;
	private ListView<String> directoriesList;
	private ComboBox<String> cbDirectoriesNames;
	private Button btnAddDirectory;
	private Button btnRemoveDirectory;
	private Button btnRemoveDiskInput;
	private Button btnStart;

	private HBox hbDirsFirstRow;
	private HBox hbDirsSecondRow;
	private Label lblStatus;

	// FileInput
	private Label fileInputLabel;
	private ComboBox<String> comboBoxFileInput;
	private Button addFileInputButton;

	// Crunchers
	private Label crunchersLabel;
	private Button addCruncherButton;
	private int cruncherNameCounter;

	private BorderPane mainView;

	// Result
	private VBox vbResult;
	private ListView<String> resultList;
	private Button btnSingleResult;
	private Button btnSumResult;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
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

		Scene scene = new Scene(mainView, Constants.SCREEN_SIZE_WIDTH, Constants.SCREEN_SIZE_HEIGHT);
		stage.setTitle(Constants.PROJECT_NAME);
		stage.setScene(scene);
		stage.show();
	}

	private void initView() {
		this.mainView = new BorderPane();

		this.hBoxInputAndCruncher = new HBox();
		this.hBoxInputAndCruncher.setSpacing(Constants.DEFAULT_PADDING);

		this.allCrunchersList = new ArrayList<>();
		this.cruncherNameCounter = 0;

		this.vBoxFileInput = new VBox();
		this.vBoxFileInput.setSpacing(Constants.DEFAULT_PADDING);
		this.vBoxFileInput.setPadding(new Insets(Constants.DEFAULT_PADDING));

		this.vbResult = new VBox();
		this.vbResult.setSpacing(Constants.DEFAULT_PADDING);
		this.vbResult.setPadding(new Insets(Constants.DEFAULT_PADDING));

		this.resultList = new ListView<>();
		this.resultList.setMaxSize(200, 500);
		this.resultList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		this.btnSingleResult = new Button("Single Result");
		this.btnSumResult = new Button("Sum Result");

		this.vbResult.getChildren().addAll(resultList, btnSingleResult, btnSumResult);

		this.fileInputLabel = new Label("File inputs");
		this.comboBoxFileInput = new ComboBox<>();
		this.addFileInputButton = new Button("Add File Input");
		this.addFileInputButton.setOnAction(new AddFileInputEvent(this));

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
				Label lblCruncherName = new Label("Name: " + cruncherName);
				Label lblCruncherArity = new Label("Arity: " + textInputDialog.getEditor().getText());
				Button btnRemoveCruncher = new Button("Remove Cruncher");

				for (ComboBox<String> comboBox : allCrunchersList) {
					comboBox.getItems().add(cruncherName);
					comboBox.getSelectionModel().select(0);
				}

				this.vBoxCrunchers.getChildren().addAll(lblCruncherName, lblCruncherArity, btnRemoveCruncher);
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
		this.yAxis.setLabel("Count");

		this.lineChart = new LineChart<>(xAxis, yAxis);
		this.lineChart.setTitle(Constants.PROJECT_NAME);

		this.series = new Series<>();
		this.data = new Data[100];
		this.series.setName("Words");

		Random random = new Random(13213);
		int start = 100;

		for (int i = 0; i < 100; i++) {
			int x = start - random.nextInt() % 100;
			int y = x - random.nextInt() % 50;
			start -= random.nextInt() % 100;

			this.data[i] = new Data<>(x, y);
		}

		// Populate the series with data
		this.series.getData().addAll(data);
		this.lineChart.getData().add(series);

		// Set views
		this.mainView.setLeft(hBoxInputAndCruncher);
		this.mainView.setCenter(lineChart);
		this.mainView.setRight(vbResult);
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

	public int getFILE_INPUT_SLEEP_TIME() {
		return FILE_INPUT_SLEEP_TIME;
	}

	public void setFILE_INPUT_SLEEP_TIME(int FILE_INPUT_SLEEP_TIME) {
		this.FILE_INPUT_SLEEP_TIME = FILE_INPUT_SLEEP_TIME;
	}

	public int getCOUNTER_DATA_LIMIT() {
		return COUNTER_DATA_LIMIT;
	}

	public void setCOUNTER_DATA_LIMIT(int COUNTER_DATA_LIMIT) {
		this.COUNTER_DATA_LIMIT = COUNTER_DATA_LIMIT;
	}

	public int getSORT_PROGRESS_LIMIT() {
		return SORT_PROGRESS_LIMIT;
	}

	public void setSORT_PROGRESS_LIMIT(int SORT_PROGRESS_LIMIT) {
		this.SORT_PROGRESS_LIMIT = SORT_PROGRESS_LIMIT;
	}

	public String[] getDISKS() {
		return DISKS;
	}

	public void setDISKS(String[] DISKS) {
		this.DISKS = DISKS;
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

	public Data<Number, Number>[] getData() {
		return data;
	}

	public void setData(Data<Number, Number>[] data) {
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

	public Label getLblFileInput() {
		return lblFileInput;
	}

	public void setLblFileInput(Label lblFileInput) {
		this.lblFileInput = lblFileInput;
	}

	public Label getLblCrunchersLabel() {
		return lblCrunchersLabel;
	}

	public void setLblCrunchersLabel(Label lblCrunchersLabel) {
		this.lblCrunchersLabel = lblCrunchersLabel;
	}

	public ListView<String> getCrunchersList() {
		return crunchersList;
	}

	public void setCrunchersList(ListView<String> crunchersList) {
		this.crunchersList = crunchersList;
	}

	public ComboBox<String> getCbCruncherNames() {
		return cbCruncherNames;
	}

	public void setCbCruncherNames(ComboBox<String> cbCruncherNames) {
		this.cbCruncherNames = cbCruncherNames;
	}

	public HBox getHbFileInputRow() {
		return hbFileInputRow;
	}

	public void setHbFileInputRow(HBox hbFileInputRow) {
		this.hbFileInputRow = hbFileInputRow;
	}

	public Button getBtnLinkCruncher() {
		return btnLinkCruncher;
	}

	public void setBtnLinkCruncher(Button btnLinkCruncher) {
		this.btnLinkCruncher = btnLinkCruncher;
	}

	public Button getBtnUnlinkCruncher() {
		return btnUnlinkCruncher;
	}

	public void setBtnUnlinkCruncher(Button btnUnlinkCruncher) {
		this.btnUnlinkCruncher = btnUnlinkCruncher;
	}

	public Label getDirectoriesLabel() {
		return directoriesLabel;
	}

	public void setDirectoriesLabel(Label directoriesLabel) {
		this.directoriesLabel = directoriesLabel;
	}

	public ListView<String> getDirectoriesList() {
		return directoriesList;
	}

	public void setDirectoriesList(ListView<String> directoriesList) {
		this.directoriesList = directoriesList;
	}

	public ComboBox<String> getCbDirectoriesNames() {
		return cbDirectoriesNames;
	}

	public void setCbDirectoriesNames(ComboBox<String> cbDirectoriesNames) {
		this.cbDirectoriesNames = cbDirectoriesNames;
	}

	public Button getBtnAddDirectory() {
		return btnAddDirectory;
	}

	public void setBtnAddDirectory(Button btnAddDirectory) {
		this.btnAddDirectory = btnAddDirectory;
	}

	public Button getBtnRemoveDirectory() {
		return btnRemoveDirectory;
	}

	public void setBtnRemoveDirectory(Button btnRemoveDirectory) {
		this.btnRemoveDirectory = btnRemoveDirectory;
	}

	public Button getBtnRemoveDiskInput() {
		return btnRemoveDiskInput;
	}

	public void setBtnRemoveDiskInput(Button btnRemoveDiskInput) {
		this.btnRemoveDiskInput = btnRemoveDiskInput;
	}

	public Button getBtnStart() {
		return btnStart;
	}

	public void setBtnStart(Button btnStart) {
		this.btnStart = btnStart;
	}

	public HBox getHbDirsFirstRow() {
		return hbDirsFirstRow;
	}

	public void setHbDirsFirstRow(HBox hbDirsFirstRow) {
		this.hbDirsFirstRow = hbDirsFirstRow;
	}

	public HBox getHbDirsSecondRow() {
		return hbDirsSecondRow;
	}

	public void setHbDirsSecondRow(HBox hbDirsSecondRow) {
		this.hbDirsSecondRow = hbDirsSecondRow;
	}

	public Label getLblStatus() {
		return lblStatus;
	}

	public void setLblStatus(Label lblStatus) {
		this.lblStatus = lblStatus;
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
}
