/*
 * To change this license header, choose License Headers in Project RedmineConnectionProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package informationsystem;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Journal;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import data.*;
import informationsystem.loggerWindow.LoggerWindowController;
import informationsystem.plagiat.PlagiatWindowController;
import informationsystem.tasksManager.FxmlTasksController;
import informationsystem.tasksManager.TaskInfo;
import informationsystem.tasksManager.TasksKeeper;
import informationsystem.xml.SettingsXmlReader;
import informationsystem.xml.TasksXmlReader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import redmineManagement.ConnectionWithRedmine;
import redmineManagement.RedmineAlternativeReader;
import tools.IssueCrawler;
import tools.PvkLogger;
import tools.TextUtils;
import tools.plagiatChecker.JavaPlagiatChecker;
import tools.plagiatChecker.PythonPlagiatChecker;

/**
 * @author Zerg0s
 */
public class FXMLDocumentController implements Initializable {
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd.MM.YYYY  HH:mm:ss", Locale.getDefault());

    private final TasksXmlReader tasksReader = new TasksXmlReader(".\\TestsInfo_v2.xml");
    private final SettingsXmlReader settingsReader = new SettingsXmlReader(".\\ProjectKey.xml");
    private final String pathToFiles = ".\\myFiles\\";

    private ArrayList<Project> projects = new ArrayList<>();
    private LoggerWindowController loggerWindowController;

    @FXML
    private Button buttonDown;

    @FXML
    private ComboBox comboxVersion;

    @FXML
    private ComboBox comboxProject;

    @FXML
    private ComboBox comboxUserName;

    @FXML
    private TextField textFieldURL;

    @FXML
    private TextField IssueToTestNumber;

    @FXML
    private TextField textFieldJavaErrorAmount;

    @FXML
    private CheckBox checkBoxPerevod;

    @FXML
    private ComboBox comboBoxPythonRating;

    @FXML
    private RadioButton radioButtonStatusClosed;

    @FXML
    private RadioButton radioButtonStatusApproved;

    @FXML
    private RadioButton radioButtonAppointForStudent;

    @FXML
    private RadioButton radioButtonAppointForProfessor;

    @FXML
    private CheckBox checkBoxJavaErrScan;

    @FXML
    private CheckBox easyModeCheck;

    @FXML
    private CheckBox checkAllIterations;

    @FXML
    private CheckBox checkBoxPythonRateScan;

    @FXML
    private Button fileChoose;

    @FXML
    private ListView students;

    @FXML
    private ListView tasks;

    @FXML
    private CheckBox checkboxNeedLint;

    @FXML
    private ComboBox lintErrorsNotificationsType;

    @FXML
    private Button btnTaskPrev;

    @FXML
    private Button btnTaskNext;

    @FXML
    private Button btnGetResults;

    @FXML
    private ComboBox<String> cbxAllAvailableTasks;

    @FXML
    private ComboBox<String> cbxAllTaskAreas;

    @FXML
    private ComboBox<String> cbxTestsForTask;

    @FXML
    private TextArea txtTestInput;

    @FXML
    private TextArea txtTestOutput;

    @FXML
    private Button btnAddNewTask;

    @FXML
    private Button btnAddNewTest;

    @FXML
    private Button btnDetails;

    @FXML
    private Button btnCheckClosed;

    @FXML
    private Button btnDownloadAll;

    @FXML
    private Button btnCheckPlagiat;

    @FXML
    private Tab tasksTab;

    private ConnectionWithRedmine connectionToRedmine;
    private RedmineAlternativeReader journalReader;
    private boolean needLog = false;
    private RedmineConnectionProperties props = new RedmineConnectionProperties();
    private String projectKeyXml = "ProjectKey.xml";
    private TasksKeeper tasksKeeper;

    public void initialize(URL url, ResourceBundle bn) {
        settingsReader.readConfigXML(projectKeyXml);

        settingsReader.getOwners().forEach((ProjectOwner p) -> {
            settingsReader.getUsersNameList().add(p.getName());
        });

        String selectedSupervisor = settingsReader.getSelectedSupervisor();
        if (!selectedSupervisor.isEmpty()) {
            comboxUserName.setValue(selectedSupervisor);
        }

        if (settingsReader.getSelectedProject() != null) {
            comboxProject.setValue(settingsReader.getSelectedProject().getProjectName());
            props.projectKey = settingsReader.getSelectedProject().getId();
        }
        ObservableList<String> userNames = FXCollections.observableArrayList(settingsReader.getUsersNameList());
        comboxUserName.setItems(userNames);

        this.comboxProject.setItems(FXCollections.observableArrayList(projects));

        ArrayList<Float> ratingValues = new ArrayList<Float>();
        for (float a = 10; a >= -3.00; a = (float) (a - 0.25)) {
            ratingValues.add(a);
        }

        comboBoxPythonRating.setItems(FXCollections.observableArrayList(ratingValues));

        final ToggleGroup groupIssueStatus = new ToggleGroup();
        radioButtonStatusClosed.setToggleGroup(groupIssueStatus);
        radioButtonStatusClosed.setSelected(true);
        radioButtonStatusClosed.requestFocus();
        radioButtonStatusApproved.setToggleGroup(groupIssueStatus);

        final ToggleGroup groupAppointment = new ToggleGroup();
        radioButtonAppointForStudent.setToggleGroup(groupAppointment);
        radioButtonAppointForStudent.setSelected(true);
        radioButtonAppointForStudent.requestFocus();
        radioButtonAppointForProfessor.setToggleGroup(groupAppointment);

        easyModeCheck.setSelected(settingsReader.isEasyMode());
        checkAllIterations.setSelected(settingsReader.needCheckAllIterations());

        initializeSelectedProject();
        setSelectedLintMode();
        LoggerWindowController loggerWindow = showLoggerStage(logger);
        logger.setLoggerControllerWindow(loggerWindow);
        journalReader = new RedmineAlternativeReader(props.url, props.apiAccessKey, props.projectKey);

        //read tasks
        tasksKeeper = TasksKeeper.update(tasksReader);
        tasksKeeper.setXmlReader(tasksReader);
    }

    private LoggerWindowController showLoggerStage(PvkLogger logger) {
        Parent root;
        Stage stage;
        Scene sceneForLogger = null;

        try {
            if (loggerWindowController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("loggerWindow/loggerWindow.fxml"));
                root = loader.load();
                loggerWindowController = loader.getController();
                loggerWindowController.setLogger(logger);
                sceneForLogger = new Scene(root);
            }

            if (loggerWindowController.getStage() == null) {
                stage = new Stage();
                stage.setTitle("Логгер ПВК");
                stage.setScene(sceneForLogger);
                stage.initModality(Modality.NONE);
                stage.getIcons().add(new Image(FXMLDocumentController.class.getResourceAsStream("robot.png")));
                stage.setOnHidden(e -> {
                    loggerWindowController.shutdown();
                });
                loggerWindowController.setStage(stage);
            } else {
                stage = loggerWindowController.getStage();
            }

            if (!stage.isShowing()) {
                Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
                double x = bounds.getMinX();
                double y = bounds.getMinY() + (bounds.getHeight() - stage.getHeight());
                stage.setX(x);
                stage.setY(y);
                stage.show();
            } else if (!stage.isFocused()) {
                stage.requestFocus();
            }

            return loggerWindowController;
        } catch (IOException e) {
            logger.error(e.toString());
        }

        return null;
    }

    private void setSelectedLintMode() {
        if (settingsReader.getSelectedLintMode() == null) {
            return;
        }

        lintErrorsNotificationsType.setItems(FXCollections.observableArrayList(getData()));
        ObservableList<LintReportMode> items = lintErrorsNotificationsType.getItems();
        int itemToSelect = items.get(0).getModeNumber();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getModeNumber() == settingsReader.getSelectedLintMode().getModeNumber()) {
                itemToSelect = i;
                break;
            }
        }

        lintErrorsNotificationsType.getSelectionModel().select(itemToSelect);
    }

    private List<LintReportMode> getData() {
        List<LintReportMode> retList = new ArrayList<LintReportMode>();
        retList.add(LintReportMode.valueOf("Default - По умолчанию(показывать, где ошибки)"));
        retList.add(LintReportMode.valueOf("Hard - Только количество ошибок"));
        retList.add(LintReportMode.valueOf("Nightmare - Только наличие ошибок"));
        return retList;
    }

    private void initializeSelectedProject() {
        if (comboxUserName.getValue() != null) {
            if (!comboxUserName.getValue().toString().isEmpty()) {
                for (ProjectOwner owner : settingsReader.getOwners()) {
                    if (comboxUserName.getValue().toString().equals(owner.getName())) {
                        projects = owner.getHisProjects();
                        props.apiAccessKey = owner.getApiKey();
                        comboxProject.setItems(
                                FXCollections.observableArrayList(projects.stream().map(
                                        pr -> pr.getProjectName()
                                ).toArray()));
                        break;
                    }
                }
            }
        }

        if (comboxProject.getValue() != null) {
            String selectedProject = comboxProject.getValue().toString();
            if (!TextUtils.isNullOrEmpty(selectedProject)) {
                props.projectKey = projects.stream().filter(pr ->
                                pr.getProjectName().equals(selectedProject))
                        .findFirst()
                        .get()
                        .getId();
            }
        }

        props.url = fillUrlProps(textFieldURL);

        String selectedIteration = settingsReader.getSelectedVersion();
        if (selectedIteration != null) {
            comboxVersion.setValue(selectedIteration);
            props.iterationName = selectedIteration;
        }

        if (!props.projectKey.isEmpty() && !props.apiAccessKey.isEmpty()
                && !props.url.isEmpty() && !props.iterationName.isEmpty()) {
            logger.info("Selected values: " + props.url + "\n"
                    + props.apiAccessKey.substring(props.apiAccessKey.length() - 3) + "\n" + props.projectKey
                    + "\n" + props.iterationName);

            updateProjectIterationsAsync(props);
        }
    }

    private void updateProjectIterationsAsync(RedmineConnectionProperties aPproperties) {
        new Thread(() -> getAllIterations(aPproperties)).start();
    }

    private String fillUrlProps(TextField textFieldURL) {
        String url = "";

        if (textFieldURL == null || textFieldURL.getText() == null || textFieldURL.getText().isEmpty()) {
            url = "https://www.hostedredmine.com";
        } else {
            url = textFieldURL.getText();
        }

        return url;
    }

    @FXML
    private void handleUserChoice() {
        if (!comboxUserName.getValue().toString().isEmpty()) {
            for (ProjectOwner owner : settingsReader.getOwners()) {
                if (comboxUserName.getValue().toString().equals(owner.getName())) {
                    projects = owner.getHisProjects();
                    props.setNewApiAccessKey(owner.getApiKey());
                    break;
                }
            }
        }

        reFillDataForCombobox(comboxProject, settingsReader.getProjectNameList(comboxUserName.getValue().toString()));
        reFillDataForCombobox(comboxVersion, new ArrayList<String>());
    }

    private void reFillDataForCombobox(ComboBox toCbxToBeRefilled, ArrayList<String> data) {
        ObservableList<String> projectNames = FXCollections.observableArrayList(data);

        toCbxToBeRefilled.getItems().removeAll(toCbxToBeRefilled.getItems());
        toCbxToBeRefilled.setValue(null);
        toCbxToBeRefilled.valueProperty().set(null);
        toCbxToBeRefilled.setItems(projectNames);
    }

    @FXML
    private void loadLists(ActionEvent event) {
        ArrayList<CellWithCheckBox> users = new ArrayList<>();
        ArrayList<CellWithCheckBox> tasks = new ArrayList<>();
        for (String user : connectionToRedmine.getProjectUsers().stream().sorted().collect(Collectors.toList())) {
            users.add(new CellWithCheckBox(user, false));
        }

        for (String task : connectionToRedmine.getIterationFreeTasks(comboxVersion.getValue().toString())) {
            tasks.add(new CellWithCheckBox(task, false));
        }

        StringConverter<CellWithCheckBox> converter = new StringConverter<CellWithCheckBox>() {
            @Override
            public String toString(CellWithCheckBox cell) {
                return cell.getTitle();
            }

            // not actually used by CheckBoxListCell
            @Override
            public CellWithCheckBox fromString(String string) {
                return null;
            }
        };

        students.setCellFactory(CheckBoxListCell.forListView(CellWithCheckBox::completedProperty, converter));
        this.tasks.setCellFactory(CheckBoxListCell.forListView(CellWithCheckBox::completedProperty, converter));

        ObservableList<CellWithCheckBox> itemsWithNames = students.getItems();
        itemsWithNames.clear();
        itemsWithNames.addAll(users);

        ObservableList<CellWithCheckBox> tasksNames = this.tasks.getItems();
        tasksNames.clear();
        tasksNames.addAll(tasks);
    }

    @FXML
    private void copyAndAssignIssues(ActionEvent event) {
        Pattern pattern = Pattern.compile("\\d+", Pattern.CASE_INSENSITIVE);

        tasks.getItems().stream().forEach(item -> ((CellWithCheckBox) item).setCompleted(true));
        students.getItems().stream().forEach(item -> ((CellWithCheckBox) item).setCompleted(true));
        String nameTo = "";

        ArrayList<Integer> issueIds = new ArrayList<>();
        for (Object task : tasks.getItems().toArray()) {
            Matcher m = pattern.matcher(((CellWithCheckBox) task).getTitle());
            m.find();
            issueIds.add(Integer.parseInt(m.group()));
        }

        for (Integer issueId : issueIds) {
            for (int i = 0; i < students.getItems().size(); i++) {
                nameTo = ((CellWithCheckBox) students.getItems().get(i)).getTitle();
                connectionToRedmine.copyAndAssignIssue(issueId, nameTo);
            }
        }
    }

    @FXML
    private void copyAndAssignSelectedIssues(ActionEvent event) {
        Pattern pattern = Pattern.compile("\\d{6,7}", Pattern.CASE_INSENSITIVE);

        String nameTo = "";

        ArrayList<Integer> issueIds = new ArrayList<>();
        for (Object task : tasks.getItems().toArray()) {
            CellWithCheckBox cell = (CellWithCheckBox) task;
            if (cell.isCompleted()) {
                Matcher m = pattern.matcher(cell.getTitle());
                m.find();
                issueIds.add(Integer.parseInt(m.group()));
            }
        }

        for (Integer issueId : issueIds) {
            for (Object student : students.getItems()) {
                CellWithCheckBox cell = (CellWithCheckBox) student;
                if (cell.isCompleted()) {
                    nameTo = cell.getTitle();
                    connectionToRedmine.copyAndAssignIssue(issueId, nameTo);
                }
            }
        }
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        LoggerWindowController loggerWindow = showLoggerStage(logger);
        logger.setLoggerControllerWindow(loggerWindow);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                runMainCheckAllTasks();
            }
        });
        thread.start();
    }

    private void runMainCheckAllTasks() {
        logger.info(LocalDateTime.now().format(formatter) + ": ======Started========\n");
        connectionToRedmine.setProfessorName(comboxUserName.getValue().toString());

        boolean easyMode = easyModeCheck.isSelected();
        boolean checkAll = checkAllIterations.isSelected();
        String iterationName = !checkAll ? comboxVersion.getValue().toString() : "";

        if (!comboxUserName.getValue().toString().isEmpty()
                && !comboxProject.getValue().toString().isEmpty()) {
            List<Issue> issues = null;
            ArrayList<String> journals = null;
            try {
                issues = connectionToRedmine.getMyIssues(iterationName);
            } catch (RedmineException ex) {
                logger.info(ex.toString());
            }

            if (issues != null) {
                issues.stream().forEach(issue -> processIssue(issue, easyMode));
            }

        }
        logger.info(LocalDateTime.now().format(formatter) + ": ======Finished=======\n");
    }

    private void processIssue(Issue issue, boolean easyMode) {
        processIssue(issue, false, false, easyMode);
    }

    private void processIssueForced(Issue issue) {
        processIssueForced(issue, false);
    }

    private void processIssueForced(Issue issue, boolean needLog) {
        processIssue(issue, false, true, false);
    }

    private void processConfiguredIssue(ConfiguredTask task) {
        processIssue(task);
    }

    private void processIssue(Issue issue, boolean needLog, boolean needForced, boolean easyMode) {
        ArrayList<String> journals;
        if (!issue.getStatusName().equals("Closed") && !issue.getStatusName().equals("Approved")) {
            logger.logHtmlIssueLink(issue, props.url + "/issues/" + issue.getId());
            logger.info(" - " + issue.toString());
            Double pyRating = 10.0;
            int javaErrorLimit = 5;
            if (comboBoxPythonRating.getValue() != null) {
                pyRating = Double.parseDouble(comboBoxPythonRating.getValue().toString());
            }

            if (textFieldJavaErrorAmount.getText() != null) {
                javaErrorLimit = Integer.parseInt(textFieldJavaErrorAmount.getText());
            }

            //connectionToRedmine.setVersionForCheck(comboxVersion.getValue().toString(), issue);
            String student = getStudentName(journalReader.getJournals(issue.getId().toString()), connectionToRedmine.getProfessorName());
            logger.info("Student is " + student);
            ConfiguredTask confTask = new ConfiguredTaskBuilder().setIssue(issue).setTaskCompleter(student).setIsNeededForceCheck(needForced).setIsLintRequired(connectionToRedmine.getLint()).setRequiredPythonRating(pyRating).setJavaErrors(javaErrorLimit).setEasyMode(easyMode).setLintReportMode((LintReportMode) lintErrorsNotificationsType.getValue()).createConfiguredTask();
            connectionToRedmine.checkIssueAttachments(confTask);

            journals = journalReader.getJournals(issue.getId().toString());
            connectionToRedmine.setStudentName(getStudentName(journals, comboxUserName.getValue().toString()));
        } else if (needLog) {
            logger.info("Already Closed: " + issue.toString());
        }
    }

    private void processIssue(ConfiguredTask task) {
        Issue issue = task.getIssue();
        if (!isIssueInDeadStatus(issue)) {
            logger.info(issue.toString());
            connectionToRedmine.setStudentName(task.getTaskCompleter());
            connectionToRedmine.checkIssueAttachments(task);
        } else if (needLog) {
            logger.info("Issue is already Closed: " + issue.toString());
        }
    }

    private boolean isIssueInDeadStatus(Issue issue) {
        String status = issue.getStatusName();
        return status.equals("Closed") || status.equals("Approved")
                || status.equals("Blocked");
    }

    private String getStudentName(ArrayList<String> journals, String professorName) {
        String retVal = "";
        for (String journal : journals) {
            if (!journal.equals(professorName)) {
                retVal = journal;
                break;
            }
        }
        return retVal;
    }

    @FXML
    private void handleProjectChoice() {
        props.url = fillUrlProps(textFieldURL);
        for (Project p : projects) {
            if (comboxProject.getValue() != null && p.getProjectName().equals(comboxProject.getValue().toString())) {
                props.projectKey = p.getId();
                break;
            }
        }

        if (!props.projectKey.isEmpty()) {
            updateProjectIterationsAsync(props);
        }
    }

    public void getAllIterations(RedmineConnectionProperties properties) {
        connectionToRedmine = new ConnectionWithRedmine(properties.apiAccessKey, properties.projectKey, properties.url);

        ObservableList<String> targetVersionLost = FXCollections.observableArrayList(
                connectionToRedmine.getVersions(props.projectKey));
        Platform.runLater(() -> comboxVersion.setItems(targetVersionLost));
    }

    @FXML
    private void handleJavaErrorScanCheck() {
        int errorAmmount = Integer.parseInt(textFieldJavaErrorAmount.getText());
        connectionToRedmine.setJavaErrorsAmount(errorAmmount);
    }

    @FXML
    private void handlePyhtonRatingScanCheck() {
        if (checkBoxPythonRateScan.isSelected()) {
            float pythonRating = (float) 10.0;
            if (comboBoxPythonRating.getValue() != null) {
                pythonRating = Float.parseFloat(comboBoxPythonRating.getValue().toString());
            }
            connectionToRedmine.setRating(pythonRating);
        }
    }

    @FXML
    private void handlePerevodCheck() {
        if (checkBoxPerevod.isSelected()) {
            connectionToRedmine.setReturnBackIfAllOk(true);
        } else {
            connectionToRedmine.setReturnBackIfAllOk(false);
        }
    }

    @FXML
    private void handleIssueStatusRadioButton() {
        if (radioButtonStatusApproved.isSelected()) {
            connectionToRedmine.setIssueStatus(4);
        } else if (radioButtonStatusClosed.isSelected()) {
            connectionToRedmine.setIssueStatus(5);
        }
    }

    @FXML
    private void handeRadioButtonAppointments() {

        if (radioButtonAppointForStudent.isSelected()) {
            connectionToRedmine.setAssigneeName(connectionToRedmine.getStudentName());
        } else if (radioButtonAppointForProfessor.isSelected()) {
            connectionToRedmine.setAssigneeName(connectionToRedmine.getProfessorName());
        }
    }

    @FXML
    private void handleCheckboxNeedLint() {
        if (!checkboxNeedLint.isSelected()) {
            connectionToRedmine.setLint(false);
            checkBoxJavaErrScan.setSelected(false);
            checkBoxPythonRateScan.setSelected(false);
        } else {
            connectionToRedmine.setLint(true);
            checkBoxJavaErrScan.setSelected(true);
            checkBoxPythonRateScan.setSelected(true);
        }
    }

    @FXML
    private void seeTestsForATask() {
        tasksTab.getTabPane().getSelectionModel().select(tasksTab);
        String taskName = "Шашки";
        cbxAllAvailableTasks.getSelectionModel().select(taskName);
        cbxTestsForTask.getSelectionModel().select(0);
    }

    @FXML
    //Загружает закрытые задачи для составления списка сдавших
    private void downloadResults() {
        String currentVersion = comboxVersion.getValue().toString();
        List<Issue> tasks = connectionToRedmine.getClosedIssues(currentVersion);
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                Platform.runLater(() -> {
                    btnGetResults.setDisable(true);
                    btnGetResults.setText("В процессе..");
                });
                getResults(currentVersion, tasks);
                Platform.runLater(() -> {
                    btnGetResults.setDisable(false);
                    btnGetResults.setText("Скачать результаты");
                });
                return null;
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    //Загружает закрытые задачи для составления списка сдавших
    private void getResults(String currentVersion, List<Issue> tasks) {
        // First, we must check who has closed the issues
        List<Issue> fraudTasks = checkWhoClosedTheIssues(tasks);
        if (fraudTasks.size() > 0) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                alert.setHeaderText("Проверка закрытых задач. Всего задач: " + tasks.size());
                StringBuilder body = new StringBuilder("Найдено нечестно закрытых: " + String.valueOf(fraudTasks.size()));
                body.append("\n");
                for (Issue issue : fraudTasks) {
                    String badStudent =
                            new RedmineAlternativeReader(props.url, props.apiAccessKey, props.projectKey)
                                    .getStudentsName(issue.getId(), connectionToRedmine.getProfessorName());
                    body.append(badStudent + ":\n" + issue.toString());
                    body.append("\n");
                }
                alert.setContentText(body.toString());
                alert.setTitle("Проверка закрытых задач");
                Optional<ButtonType> result = alert.showAndWait();
            });
            return;
        }

        ArrayList<StudentsIssue> studentsIssues = new ArrayList<>();
        for (Issue task : tasks) {
            String taskStatusName = task.getStatusName();
            if (taskStatusName.equals("Closed")) {
                StudentsIssue currentIssue = new StudentsIssue();
                currentIssue.setStudentsName(task.getAssigneeName());
                currentIssue.setIssueName(task.getSubject());
                studentsIssues.add(currentIssue);
            }
        }

        HashMap<String, ArrayList<String>> issuesOfTheStudent = new HashMap<String, ArrayList<String>>();
        for (StudentsIssue issue : studentsIssues) {
            if (issue.getStudentsName() != null) {
                if (!issuesOfTheStudent.containsKey(issue.getStudentsName())) {
                    issuesOfTheStudent.put(issue.getStudentsName(), new ArrayList<String>());
                }
                issuesOfTheStudent.get(issue.getStudentsName()).add(issue.getIssueName());
            }
        }

        new FileOperator(currentVersion + ".txt").saveDataToFile(issuesOfTheStudent);
    }

    @FXML
    private void addTaskToMyIteration(ActionEvent event) {
        TaskInfo task = new TaskInfo(tasksKeeper.getSelectedTask());
        task.setIterationPath(props.iterationName);
        connectionToRedmine.createNewIssueToRedmine(task);
    }

    @FXML
    private void handleButtonSingleIssueCheckAction(ActionEvent event) {
        String issueNum = IssueToTestNumber.getText();
        int issueNumLong = 0;
        boolean easyMode = easyModeCheck.isSelected();
        connectionToRedmine.setProfessorName(comboxUserName.getValue().toString());
        try {
            issueNumLong = Integer.parseInt(issueNum);
            Issue currentIssue = connectionToRedmine.getIssueByID(issueNumLong);
            boolean isLintNeeded = checkboxNeedLint.isSelected();
            boolean isForceCheck = true;
            double pyRating = 10.0;
            int javaErrorLimit = 0;
            if (comboBoxPythonRating.getValue() != null) {
                pyRating = Double.parseDouble(comboBoxPythonRating.getValue().toString());
            }

            if (textFieldJavaErrorAmount.getText() != null) {
                javaErrorLimit = Integer.parseInt(textFieldJavaErrorAmount.getText());
            }

            String student = getStudentName(journalReader.getJournals(currentIssue.getId().toString()), connectionToRedmine.getProfessorName());

            ConfiguredTask task = new ConfiguredTaskBuilder()
                    .setIssue(currentIssue)
                    .setTaskCompleter(student)
                    .setIsNeededForceCheck(isForceCheck)
                    .setIsLintRequired(isLintNeeded)
                    .setRequiredPythonRating(pyRating)
                    .setJavaErrors(javaErrorLimit)
                    .setEasyMode(easyMode)
                    .setLintReportMode((LintReportMode) lintErrorsNotificationsType.getValue())
                    .createConfiguredTask();

            if (task.getLintReportMode() == null) {
                task.setLintReportMode(new LintReportMode(0, "Default"));
            }

            logger.info(task.toString());
            this.processConfiguredIssue(task);
        } catch (Exception ex) {
            logger.info(ex.toString());
        }
    }

    @FXML
    private void handleButtonPlagiatCheck(ActionEvent event) {
        List<String> choices = Arrays.asList("Java", "Python", "Cpp");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Выберите язык программирования");
        dialog.setHeaderText("Список подерживаемых языков");
        dialog.setContentText("Выберите язык:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(res ->
                {
                    PlagiatWindowController plagiatWindow = null;
                    if (res.equals("Java")) {
                        plagiatWindow = new PlagiatWindowController(new JavaPlagiatChecker());

                    } else if (res.equals("Python")) {
                        plagiatWindow = new PlagiatWindowController(new PythonPlagiatChecker());
                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Данный язык пока не поддерживается", ButtonType.CLOSE);
                        alert.setHeaderText("Данный язык не поддерживается");
                        alert.setTitle("Проверка плагиата");
                        alert.show();
                        return;
                    }

                    plagiatWindow.openChooseTaskDialog(event, tasksKeeper, pathToFiles);
                }
        );
    }

    private void openTaskStage(ActionEvent event, TaskInfo taskInfo) {
        Parent root;
        try {
            if (taskInfo == null) {
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tasksManager/TaskView.fxml"));
            root = loader.load();
            FxmlTasksController tasksController = loader.getController();
            tasksController.setTask(taskInfo);
            Stage stage = new Stage();
            stage.setTitle("Добавить новую задачу");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setOnHidden(e -> {
                TaskInfo task = tasksKeeper.getSelectedTask();
                if (task != null) {
                    updateSelectedItem(cbxAllAvailableTasks, task.getTaskName());
                } else {
                    addNewSelectedItem(cbxAllAvailableTasks, tasksController.getTaskInfo().getTaskName());
                    tasksKeeper.addNewTask(tasksController.getTaskInfo());
                }
                tasksKeeper.setSelectedTask(tasksController.getTaskInfo());
                tasksController.shutdown();
            });
            stage.initOwner(
                    ((Node) event.getSource()).getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addNewSelectedItem(ComboBox<String> cbxAllAvailableTasks, String taskName) {
        cbxAllAvailableTasks.getItems().add(taskName);
        cbxAllAvailableTasks.setValue(taskName);
    }

    private void updateSelectedItem(ComboBox<String> cbx, String task) {
        String oldItem = cbx.getSelectionModel().getSelectedItem();
        if (oldItem == null) {
            oldItem = cbx.getValue();
        }

        ObservableList<String> items = cbx.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equals(oldItem)) {
                items.set(i, task);
                break;
            }
        }

        cbx.setItems(items);
    }

    public void shutdown() {
        HashMap<String, String> map = new HashMap<String, String>();
        putToSettings(map, "isEasyMode", easyModeCheck.isSelected());
        putToSettings(map, "checkAllIterations", checkAllIterations.isSelected());
        putToSettings(map, "SelectedSupervisor", comboxUserName.getValue());
        putToSettings(map, "SelectedIteration", comboxVersion.getValue());

        if (comboxProject.getValue() != null) {
            putToSettings(map, "SelectedProjectName", comboxProject.getValue());
            putToSettings(map, "SelectedProjectId", props.projectKey);
        }

        if (lintErrorsNotificationsType.getValue() != null) {
            LintReportMode lintReportMode = (LintReportMode) lintErrorsNotificationsType.getValue();
            putToSettings(map, "SelectedLintMode", lintReportMode.toStringForXml());
        }

        settingsReader.saveSettings(projectKeyXml, map);
        loggerWindowController.getStage().hide();
    }

    private void putToSettings(Map<String, String> map, Object key, Object value) {
        if (map == null || key == null || value == null) return;
        map.put(String.valueOf(key), String.valueOf(value));
    }

    @FXML
    private void btnPrevTaskClick(ActionEvent event) {
        cbxTestsForTask.getSelectionModel().select(
                (cbxTestsForTask.getSelectionModel().getSelectedIndex() - 1) % cbxTestsForTask.getItems().size());
    }

    @FXML
    private void btnTaskNextClicked(ActionEvent event) {
        cbxTestsForTask.getSelectionModel().select(
                (cbxTestsForTask.getSelectionModel().getSelectedIndex() + 1) % cbxTestsForTask.getItems().size());
    }

    @FXML
    private void initTasksTab() {
        if (!tasksTab.isSelected()) {
            return;
        }

        cbxAllAvailableTasks.setItems(FXCollections.observableArrayList(tasksKeeper.getAllTaskNames()));
        cbxAllTaskAreas.setItems(FXCollections.observableArrayList(tasksKeeper.getAllIterations()));

        txtTestInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if ((cbxTestsForTask.getValue() == null) || oldValue.isBlank()
                    || newValue.isBlank() || newValue.equals(oldValue)) {
                return;
            }
            tasksKeeper.getSelectedTask().setTestInput(cbxTestsForTask.getValue(), newValue);
        });
        txtTestOutput.textProperty().addListener((observable, oldValue, newValue) -> {
            if ((cbxTestsForTask.getValue() == null) || oldValue.isBlank()
                    || newValue.isBlank() || newValue.equals(oldValue)) {
                return;
            }
            tasksKeeper.getSelectedTask().setTestOutput(cbxTestsForTask.getValue(), newValue);
        });

        handleTaskAreaChoice();
    }

    @FXML
    private void handleTaskAreaChoice() {
        String selected = cbxAllTaskAreas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        List<String> tesksByIter = tasksKeeper.getTasksByIteration(selected);
        cbxAllAvailableTasks.setItems(FXCollections.observableArrayList(tesksByIter));
    }

    @FXML
    private void handleTaskChoice() {
        try {
            String selected = cbxAllAvailableTasks.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }

            cbxTestsForTask.getItems().clear();
            cbxTestsForTask.setItems(FXCollections.observableArrayList(
                    tasksKeeper.getAllTests(selected)));
            txtTestInput.clear();
            txtTestOutput.clear();
        } catch (IOException e) {
            logger.info(e.toString());
        }
    }

    @FXML
    private void handleTestChoice() {
        String selected = cbxAllAvailableTasks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        TaskInfo taskInfo = tasksKeeper.getTestByName(selected);
        if (cbxTestsForTask.getValue() != null) {
            txtTestInput.clear();
            txtTestOutput.clear();
            txtTestInput.setText(taskInfo.getTestContent(cbxTestsForTask.getValue().toString(), false));
            txtTestOutput.setText(taskInfo.getTestContent(cbxTestsForTask.getValue().toString(), true));
        }
    }

    @FXML
    private void addNewTask(ActionEvent event) {
        TaskInfo task = new TaskInfo();
        task.setIterationPath(cbxAllTaskAreas.getValue());
        tasksKeeper.setSelectedTask(null);
        openTaskStage(event, task);
    }

    @FXML
    private void editTask(ActionEvent event) {
        openTaskStage(event, tasksKeeper.getSelectedTask());
    }

    @FXML
    private void addNewTest() {
        logger.info("addNew click");
    }

    @FXML
    private void checkWhoClosedTheIssues() {
    }

    private List<Issue> checkWhoClosedTheIssues(List<Issue> tasks) {
        RedmineAlternativeReader redmineReader =
                new RedmineAlternativeReader(props.url, props.apiAccessKey, props.projectKey);
        //redmineReader.getAllClosedIssues();
        final String professofName = comboxUserName.getValue().toString();
        final String currentVersion = comboxVersion.getValue().toString();
        //final List<Issue> tasks = connectionToRedmine.getClosedIssues(currentVersion);
        final List<Issue> fraudTasks = new ArrayList<>();

        Task task = new Task() {
            @Override
            public Void call() {
                for (Issue issue : tasks) {
                    Issue issue1 = connectionToRedmine.getIssueWithJournals(issue.getId());
                    Optional<Journal> completitor = issue1.getJournals().stream()
                            .max((j1, j2) -> Integer.compare(j1.getId(), j2.getId()));
                    String user = completitor.get().getUser().getFullName();
                    if (!user.equals(professofName)) {
                        fraudTasks.add(issue1);
                    }
                }

                return null;
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            logger.info(e.toString());
        }
        return fraudTasks;
    }

    @FXML
    private void btnDownloadAllClick(ActionEvent e) {
        btnDownloadAll.setText(btnDownloadAll.getText().substring(1)); //just for fun
        IssueCrawler crawler = new IssueCrawler(props);
        crawler.getAllProjectIssues();
        TasksKeeper keeper = tasksReader.getAllTests();
        keeper.setXmlReader(tasksReader);
        crawler.substractKnownIssues(keeper.getAllTaskNames());
        keeper.addNewTests(crawler.getIssues());
    }

    @FXML
    private void setSelectedIteration(ActionEvent e) {
        if (comboxVersion.getValue() == null) {
            return;
        }

        if (!comboxVersion.getValue().toString().isEmpty()) {
            String selected = (String) comboxVersion.getValue();
            props.iterationName = selected;
        }
    }


    private static PvkLogger logger = PvkLogger.getLogger(FXMLDocumentController.class.getSimpleName(), false);
}
