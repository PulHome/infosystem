/*
 * To change this license header, choose License Headers in Project RedmineConnectionProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package informationsystem.tasksManager;

import informationsystem.xml.TasksXmlReader;
import informationsystem.xml.XmlReader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import tools.PvkLogger;
import tools.TextUtils;
import tools.Translit;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

/**
 * @author user
 */
public class FxmlTasksController implements Initializable {

    private final XmlReader reader = new XmlReader();
    private TaskInfo taskInfo;

    @FXML
    private Accordion tasksWindow;

    @FXML
    private SplitPane tasksDescriptionPane;

    @FXML
    private TextField txtTaskTitle;

    @FXML
    private TextArea txtTaskDescription;

    @FXML
    private TextField txtTestFolderName;

    @FXML
    private TitledPane paneTaskDescription;

    @FXML
    private Button btnOpenTestFolder;

    public FxmlTasksController() {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tasksWindow.setExpandedPane(tasksWindow.getPanes().get(0));
        tasksDescriptionPane.setDividerPosition(0, 0.14);
        tasksWindow.expandedPaneProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        tasksDescriptionPane.setDividerPosition(0, 0.14);
                    }
                });

        txtTaskTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isBlank() || newValue.equals(oldValue)) {
                return;
            }
            taskInfo.setTaskName(newValue);
        });

        txtTaskDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isBlank() || newValue.equals(oldValue)) {
                return;
            }
            taskInfo.setTaskBody(newValue);
        });

        txtTestFolderName.textProperty().addListener((observable, oldValue, newValue) -> {
            // TODO: если стереть все и сохранит
            //  будет сохранен последний непустой символ, подумать надо ли править

            if (newValue.isBlank() || newValue.equals(oldValue)) {
                return;
            }

            taskInfo.setTaskPath(newValue);
        });
    }

    public void shutdown() {
        saveTask(taskInfo);
    }

    public void setTask(TaskInfo taskInfo) {
        if (taskInfo != null) {
            this.taskInfo = taskInfo;
        }

        if (!TextUtils.isNullOrEmpty(taskInfo.getTaskName())) {
            txtTaskTitle.setText(taskInfo.getTaskName());
        }

        if (!TextUtils.isNullOrEmpty(taskInfo.getTaskBody())) {
            txtTaskDescription.setText(taskInfo.getTaskBody());
        }

        if (!TextUtils.isNullOrEmpty(taskInfo.getTaskPath())) {
            txtTestFolderName.setText(taskInfo.getTaskPath());
        }
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    private void saveTask(TaskInfo selectedTask) {
        String xmlWithTests = "TestsInfo_v2.xml";
        if (TextUtils.isNullOrEmpty(selectedTask.getTaskPath())) {
            selectedTask.setTaskPath(Translit.toTranslit(selectedTask.getTaskName()));
        }

        TasksXmlReader reader = new TasksXmlReader(xmlWithTests);
        if (reader.exists(taskInfo)) {
            reader.saveTask(selectedTask);
        } else {
            reader.addTask(selectedTask);
        }
    }

    @FXML
    private void btnOpenTestFolderClick() {
        if (TextUtils.isNullOrEmpty(txtTestFolderName.getText())) {
            txtTestFolderName.setText(Translit.toTranslit(txtTaskTitle.getText()));
        }

        //save changes to task updating folder path
        taskInfo.setTaskPath(txtTestFolderName.getText());
        saveTask(taskInfo);

        String testsFolder = ".//pylint//tests//" + txtTestFolderName.getText();
        Path testsPath = Path.of(testsFolder);
        if (!Files.exists(testsPath)) {
            try {
                Files.createDirectory(testsPath);
            } catch (IOException e) {
                logger.error("Can't create test directory: " + testsFolder);
            }
        }

        ProcessBuilder proc = new ProcessBuilder();
        proc.command("explorer", testsPath.toAbsolutePath().toString());
        try {
            proc.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static PvkLogger logger = PvkLogger.getLogger(FxmlTasksController.class.getSimpleName());
}
