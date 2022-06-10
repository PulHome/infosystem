package tools.plagiatChecker;

import mai.student.CodeComparer;
import tools.PvkLogger;
import tools.TextUtils;
import tools.Translit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class JavaPlagiatChecker implements IPlagiatChecker {

    public JavaPlagiatChecker() {
        CodeComparer.setLoggerHandler(null);
    }

    @Override
    public String getLanguage() {
        return ".java";
    }

    @Override
    public double check(String path1, String path2) {
        return check(createSimpleArrayList(path1), createSimpleArrayList(path2));
    }

    @Override
    public double check(ArrayList<Path> paths1, ArrayList<Path> paths2) {
        CodeComparer codeComparer = null;
        try {
            if (Files.exists(paths1.get(0)) && Files.exists(paths2.get(0))) {
                codeComparer = new CodeComparer(paths1, paths2);
                codeComparer.compare();
                return Math.round(codeComparer.getResult() * 100) / 100.;
            }
        } catch (Exception e) {
            logger.error("Code comparer error:" + e);
        }
        return 0;
    }

    @Override
    public Map<String, ArrayList<Map<String, Double>>> checkAll(String path) {
        return checkAll(path, "");
    }

    @Override
    public Map<String, ArrayList<Map<String, Double>>> checkAll(String path, String taskNameTranslited) {
        Map<String, ArrayList<Map<String, Double>>> comparisonMap = new HashMap<>();
        List<Path> javaFiles = null;
        try {
            if (TextUtils.isNullOrEmpty(taskNameTranslited)) {
                javaFiles = Files.walk(Path.of(path))
                        .filter(p -> p.toString().endsWith(".java"))
                        .collect(Collectors.toList());
            } else {
                javaFiles = Files.walk(Path.of(path))
                        .filter(task -> task.toString().contains(taskNameTranslited.toLowerCase()) && task.toString().endsWith(".java"))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        for (int i = 0; i < javaFiles.size(); i++) {
            String taskName = TextUtils.getTaskName(javaFiles.get(i).toString());
            //if a filename is not correct
            if (TextUtils.isNullOrEmpty(taskName)) {
                continue;
            }
            if (!comparisonMap.containsKey(javaFiles.get(i).toString())) {
                comparisonMap.put(javaFiles.get(i).toString(), new ArrayList<>());
            }

            for (Path otherFile : javaFiles.stream().skip(i + 1)
                    .filter(file -> file.toString().contains(taskName)).collect(Collectors.toList())) {
                CodeComparer codeComparer = null;
                try {
                    codeComparer = new CodeComparer(createSimpleArrayList(javaFiles.get(i)),
                            createSimpleArrayList(otherFile));
                    if (!comparisonMap.containsKey(otherFile.toString())) {
                        comparisonMap.put(otherFile.toString(), new ArrayList<>());
                    }
                } catch (Exception e) {
                    logger.error("CodeComparer crash: " + e);
                }

                if (codeComparer == null) {
                    continue;
                } else {
                    codeComparer.compare();
                    comparisonMap.get(javaFiles.get(i).toString())
                            .add(getMapOfData(otherFile, codeComparer.getResult()));
                    comparisonMap.get(otherFile.toString())
                            .add(getMapOfData(javaFiles.get(i), codeComparer.getResult()));
                }
            }
            comparisonMap.get(javaFiles.get(i).toString())
                    .add(getMapOfData(javaFiles.get(i), 0.0));
        }

        return comparisonMap;
    }

    @Override
    public Map<String, ArrayList<Map<String, Double>>> checkTask(String path, String task) {
        return checkAll(path, Translit.toTranslit(task));
    }

    private Map<String, Double> getMapOfData(Path otherFile, double result) {
        Map<String, Double> map = new HashMap<>();
        map.put(otherFile.toString(), Math.round(result * 100) / 100.);

        return map;
    }

    private ArrayList<Path> createSimpleArrayList(Path... paths) {
        return Arrays.stream(paths).collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<Path> createSimpleArrayList(String... paths) {
        ArrayList<Path> retVal = Arrays.stream(paths).map(Path::of).collect(Collectors.toCollection(ArrayList::new));
        return retVal;
    }

    private static PvkLogger logger = PvkLogger.getLogger(JavaPlagiatChecker.class.getSimpleName());

}
