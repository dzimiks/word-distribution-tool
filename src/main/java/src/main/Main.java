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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import src.utils.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class Main extends Application {

	private int FILE_INPUT_SLEEP_TIME;
	private int COUNTER_DATA_LIMIT;
	private int SORT_PROGRESS_LIMIT;

	// Setup
	private InputStream inputStream;

	// FileInput
	private Label fileInputLabel;
	private ComboBox<String> comboBoxFileInput;
	private Button addFileInputButton;

	// Crunchers
	private Label crunchersLabel;
	private Button addCruncherButton;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		BorderPane borderPane = new BorderPane();

		HBox hBoxInputAndCruncher = new HBox();
		hBoxInputAndCruncher.setSpacing(30);

		VBox vBoxFileInput = new VBox();
		vBoxFileInput.setSpacing(10);
		vBoxFileInput.setPadding(new Insets(10));

		this.fileInputLabel = new Label("File inputs");
		this.comboBoxFileInput = new ComboBox<>();
		this.addFileInputButton = new Button("Add File Input");
		this.addFileInputButton.setOnAction(event -> {
			// Crunchers
			Label lblFileInput = new Label("File Input: " + comboBoxFileInput.getValue());
			Label lblCrunchersLabel = new Label("Crunchers:");

			ListView<String> crunchersList = new ListView<>();
			crunchersList.setMaxSize(200, 150);
			crunchersList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

			ComboBox<String> cbCruncherNames = new ComboBox<>();
			cbCruncherNames.getItems().add("File 1");
			cbCruncherNames.getItems().add("File 2");
			cbCruncherNames.getSelectionModel().select(0);

			Button btnLinkCruncher = new Button("Link Cruncher");
			btnLinkCruncher.setOnAction(e -> crunchersList.getItems().add(cbCruncherNames.getValue()));

			Button btnUnlinkCruncher = new Button("Unlink Cruncher");
			btnUnlinkCruncher.setDisable(true);

			// Directories
			Label directoriesLabel = new Label("Directories:");
			ListView<String> directoriesList = new ListView<>();
			directoriesList.setMaxSize(200, 150);
			directoriesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

			ComboBox<String> cbDirectoriesNames = new ComboBox<>();
			cbDirectoriesNames.getItems().add("Dir 1");
			cbDirectoriesNames.getSelectionModel().select(0);

			Button btnAddDirectory = new Button("Add Directory");
			btnAddDirectory.setOnAction(e -> {
				FileChooser fileChooser = new FileChooser();
				List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

				if (selectedFiles != null) {
					for (File selectedFile : selectedFiles) {
						directoriesList.getItems().add(selectedFile.getAbsolutePath());
					}
				}
			});

			Button btnRemoveDirectory = new Button("Remove Directory");
			btnRemoveDirectory.setDisable(true);
			Button btnRemoveDiskInput = new Button("Remove Disk Input");
			Button btnStart = new Button("Start");

			HBox hbFirstRow = new HBox();
			hbFirstRow.setSpacing(10);
			hbFirstRow.getChildren().add(btnAddDirectory);
			hbFirstRow.getChildren().add(btnRemoveDirectory);

			HBox hbSecondRow = new HBox();
			hbSecondRow.setSpacing(10);
			hbSecondRow.getChildren().add(btnRemoveDiskInput);
			hbSecondRow.getChildren().add(btnStart);

			Label lblStatus = new Label("Idle");

			// Crunchers
			vBoxFileInput.getChildren().add(lblFileInput);
			vBoxFileInput.getChildren().add(lblCrunchersLabel);
			vBoxFileInput.getChildren().add(crunchersList);
			vBoxFileInput.getChildren().add(cbCruncherNames);
			vBoxFileInput.getChildren().add(btnLinkCruncher);
			vBoxFileInput.getChildren().add(btnUnlinkCruncher);

			// Dirs
			vBoxFileInput.getChildren().add(directoriesLabel);
			vBoxFileInput.getChildren().add(directoriesList);
			vBoxFileInput.getChildren().add(hbFirstRow);
			vBoxFileInput.getChildren().add(hbSecondRow);
			vBoxFileInput.getChildren().add(lblStatus);
		});

		vBoxFileInput.getChildren().add(fileInputLabel);
		vBoxFileInput.getChildren().add(comboBoxFileInput);
		vBoxFileInput.getChildren().add(addFileInputButton);

		ScrollPane fileInputScrollPane = new ScrollPane();
		fileInputScrollPane.setContent(vBoxFileInput);

		VBox vBoxCrunchers = new VBox();
		vBoxCrunchers.setSpacing(10);

		this.crunchersLabel = new Label("Crunchers");
		this.addCruncherButton = new Button("Add Cruncher");

		vBoxCrunchers.getChildren().add(crunchersLabel);
		vBoxCrunchers.getChildren().add(addCruncherButton);

		ScrollPane crunchersScrollPane = new ScrollPane();
		crunchersScrollPane.setContent(vBoxCrunchers);

		hBoxInputAndCruncher.getChildren().add(fileInputScrollPane);
		hBoxInputAndCruncher.getChildren().add(crunchersScrollPane);
		borderPane.setLeft(hBoxInputAndCruncher);

		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();

		xAxis.setLabel("Number of Words");
		yAxis.setLabel("Count");

		LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

		lineChart.setTitle(Constants.PROJECT_NAME);
		Series<Number, Number> series = new Series<>();
		Data<Number, Number>[] data = new Data[100];
		series.setName("Words");
		Random random = new Random(13213);
		int start = 100;

		for (int i = 0; i < 100; i++) {
			int x = start - random.nextInt() % 100;
			int y = x - random.nextInt() % 50;
			start -= random.nextInt() % 100;

			data[i] = new Data<>(x, y);
		}

		// Populate the series with data
		series.getData().addAll(data);
		lineChart.getData().add(series);
		borderPane.setCenter(lineChart);

		// Load data
		loadData();

		// Print
		System.out.println("FILE_INPUT_SLEEP_TIME: " + FILE_INPUT_SLEEP_TIME);
		System.out.println("COUNTER_DATA_LIMIT: " + COUNTER_DATA_LIMIT);
		System.out.println("SORT_PROGRESS_LIMIT: " + SORT_PROGRESS_LIMIT);

		Scene scene = new Scene(borderPane, Constants.SCREEN_SIZE_WIDTH, Constants.SCREEN_SIZE_HEIGHT);
		stage.setTitle(Constants.PROJECT_NAME);
		stage.setScene(scene);
		stage.show();
	}

	public void loadData() throws IOException {
		Properties prop = new Properties();
		String configFilePath = Constants.CONFIG_FILE_PATH;

		inputStream = new FileInputStream(configFilePath);
		prop.load(inputStream);

		FILE_INPUT_SLEEP_TIME = Integer.parseInt(prop.getProperty(Constants.FILE_INPUT_SLEEP_TIME));
		COUNTER_DATA_LIMIT = Integer.parseInt(prop.getProperty(Constants.COUNTER_DATA_LIMIT));
		SORT_PROGRESS_LIMIT = Integer.parseInt(prop.getProperty(Constants.SORT_PROGRESS_LIMIT));

		String disksProperty = prop.getProperty(Constants.DISKS);
		String[] disks = disksProperty.split(";");

		comboBoxFileInput.getItems().addAll(disks);
		comboBoxFileInput.getSelectionModel().select(0);
		inputStream.close();
	}
}
