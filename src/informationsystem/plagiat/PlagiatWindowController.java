package informationsystem.plagiat;

import informationsystem.tasksManager.TasksKeeper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import tools.PvkLogger;
import tools.TextUtils;
import tools.Translit;
import tools.plagiatChecker.IPlagiatChecker;
import tools.plagiatChecker.JavaPlagiatChecker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PlagiatWindowController {

    public static final String MY_FILES = ".\\myFiles\\";
    public static final String K_DIFF_3 = ".\\KDiff3\\Kdiff3.exe";
    public static final double LIMIT_FOR_HIGHLIGHTING = 0.15;
    public static final Color COLOR_FOR_HIGHLIGHTING = Color.INDIANRED;

    private String currentLanguage = ".java";

    private final IPlagiatChecker plagiatChecker;

    @FXML
    private Map<String, ArrayList<Map<String, Double>>> tableData;

    private Callback<TableColumn<PlagiatDataModel, Double>, TableCell<PlagiatDataModel, Double>> factory = new Callback<TableColumn<PlagiatDataModel, Double>, TableCell<PlagiatDataModel, Double>>() {
        @Override
        public TableCell<PlagiatDataModel, Double> call(TableColumn<PlagiatDataModel, Double> param) {
            return new TableCell<PlagiatDataModel, Double>() {

                private int columnIndex = param.getTableView().getColumns().indexOf(param);

                @Override
                public void updateIndex(int i) {
                    super.updateIndex(i);
                    if (this.getItem() == null) {
                        return;
                    }

                    if (columnIndex == i + 1) {
                        return;
                    }

                    if (this.getItem() <= LIMIT_FOR_HIGHLIGHTING) {
                        this.backgroundProperty().set(new Background(
                                new BackgroundFill(COLOR_FOR_HIGHLIGHTING, CornerRadii.EMPTY, Insets.EMPTY)));
                    }
                }

                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }
                }
            };
        }
    };

    public PlagiatWindowController(IPlagiatChecker plagiatChecker) {
        this.plagiatChecker = plagiatChecker;
        currentLanguage = plagiatChecker.getLanguage();
    }


    private void setTableData(Map<String, ArrayList<Map<String, Double>>> data) {
        this.tableData = data;
    }

    public void openChooseTaskDialog(ActionEvent e, TasksKeeper keeper, String pathToFiles) {
        if (keeper == null) {
            logger.error("There is no information about Tasks. Wait and retry");
            return;
        }

        List<String> choices = fillTasks(keeper);
        if (choices.isEmpty()) {
            logger.warning("No suitable tasks/files were found in " + MY_FILES + "directory.");
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Выберите задачу");
        dialog.setHeaderText("Список известных Роботу задач");
        dialog.setContentText("Выберите задачу:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(task -> openPlagiatStage(e, task, plagiatChecker.checkTask(pathToFiles, task)));
    }

    private List<String> fillTasks(TasksKeeper keeper) {
        List<String> fileNames = null;
        try {
            fileNames = new HashSet<>(listFilesOldestFirst(MY_FILES).stream()
                    .map(f -> TextUtils.getTaskName(f.toString())).toList()).stream().toList();
        } catch (IOException e) {
            logger.warning("No suitable files found in " + MY_FILES + " directory!");
        }

        List<String> finalSortedList = new ArrayList<>();
        for (int i = 0; i < keeper.getAllTaskNames().size(); i++) {
            String ruTaskName = keeper.getAllTaskNames().get(i);
            for (int j = fileNames.size() - 1; j >= 0; j--) {
                if (TextUtils.isNullOrEmpty(ruTaskName) || TextUtils.isNullOrEmpty(fileNames.get(j))) {
                    continue;
                }

                if (Translit.toTranslit(ruTaskName).toLowerCase().contains(fileNames.get(j))) {
                    finalSortedList.add(ruTaskName);
                    break;
                }
            }
        }

        return new HashSet<>(finalSortedList).stream().toList();
    }

    public void openPlagiatStage(ActionEvent event, String task, Map<String, ArrayList<Map<String, Double>>> data) {
        if (data == null) {
            return;
        }

        // для сортировки списка из Map<String, Double> по первому String
        Comparator<Map<String, Double>> mapComparator = Comparator.comparing(m ->
                m.keySet().iterator().next());

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
            resColumn.setCellFactory(factory);
            table.getColumns().add(resColumn);
        }

        table.setPrefWidth(Region.USE_COMPUTED_SIZE);
        table.setOnMouseClicked(mouseEvent -> {
            int column = table.getSelectionModel().getSelectedCells().get(0).getColumn();
            String name2 = people.get(Math.max((column - 1), 0)).getTaskName();
            String name = table.getSelectionModel().getSelectedItem().getTaskName();
            logger.info("First: " + name + " Second: " + name2);
            runKDiff3(getFullFileName(name, task), getFullFileName(name2, task));
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
                    .filter(path -> path.toString().contains(name) &&
                            path.toString().contains(Translit.toTranslit(task).toLowerCase()) &&
                            path.toString().endsWith(currentLanguage))
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

    private List<Path> listFilesOldestFirst(final String directoryPath) throws IOException {
        try (final Stream<Path> fileStream = Files.list(Paths.get(directoryPath))) {
            return fileStream
                    .filter(path -> path.toString().contains(currentLanguage))
                    .map(Path::toFile)
                    .collect(Collectors.toMap(Function.identity(), File::lastModified))
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .map(File::toPath)  // remove this line if you would rather work with a List<File> instead of List<Path>
                    .collect(Collectors.toList());
        }
    }

    private static PvkLogger logger = PvkLogger.getLogger(PlagiatWindowController.class.getSimpleName());
}
