package tools;

import data.ConfiguredTask;
import data.LintReportMode;
import org.apache.commons.codec.Charsets;
import redmineManagement.ConnectionWithRedmine;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
    /**
     * * @param reportLines
     *
     * @return
     */
    public static String getPrettyErrorsJava(String[] reportLines) {
        int i = 0;
        StringBuffer result = new StringBuffer();
        while (i < reportLines.length - 2) {
            i++;
            result.append(reportLines[i].replaceAll("^\\[ERROR\\](.*)(\\\\(\\w+)\\.java:)(.*)$",
                    "[ERROR]File: $3.java, Line: $4") + "\n");
        }
        return result.toString();
    }

    public static String getPrettyErrorsCpp(String[] reportLines) {
        int i = 0;
        StringBuilder result = new StringBuilder();
        result.append("<pre>");
        while (i < reportLines.length) {
            result.append(reportLines[i])
                    .append("\r\n");
            i++;
        }

        result.append("</pre>");
        return result.toString();
    }

    /**
     * * @param reportLines
     *
     * @return
     */
    public static String getPrettyErrorsPython(String[] reportLines) {
        int i = 0;
        StringBuilder result = new StringBuilder();
        while (i < reportLines.length - 2) {
            i++;
            result.append(reportLines[i].replaceAll("^(.*)(\\.py:)(.*)$",
                    "[ERROR]Line $3")).append("\n");
        }

        if (result.isEmpty()) {
            Arrays.stream(reportLines).forEach(result::append);
        }

        return result.toString();
    }

    public static String generateErrorMsg(ConfiguredTask task, String lastLineInReport) {
        String msg = "\n PEP8 mismatch found. Some corrections are required!";
        String error = "";

        if (task.getLintReportMode() != null &&
                task.getLintReportMode().getModeNumber() == LintReportMode.NIGHTMARE_MODE)
            error = msg;
        else {
            error = lastLineInReport + msg;
        }

        return error;
    }

    public static String[] readReportFile(String fileDir) {
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(Paths.get(fileDir), Charsets.UTF_8);

            if (lines.isEmpty()) {
                lines = Files.readAllLines(Paths.get(fileDir), Charset.forName("windows-1251"));
            }
        } catch (IOException ex) {
            logger.info(ex.getMessage());
        }

        String result = "";
        int i = lines.size() - 1;
        while (lines.size() > 0) {
            result = lines.get(i);
            lines.remove(i);
            if (!result.isBlank()) {
                break;
            }
            i--;
        }
        if (result.indexOf('(') != -1) {
            result = result.substring(0, result.indexOf('('));
        }
        lines.add(result);
        return lines.toArray(new String[0]);
    }

    public static Integer javaErrorAmountDetectionInFile(String lastlineInFile) {
        try {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher m = pattern.matcher(lastlineInFile);
            m.find();
            String numberOfErrors = m.group();
            if (numberOfErrors != null && !numberOfErrors.isEmpty()) {
                return Integer.parseInt(numberOfErrors);
            }
        } catch (IllegalStateException ex) {
            logger.info(ex.getMessage());
        }
        return 0;
    }

    public static Integer cppErrorAmountDetectionInFile(String string) {
        return javaErrorAmountDetectionInFile(string);
    }

    public static Integer csErrorAmountDetectionInFile(String string) {
        return javaErrorAmountDetectionInFile(string);
    }

    public static Float pythonRatingCheck(String lastStringInReport) {
        ArrayList<String> splittedWords = new ArrayList<>();
        //String result = "";
        if (!lastStringInReport.isEmpty()) {
            for (String word : lastStringInReport.split(" ")) {
                splittedWords.add(word);
            }
        }
        String matchedConstruction = "";
        for (String word : splittedWords) {
            if (Pattern.compile(".?\\d+(\\.)?(\\d+)?").matcher(word).find()) {
                System.out.println(word);
                matchedConstruction = word;
                break;
            }

        }
        for (String rateValue : matchedConstruction.split("/")) {
            matchedConstruction = rateValue;
            break;
        }

        float rateValue = Float.parseFloat(matchedConstruction);
        logger.info(Float.toString(rateValue));
        return rateValue;
    }

    public static List<String> readFile(String fileDir) {
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(Paths.get(fileDir), Charsets.UTF_8);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return lines;
    }

    public static boolean isNullOrEmpty(String testStr) {
        return (testStr == null) || testStr.trim().isEmpty();
    }

    public static String getStringResult(int checkRes) {
        return (checkRes > 0) ? "Passed" : "Failed";
    }

    static Logger logger = Logger.getLogger(TextUtils.class.getSimpleName());

    public static String getTaskName(String pathOfTheTask) {
        if (TextUtils.isNullOrEmpty(pathOfTheTask)) {
            return "";
        }

        String[] parts = pathOfTheTask.split("_");
        return parts.length > 1 ? parts[1] : "";
    }

    public static String getUserName(String pathOfTheTask) {
        String[] parts = pathOfTheTask.split("_")[0].split("\\\\");
        return parts.length > 2 ? parts[2] : "";
    }
}
