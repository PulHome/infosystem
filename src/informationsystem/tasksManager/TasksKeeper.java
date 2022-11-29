package informationsystem.tasksManager;

import com.taskadapter.redmineapi.bean.Issue;
import informationsystem.xml.TasksXmlReader;
import tools.Translit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TasksKeeper {
    static String PathToTests = ".\\pylint\\tests\\";


    private Set<TaskInfo> allTasks = new HashSet<>();
    private TaskInfo selectedTask;
    private TasksXmlReader reader;

    public TasksKeeper() {

    }

    public static TasksKeeper update(TasksXmlReader reader) {
        return reader.getAllTests();
    }

    public void put(TaskInfo testData) {
        allTasks.add(testData);
    }

    public Set<TaskInfo> getAllTasks() {
        return allTasks;
    }

    public HashMap<String, String> getOldStyleTasks() {
        HashMap<String, String> oldStyleTasks = new HashMap<>();
        for (TaskInfo taskInfo : allTasks) {
            oldStyleTasks.put(taskInfo.getTaskName(), taskInfo.getTaskPath());
        }
        return oldStyleTasks;
    }

    public List<String> getAllTaskNames() {
        return allTasks.stream()
                .flatMap(p -> Stream.of(p.getTaskName()))
                .sorted()
                .collect(Collectors.toList());
    }

    public Map<String, String> getAllTaskFilenamesAndTitles() {
        Map<String, String> allTaskFilenamesAndTitles = new HashMap<>();

//        Set<String> setOfTasks = new HashSet<>();
//        for (String task : getAllTaskNames()) {
//            setOfTasks.add(task);
//        }

        for (String task : getAllTaskNames()) {
            allTaskFilenamesAndTitles.put(Translit.toTranslit(task).toLowerCase(), task);
        }

        return allTaskFilenamesAndTitles;
    }

    public List<String> getTasksByIteration(String iteration) {
        //allTasks.stream().forEach(System.out::println);
        return allTasks.stream().filter(taskInfo -> taskInfo.getIterationPath().equalsIgnoreCase(iteration))
                .flatMap(p -> Stream.of(p.getTaskName()))
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getAllTests(String taskName) throws IOException {
        TaskInfo taskInfo = getTestByName(taskName);
        setSelectedTask(taskInfo);
        List<String> tests = Files.list(Path.of(TasksKeeper.PathToTests, taskInfo.getTaskPath()))
                .filter(path -> path.toString().toLowerCase().endsWith(".t"))
                .flatMap(p -> Stream.of(p.getFileName().toString()))
                .collect(Collectors.toList());
        taskInfo.setTests(tests);

        return tests;
    }

    public TaskInfo getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(TaskInfo task) {
        this.selectedTask = task;
    }

    public TaskInfo getTestByName(String value) {
        Optional<TaskInfo> task = allTasks.stream().filter(item -> item.getTaskName().equals(value))
                .findFirst();
        return task.orElseGet(TaskInfo::new);
    }

    public void addNewTests(List<Issue> issuesToAdd) {
        for (Issue issue : issuesToAdd) {
            addNewTask(issue);
        }
    }

    public void setXmlReader(TasksXmlReader reader) {
        this.reader = reader;
    }

    public void UpdateTasks(List<Issue> issues) {
    }

    public List<String> getAllIterations() {
        return allTasks.stream()
                .map(TaskInfo::getIterationPath).distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public void addNewTask(TaskInfo task) {
        if (!reader.exists(task)) {
            reader.addTask(task);
            allTasks.add(task);
        } else {
            reader.saveTask(task);
        }
    }

    private void addNewTask(Issue issue) {
        TaskInfo task = new TaskInfo(issue);
        addNewTask(task);
    }
}
