package tools.plagiatChecker;

import lintsForLangs.MyPylint;
import mai.student.CodeComparer;
import tools.PvkLogger;
import tools.TextUtils;
import tools.Translit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PythonPlagiatChecker implements IPlagiatChecker {

    public PythonPlagiatChecker() {

    }

    @Override
    public String getLanguage() {
        return ".py";
    }

    @Override
    public double check(String path1, String path2) {
        return check(createSimpleArrayList(path1), createSimpleArrayList(path2));
    }

    @Override
    public double check(ArrayList<Path> paths1, ArrayList<Path> paths2) {
        try {
            if (Files.exists(paths1.get(0)) && Files.exists(paths2.get(0))) {
                ProcessBuilder builder = new ProcessBuilder(
                        "python",
                        ".\\pylint\\PlagChecker\\main.py",
                        paths1.get(0).toAbsolutePath().toString(),
                        paths2.get(0).toAbsolutePath().toString());
                builder.redirectErrorStream(true);
                Process p = builder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while (true) {
                    line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    logger.info(line);
                    return Double.parseDouble(line);
                }
            }
        } catch (Exception e) {
            logger.error("Code comparer error:" + e);
        }
        return -1;
    }

    @Override
    public Map<String, ArrayList<Map<String, Double>>> checkAll(String path) {
        return checkAll(path, "");
    }

    @Override
    public Map<String, ArrayList<Map<String, Double>>> checkAll(String path, String taskNameTranslited) {
        Map<String, ArrayList<Map<String, Double>>> comparisonMap = new HashMap<>();
        List<Path> pyFiles = null;
        try {
            if (TextUtils.isNullOrEmpty(taskNameTranslited)) {
                pyFiles = Files.walk(Path.of(path))
                        .filter(p -> p.toString().endsWith(getLanguage()))
                        .collect(Collectors.toList());
            } else {
                pyFiles = Files.walk(Path.of(path))
                        .filter(task -> task.toString().contains(taskNameTranslited.toLowerCase())
                                && task.toString().endsWith(getLanguage()))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        for (int i = 0; i < pyFiles.size(); i++) {
            String taskName = TextUtils.getTaskName(pyFiles.get(i).toString());
            //if a filename is not correct
            if (TextUtils.isNullOrEmpty(taskName)) {
                continue;
            }
            if (!comparisonMap.containsKey(pyFiles.get(i).toString())) {
                comparisonMap.put(pyFiles.get(i).toString(), new ArrayList<>());
            }

            for (Path otherFile : pyFiles.stream().skip(i + 1)
                    .filter(file -> file.toString().contains(taskName)).toList()) {
                double result = check(pyFiles.get(i).toAbsolutePath().toString(),
                        otherFile.toAbsolutePath().toString());
                if (!comparisonMap.containsKey(otherFile.toString())) {
                    comparisonMap.put(otherFile.toString(), new ArrayList<>());
                }

                comparisonMap.get(pyFiles.get(i).toString())
                        .add(getMapOfData(otherFile, result));
                comparisonMap.get(otherFile.toString())
                        .add(getMapOfData(pyFiles.get(i), result));
            }
            comparisonMap.get(pyFiles.get(i).toString())
                    .add(getMapOfData(pyFiles.get(i), 0.0));
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

    private static PvkLogger logger = PvkLogger.getLogger(PythonPlagiatChecker.class.getSimpleName());

}
