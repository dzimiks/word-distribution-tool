package src.components.input;

import com.google.common.io.Files;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.StageStyle;
import src.components.cruncher.CruncherView;
import src.main.Main;
import src.utils.Constants;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class FileInputView extends VBox {

	private Main app;
	private ExecutorService threadPool;
	private FileInput fileInput;

	private ListView<String> crunchersList;
	private ComboBox<String> cbCruncherNames;
	private Button btnLinkCruncher;
	private Button btnUnlinkCruncher;
	private Label lblFileInput;

	public FileInputView(ExecutorService threadPool, Main app) {
		this.threadPool = threadPool;
		this.app = app;
		this.fileInput = new FileInput(threadPool);

		init();

		System.out.println("AddFileInputEvent init\n");
	}

	private void init() {
		// Crunchers
		this.lblFileInput = new Label("File Input: " + app.getComboBoxFileInput().getValue());
		Label lblCrunchersLabel = new Label("Crunchers:");

		this.crunchersList = new ListView<>();
		this.crunchersList.setMaxSize(200, 150);
		this.crunchersList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		this.cbCruncherNames = new ComboBox<>();

		this.btnLinkCruncher = new Button("Link Cruncher");
		this.btnUnlinkCruncher = new Button("Unlink Cruncher");

		this.btnLinkCruncher.setDisable(true);
		this.btnUnlinkCruncher.setDisable(true);

		HBox hbFileInputRow = new HBox();
		hbFileInputRow.setSpacing(Constants.DEFAULT_PADDING);
		hbFileInputRow.getChildren().addAll(btnLinkCruncher, btnUnlinkCruncher);

		// Directories
		Label directoriesLabel = new Label("Directories:");
		ListView<String> directoriesList = new ListView<>();
		directoriesList.setMaxSize(200, 150);
		directoriesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		ComboBox<String> cbDirectoriesNames = new ComboBox<>();
		cbDirectoriesNames.getItems().add("Dir 1");
		cbDirectoriesNames.getSelectionModel().select(0);

		Button btnAddDirectory = new Button("Add Directory");
		Button btnRemoveDirectory = new Button("Remove Directory");
		btnRemoveDirectory.setDisable(true);

		Button btnRemoveDiskInput = new Button("Remove Disk Input");
		Button btnStart = new Button("Start");

		HBox hbDirsFirstRow = new HBox();
		hbDirsFirstRow.setSpacing(Constants.DEFAULT_PADDING);
		hbDirsFirstRow.getChildren().add(btnAddDirectory);
		hbDirsFirstRow.getChildren().add(btnRemoveDirectory);

		HBox hbDirsSecondRow = new HBox();
		hbDirsSecondRow.setSpacing(Constants.DEFAULT_PADDING);
		hbDirsSecondRow.getChildren().add(btnRemoveDiskInput);
		hbDirsSecondRow.getChildren().add(btnStart);

		Label lblStatus = new Label("Idle");

		// TODO: Change label idle
		this.fileInput.setLblIdle(lblStatus);
		this.fileInput.getFileInputMiddleware().setLblIdle(lblStatus);

		// Config
		this.setSpacing(Constants.DEFAULT_PADDING);
		this.setPadding(new Insets(Constants.DEFAULT_PADDING, 0, 0, 0));

		// TODO: Actions
		crunchersList.setOnMouseClicked(event -> {
			ObservableList<String> selectedCrunchers = crunchersList.getSelectionModel().getSelectedItems();

			if (!selectedCrunchers.isEmpty()) {
				btnUnlinkCruncher.setDisable(false);
			} else {
				btnUnlinkCruncher.setDisable(true);
			}
		});

		directoriesList.setOnMouseClicked(event -> {
			ObservableList<String> selectedDirectories = directoriesList.getSelectionModel().getSelectedItems();

			if (!selectedDirectories.isEmpty()) {
				btnRemoveDirectory.setDisable(false);
			} else {
				btnRemoveDirectory.setDisable(true);
			}
		});

		cbCruncherNames.valueProperty().addListener(event -> {
			String value = cbCruncherNames.getValue();
			System.out.println("CB changed to " + value);

			if (crunchersList.getItems().contains(value)) {
				btnLinkCruncher.setDisable(true);
			} else {
				btnLinkCruncher.setDisable(false);
			}
		});

		btnLinkCruncher.setOnAction(event -> {
			String value = cbCruncherNames.getValue();

			if (!crunchersList.getItems().contains(value)) {
				crunchersList.getItems().add(value);
				btnLinkCruncher.setDisable(true);
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.initStyle(StageStyle.UTILITY);
				alert.setTitle("Link Cruncher Error");
				alert.setHeaderText("Error");
				alert.setContentText("Cruncher '" + value + "' has been linked already!");
				alert.showAndWait();
			}
		});

		btnUnlinkCruncher.setOnAction(event -> {
			ObservableList<String> selectedCrunchers = crunchersList.getSelectionModel().getSelectedItems();
			List<String> whatToRemove = new CopyOnWriteArrayList<>();
			System.out.println("SELECTED CRUNCHERS: " + selectedCrunchers);

			if (!selectedCrunchers.isEmpty()) {
				for (String cruncher : selectedCrunchers) {
					whatToRemove.add(cruncher);
					btnLinkCruncher.setDisable(false);
					btnUnlinkCruncher.setDisable(true);
				}

				if (!whatToRemove.isEmpty()) {
					for (String cruncher : whatToRemove) {
						crunchersList.getItems().remove(cruncher);
						System.out.println("Cruncher: " + cruncher + " is removed!");
					}

					whatToRemove.clear();
				}
			}
		});

		btnAddDirectory.setOnAction(event -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			String rootDir = lblFileInput.getText().split(":")[1].trim();
			directoryChooser.setInitialDirectory(new File(rootDir));
			File selectedFile = directoryChooser.showDialog(null);

			if (selectedFile != null) {
				String value = selectedFile.getAbsolutePath();

				if (!directoriesList.getItems().contains(value)) {
					directoriesList.getItems().add(value);
					fileInput.getDirectories().add(value);
				} else {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.initStyle(StageStyle.UTILITY);
					alert.setTitle("Add Directory Error");
					alert.setHeaderText("Error");
					alert.setContentText("Directory '" + value + "' has been added already!");
					alert.showAndWait();
				}
			}
		});

		btnRemoveDirectory.setOnAction(event -> {
			ObservableList<String> selectedDirectories = directoriesList.getSelectionModel().getSelectedItems();
			List<String> whatToRemove = new CopyOnWriteArrayList<>();
			System.out.println("SELECTED DIRECTORIES: " + selectedDirectories);

			if (!selectedDirectories.isEmpty()) {
				for (String directory : selectedDirectories) {
					whatToRemove.add(directory);
					btnRemoveDirectory.setDisable(true);
				}

				// TODO: Remove dirs
				if (!whatToRemove.isEmpty()) {
					for (String directory : whatToRemove) {
						File rootDir = new File(directory);
						directoriesList.getItems().remove(directory);
						System.out.println("Directory: " + directory + " is removed!");

						for (File file : Files.fileTraverser().depthFirstPreOrder(rootDir)) {
							String fileName = file.getName();

							if (fileName.endsWith(".txt")) {
								String absolutePath = file.getAbsolutePath();

								if (!fileInput.getSeenFiles().containsKey(absolutePath)) {
									fileInput.getSeenFiles().remove(absolutePath);
									System.out.println("File: " + absolutePath + " is removed!");
								}
							}
						}
					}

					whatToRemove.clear();
				}
			}
		});

		btnStart.setOnAction(event -> {
			Thread thread = new Thread(fileInput);

			if (btnStart.getText().equals("Start")) {
				ObservableList<String> linkedDirs = directoriesList.getItems();
				System.out.println("linkedDirs: " + linkedDirs);

				ObservableList<String> linkedCrunchers = crunchersList.getItems();
				System.out.println("linkedCrunchers: " + linkedCrunchers);

				if (!linkedDirs.isEmpty() && !linkedCrunchers.isEmpty()) {
					System.out.println("Started File Input");
					btnStart.setText("Pause");

					for (CruncherView view : app.getCruncherViews()) {
						for (String name : linkedCrunchers) {
							if (view.getCruncherName().equals(name)) {
								if (!fileInput.getCruncherList().contains(view)) {
									fileInput.getCruncherList().add(view);
								}
							}
						}
					}

					thread.start();
				} else {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.initStyle(StageStyle.UTILITY);
					alert.setTitle("FileInput Error");
					alert.setHeaderText("Error");
					alert.setContentText("You didn't select any directory!");
					alert.showAndWait();
				}
			} else {
				System.out.println("Paused File Input");
				btnStart.setText("Start");
				fileInput.getDirectories().add("STOP");
			}
		});

		btnRemoveDiskInput.setOnAction(event -> {
			app.getFileInputs().remove(this);
			app.getvBoxFileInput().getChildren().remove(this);
			app.getAddFileInputButton().setDisable(false);
			System.out.println("Disk input " + this + " is removed!");
		});

		app.getAllCrunchersList().add(cbCruncherNames);

		// Crunchers
		this.getChildren().add(lblFileInput);
		this.getChildren().add(lblCrunchersLabel);
		this.getChildren().add(crunchersList);
		this.getChildren().add(cbCruncherNames);
		this.getChildren().add(hbFileInputRow);

		// Dirs
		this.getChildren().add(directoriesLabel);
		this.getChildren().add(directoriesList);
		this.getChildren().add(hbDirsFirstRow);
		this.getChildren().add(hbDirsSecondRow);
		this.getChildren().add(lblStatus);

		app.getvBoxFileInput().getChildren().add(this);
	}

	public Label getLblFileInput() {
		return lblFileInput;
	}

	public FileInput getFileInput() {
		return fileInput;
	}

	public ListView<String> getCrunchersList() {
		return crunchersList;
	}

	public ComboBox<String> getCbCruncherNames() {
		return cbCruncherNames;
	}

	public Button getBtnLinkCruncher() {
		return btnLinkCruncher;
	}

	public Button getBtnUnlinkCruncher() {
		return btnUnlinkCruncher;
	}
}
