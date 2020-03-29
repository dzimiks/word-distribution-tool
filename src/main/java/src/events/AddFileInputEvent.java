package src.events;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import src.main.Main;

import java.io.File;

public class AddFileInputEvent implements EventHandler<ActionEvent> {

	private Main app;

	public AddFileInputEvent(Main app) {
		this.app = app;
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
		cbCruncherNames.getItems().add("File 1");
		cbCruncherNames.getItems().add("File 2");
		cbCruncherNames.getSelectionModel().select(0);

		Button btnLinkCruncher = new Button("Link Cruncher");
		btnLinkCruncher.setOnAction(e -> crunchersList.getItems().add(cbCruncherNames.getValue()));

		Button btnUnlinkCruncher = new Button("Unlink Cruncher");
		btnUnlinkCruncher.setDisable(true);

		HBox hbFileInputRow = new HBox();
		hbFileInputRow.setSpacing(10);
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

		btnAddDirectory.setOnAction(e -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			File selectedFile = directoryChooser.showDialog(null);
			directoriesList.getItems().add(selectedFile.getAbsolutePath());
		});

		Button btnRemoveDirectory = new Button("Remove Directory");
		btnRemoveDirectory.setDisable(true);
		Button btnRemoveDiskInput = new Button("Remove Disk Input");
		Button btnStart = new Button("Start");

		HBox hbDirsFirstRow = new HBox();
		hbDirsFirstRow.setSpacing(10);
		hbDirsFirstRow.getChildren().add(btnAddDirectory);
		hbDirsFirstRow.getChildren().add(btnRemoveDirectory);

		HBox hbDirsSecondRow = new HBox();
		hbDirsSecondRow.setSpacing(10);
		hbDirsSecondRow.getChildren().add(btnRemoveDiskInput);
		hbDirsSecondRow.getChildren().add(btnStart);

		Label lblStatus = new Label("Idle");

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
