package tools.plagiatChecker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

public interface IPlagiatChecker {
    double check(String path1, String path2);

    double check(ArrayList<Path> paths1, ArrayList<Path> paths2);

    Map<String, ArrayList<Map<String, Double>>> checkAll(String path);

    Map<String, ArrayList<Map<String, Double>>> checkAll(String path, String taskNameTranslited);

    Map<String, ArrayList<Map<String, Double>>> checkTask(String path, String task);

    String getLanguage();
}
