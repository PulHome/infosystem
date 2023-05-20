package informationsystem.tasksManager;

import com.taskadapter.redmineapi.bean.Issue;
import tools.Translit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TaskInfo {
    private String taskId;
    private String taskName;
    private String taskBody;
    private String taskPath;
    private String iterationPath;
    private List<String> allAvailableTests = new ArrayList<String>();

    public TaskInfo() {
        taskId = "";
        taskName = "";
        taskBody = "";
        taskPath = "";
        iterationPath = "";
    }

    public TaskInfo(Issue issue) {
        this.taskBody = issue.getDescription();
        this.iterationPath = issue.getTargetVersion().getName();
        // ?? this.taskId
        this.taskName = issue.getSubject();
        this.taskPath = Translit.toTranslit(issue.getSubject());
    }

    public TaskInfo(TaskInfo otherTask) {
        this.taskBody = otherTask.taskBody;
        this.taskPath = otherTask.taskPath;
        this.taskName = otherTask.taskName;
        this.iterationPath = otherTask.iterationPath;
        this.taskId = otherTask.taskId;
        this.allAvailableTests = otherTask.allAvailableTests;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskBody() {
        return taskBody;
    }

    public void setTaskBody(String taskBody) {
        this.taskBody = taskBody;
    }

    public String getTaskPath() {
        return taskPath;
    }

    public void setTaskPath(String taskPath) {
        this.taskPath = taskPath;
    }

    public void addTest(String testName) {
        allAvailableTests.add(testName);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other instanceof TaskInfo) {
            return taskId.equals(((TaskInfo) other).getTaskId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return taskId.hashCode();
    }

    public String getTestContent(String value, Boolean needAnswer) {
        String testFileName = allAvailableTests.stream().filter(item -> item.equals(value)).findFirst().get();
        if (needAnswer) {
            testFileName = testFileName.replace(".t", ".a");
        }

        try {
            return Files.readString(Path.of(TasksKeeper.PathToTests, taskPath, testFileName));
        } catch (IOException e) {
            logger.info(e.toString());
        }

        return ((needAnswer) ? "answer" : "test") + " not found";
    }

    public void setTests(List<String> tests) {
        allAvailableTests.clear();
        for (String testName : tests) {
            allAvailableTests.add(testName);
        }
    }

    public void setTestInput(String testFileName, String newValue) {
        try {
            Files.writeString(Path.of(TasksKeeper.PathToTests, taskPath, testFileName), newValue);
        } catch (IOException e) {
            logger.info(e.toString());
        }
    }

    public void setTestOutput(String testFileName, String newValue) {
        try {
            Files.writeString(
                    Path.of(TasksKeeper.PathToTests, taskPath, testFileName.replace(".t", ".a")),
                    newValue);
        } catch (IOException e) {
            logger.info(e.toString());
        }
    }

    public void saveAllData() {

    }

    public String getIterationPath() {
        return iterationPath;
    }

    public void setIterationPath(String iterationPath) {
        this.iterationPath = iterationPath;
    }

    public String toString() {
        return taskId + " - " + iterationPath + " - " + taskName;
    }

    private static Logger logger = Logger.getLogger(TaskInfo.class.getSimpleName());
}
