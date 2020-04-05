package src.events;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.StageStyle;
import src.components.cruncher.CruncherView;
import src.components.input.FileInput;
import src.main.Main;
import src.utils.Constants;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;

public class AddFileInputEvent implements EventHandler<ActionEvent> {

	private Main app;
	private ExecutorService threadPool;
	private FileInput fileInput;

	public AddFileInputEvent(Main app, ExecutorService threadPool) {
		this.app = app;
		this.threadPool = threadPool;
		this.fileInput = new FileInput(threadPool);
		System.out.println("AddFileInputEvent init");
	}

	@Override
	public void handle(ActionEvent actionEvent) {
		// Crunchers
		Label lblFileInput = new Label("File Input: " + app.getComboBoxFileInput().getValue());
		Label lblCrunchersLabel = new Label("Crunchers:");

		ListView<String> crunchersList = new ListView<>();
		crunchersList.setMaxSize(200, 150);
		crunchersList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		ComboBox<String> cbCruncherNames = new ComboBox<>();

		Button btnLinkCruncher = new Button("Link Cruncher");
		Button btnUnlinkCruncher = new Button("Unlink Cruncher");
		btnUnlinkCruncher.setDisable(true);

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

		btnLinkCruncher.setOnAction(event -> {
			String value = cbCruncherNames.getValue();
			crunchersList.getItems().add(value);
		});

		btnUnlinkCruncher.setOnAction(event -> {
			ObservableList<String> selectedCrunchers = crunchersList.getSelectionModel().getSelectedItems();
			System.out.println("SELECTED CRUNCHERS: " + selectedCrunchers);

			if (!selectedCrunchers.isEmpty()) {
				for (String cruncher : selectedCrunchers) {
					crunchersList.getItems().remove(cruncher);
					System.out.println("Cruncher: " + cruncher + " is removed!");
				}
			}
		});

		btnAddDirectory.setOnAction(event -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setInitialDirectory(new File("data/disk1/"));
			File selectedFile = directoryChooser.showDialog(null);
			directoriesList.getItems().add(selectedFile.getAbsolutePath());
		});

		btnRemoveDirectory.setOnAction(event -> {
			ObservableList<String> selectedDirectories = directoriesList.getSelectionModel().getSelectedItems();
			System.out.println("SELECTED DIRECTORIES: " + selectedDirectories);

			if (!selectedDirectories.isEmpty()) {
				try {
					for (String directory : selectedDirectories) {
						directoriesList.getItems().remove(directory);
						System.out.println("Directory: " + directory + " is removed!");
					}
				} catch (NoSuchElementException e) {
					System.err.println("btnRemoveDirectory - NoSuchElementException!");
				}
			}
		});

		btnStart.setOnAction(event -> {
			if (btnStart.getText().equals("Start")) {
				ObservableList<String> linkedDirs = directoriesList.getItems();
				System.out.println("linkedDirs: " + linkedDirs);

				ObservableList<String> linkedCrunchers = crunchersList.getItems();
				System.out.println("linkedCrunchers: " + linkedCrunchers);

				if (!linkedDirs.isEmpty() && !linkedCrunchers.isEmpty()) {
					btnStart.setText("Pause");

					// TODO: File Input
					fileInput.getDirectories().addAll(linkedDirs);

					for (CruncherView view : app.getCruncherViews()) {
						for (String name : linkedCrunchers) {
							if (view.getCruncherName().equals(name)) {
								fileInput.getCruncherList().add(view);
							}
						}
					}

					Thread thread = new Thread(fileInput);
					thread.start();

					app.getFileInputs().add(fileInput);
				} else {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.initStyle(StageStyle.UTILITY);
					alert.setTitle("FileInput Error");
					alert.setHeaderText("Error");
					alert.setContentText("You didn't select any directory!");
					alert.showAndWait();
				}
			} else {
				btnStart.setText("Start");
				fileInput.getDirectories().add("STOP");
			}

//			try {
//				CountWords countWords = new CountWords();
//				String filePath = "data/disk1/A/wiki-1.txt";
//				int arity = 1;
//
//				ImmutableList<Multiset.Entry<Object>> result = countWords.getMostOccurringBOW(filePath, arity);
//				AtomicInteger counter = new AtomicInteger();
//				List<Data<Number, Number>> data = new ArrayList<>();
//
//				for (int i = 0; i < result.size(); i++) {
//					Multiset.Entry<Object> bow = result.get(i);
//					System.out.println(i + ": " + bow);
//					Data<Number, Number> newData = new Data<>(counter.getAndIncrement(), bow.getCount());
//					data.add(newData);
//				}
//
//				app.getSeries().getData().clear();
//				app.getSeries().getData().addAll(data);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		});

//		btnRemoveDiskInput.setOnAction(event -> {
//			List<FileInput> fileInputs = app.getFileInputs();
//			System.out.println("\n>>> " + fileInputs);
//
//			fileInputs.forEach(input -> {
//				input.setBlocked(!input.isBlocked());
//			});
//		});

		app.getAllCrunchersList().add(cbCruncherNames);

		// Crunchers
		app.getvBoxFileInput().getChildren().add(lblFileInput);
		app.getvBoxFileInput().getChildren().add(lblCrunchersLabel);
		app.getvBoxFileInput().getChildren().add(crunchersList);
		app.getvBoxFileInput().getChildren().add(cbCruncherNames);
		app.getvBoxFileInput().getChildren().add(hbFileInputRow);

		// Dirs
		app.getvBoxFileInput().getChildren().add(directoriesLabel);
		app.getvBoxFileInput().getChildren().add(directoriesList);
		app.getvBoxFileInput().getChildren().add(hbDirsFirstRow);
		app.getvBoxFileInput().getChildren().add(hbDirsSecondRow);
		app.getvBoxFileInput().getChildren().add(lblStatus);
	}
}
