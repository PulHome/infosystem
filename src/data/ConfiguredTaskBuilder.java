package data;

import com.taskadapter.redmineapi.bean.Issue;

public class ConfiguredTaskBuilder {
    private Issue issue;
    private String taskCompleter;
    private boolean isNeededForceCheck;
    private boolean isLintRequired;
    private double requiredPythonRating;
    private int javaErrors;
    private boolean easyMode;
    private LintReportMode lintReportMode;

    public ConfiguredTaskBuilder setIssue(Issue issue) {
        this.issue = issue;
        return this;
    }

    public ConfiguredTaskBuilder setTaskCompleter(String taskCompleter) {
        this.taskCompleter = taskCompleter;
        return this;
    }

    public ConfiguredTaskBuilder setIsNeededForceCheck(boolean isNeededForceCheck) {
        this.isNeededForceCheck = isNeededForceCheck;
        return this;
    }

    public ConfiguredTaskBuilder setIsLintRequired(boolean isLintRequired) {
        this.isLintRequired = isLintRequired;
        return this;
    }

    public ConfiguredTaskBuilder setRequiredPythonRating(double requiredPythonRating) {
        this.requiredPythonRating = requiredPythonRating;
        return this;
    }

    public ConfiguredTaskBuilder setJavaErrors(int javaErrors) {
        this.javaErrors = javaErrors;
        return this;
    }

    public ConfiguredTaskBuilder setEasyMode(boolean easyMode) {
        this.easyMode = easyMode;
        return this;
    }

    public ConfiguredTaskBuilder setLintReportMode(LintReportMode lintReportMode) {
        this.lintReportMode = lintReportMode;
        return this;
    }

    public ConfiguredTask createConfiguredTask() {
        return new ConfiguredTask(issue, taskCompleter, isNeededForceCheck, isLintRequired, requiredPythonRating, javaErrors, easyMode, lintReportMode);
    }
}