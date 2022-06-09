package informationsystem.plagiat;

import informationsystem.tasksManager.FxmlTasksController;
import informationsystem.tasksManager.TaskInfo;
import informationsystem.tasksManager.TasksKeeper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import tools.PvkLogger;
import tools.TextUtils;
import tools.Translit;
import tools.plagiatChecker.PlagiatChecker;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


public class PlagiatWindowController implements Initializable {

    public static final String MY_FILES = ".\\myFiles\\";
    public static final String K_DIFF_3 = ".\\KDiff3\\Kdiff3.exe";

    @FXML
    private Map<String, ArrayList<Map<String, Double>>> tableData;
    private Callback<TableColumn<PlagiatDataModel, Double>, TableCell<PlagiatDataModel, Double>> factory;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    private void setTableData(Map<String, ArrayList<Map<String, Double>>> data) {
        this.tableData = data;
    }

    public void openChooseTaskDialog(ActionEvent e, TasksKeeper keeper, PlagiatChecker checker, String pathToFiles) {
        if (keeper == null) {
            logger.error("There is no information about Tasks. Wait and retry");
        }
        List<String> choices = new HashSet<>(fillTasks(keeper)).stream().sorted().collect(Collectors.toList());
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(20), choices);
        dialog.setTitle("Выберите задачу");
        dialog.setHeaderText("Список известных Роботу задач");
        dialog.setContentText("Выберите задачу:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(task -> openPlagiatStage(e, task, checker.checkTask(pathToFiles, task)));
    }

    private List<String> fillTasks(TasksKeeper keeper) {
        return keeper.getAllTaskNames();
    }

    public void openPlagiatStage(ActionEvent event, String task, Map<String, ArrayList<Map<String, Double>>> data) {
        if (data == null) {
            return;
        }
        // для сортировки списка из Map<String, Double> по первому String
        Comparator<Map<String, Double>> mapComparator = new Comparator<Map<String, Double>>() {
            public int compare(Map<String, Double> m1, Map<String, Double> m2) {
                return m1.keySet().iterator().next().compareTo(m2.keySet().iterator().next());
            }
        };

        // создаем список объектов PlagiatDataModel для таблички
        ObservableList<PlagiatDataModel> people = FXCollections.observableArrayList();

        for (Map.Entry<String, ArrayList<Map<String, Double>>> entry :
                data.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList())) {
            List<Double> results = new ArrayList<>();

            ArrayList<Map<String, Double>> temp = entry.getValue();
            Collections.sort(temp, mapComparator);
            for (Map<String, Double> item : temp) {
                results.add(item.entrySet().iterator().next().getValue());
            }
            people.add(new PlagiatDataModel(TextUtils.getUserName(entry.getKey()), results));
        }

        if (people.size() == 0) {
            logger.warning("No data for the task : " + task);
            return;
        }

        TableView<PlagiatDataModel> table = new TableView<PlagiatDataModel>(people);

        // столбец для вывода имени автора задачи
        TableColumn<PlagiatDataModel, String> nameColumn = new TableColumn<>(task);
        nameColumn.setCellValueFactory(new PropertyValueFactory<PlagiatDataModel, String>("taskName"));
        table.getColumns().add(nameColumn);

        List<String> usersList = data.keySet().stream().sorted().collect(Collectors.toList());

        //столбцы с результатами сравнения
        for (int i = 0; i < people.get(0).getListOfResults().size(); i++) {
            TableColumn<PlagiatDataModel, Double> resColumn = new TableColumn<>(
                    TextUtils.getUserName(new ArrayList<>(usersList).get(i)));
            int finalI = i;
            resColumn.setCellValueFactory(p ->
                    (new SimpleDoubleProperty(p.getValue().getListOfResults().get(finalI))).asObject());
            //resColumn.setCellFactory(factory);
            table.getColumns().add(resColumn);
        }

        table.setPrefWidth(Region.USE_COMPUTED_SIZE);
        table.setOnMouseClicked(mouseEvent -> {
            int column = table.getSelectionModel().getSelectedCells().get(0).getColumn();
            String name2 = people.get(Math.max((column - 1), 0)).getTaskName();
            String name = table.getSelectionModel().getSelectedItem().getTaskName();
            logger.info("First: " + name + " Second: " + name2);
            runKDiff3(getFullFileName(name, task) , getFullFileName(name2,task));
        });

        VBox root = new VBox();
        root.getChildren().add(table);

        Scene scene = new Scene(root, 1000, 250);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(task);
        stage.show();
    }

    private String getFullFileName(String name, String task) {
        try {
            List<Path> javaFiles = Files.walk(Path.of(MY_FILES))
                    .filter(path -> path.toString().contains(name) && path.toString().contains(Translit.toTranslit(task).toLowerCase()) && path.toString().endsWith(".java"))
                    .collect(Collectors.toList());
            return javaFiles.get(0).toString();
        } catch (IOException e) {
            return "";
        }
    }

    private void runKDiff3(String name, String name2) {
        String kDiff3 = K_DIFF_3;

        if (!Files.exists(Path.of(kDiff3))) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Программа сравнения не найдена(KDiff)", ButtonType.CLOSE);
            alert.show();
            return;
        }
        ProcessBuilder builder = new ProcessBuilder(kDiff3, name, name2);
        try {
            builder.start();
        } catch (IOException e) {

        }
    }

    private static PvkLogger logger = PvkLogger.getLogger(PlagiatWindowController.class.getSimpleName());
}
