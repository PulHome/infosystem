package taskCheckers;

import lintsForLangs.MyPylint;
import tools.PvkLogger;
import tools.ZipFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class CsTaskChecker extends TaskChecker {
    public CsTaskChecker(String subject, String fileToManage, boolean easyMode) {
        this.setSubject(subject);
        this.setFileToManage(fileToManage);
        this.isEasyMode = easyMode;
        workingDir = ".\\csharpLint\\";
    }

    public static void main(String[] args) {
        CsTaskChecker checker = new CsTaskChecker("Гражданская оборона(*)", "myFiles/nikitautochkin_grazhdanskayaoborona_civildefense.zip", true);
        checker.startCsCheck();
}

    public String startCsCheck() {
        return startCsCheck(this.getSubject(), this.getFileToManage());
    }

    public String startCsCheck(String subject, String fileToManage) throws UnsupportedOperationException {
        StringBuilder sbResultOfTests = new StringBuilder();
        FileAndItsTest data = copyFileToTempFolder(fileToManage);
        data.testName = getTestName3(subject);

        if (data.testName.equals("")) {
            throw new UnsupportedOperationException("Not supported");
        }

        if (data.fileName.endsWith(".cs")) {
            CsCompileSingleCsFile(sbResultOfTests, data);
        }

        if (data.fileName.endsWith(".zip")) {
            throw new UnsupportedOperationException("Not supported");
        }

        //Logger.getLogger(MyPylint.class.getName()).log(Level.INFO, sb.toString());
        return sbResultOfTests.toString();
    }

    private void CsCompileSingleCsFile(StringBuilder sb, FileAndItsTest data) {
        CsCompileSingleCsFile(sb, data, true);
    }

    private void CsCompileSingleCsFile(StringBuilder sb, FileAndItsTest data, boolean needManualProcessing) {
        String csFileToCompile = data.fileName;
        if (!Files.exists(Paths.get(csFileToCompile))) {
            csFileToCompile = workingDir + data.fileName;
        }
        if (!Files.exists(Paths.get(csFileToCompile))) {
            return;
        }
        try {
            if (needManualProcessing) {
                //removePackageFromJavaFile(csFileToCompile);
                //String correctJavaName = getCorrectJavaName(csFileToCompile) + ".cs";
                String correctCsName = csFileToCompile;
                Path pathFrom = new File(csFileToCompile).toPath();
                Path pathTo = new File(workingDir.substring(2) + correctCsName).toPath();

                Files.move(pathFrom, pathTo, StandardCopyOption.REPLACE_EXISTING);
                data.fileName = pathTo.toAbsolutePath().toString();
            }
            String csCompilerBat = new File(workingDir.substring(2) + "runcsc.bat").getAbsolutePath();

            ProcessBuilder builder = new ProcessBuilder(csCompilerBat, new File(data.fileName).getAbsolutePath());
            builder.redirectErrorStream(true);
            builder.directory(new File(workingDir));
            Process p = builder.start();

            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append("\n");
            }

            if (p.exitValue() != 0) {
                // достаточно просто return, все ошибки останутся в буффере sb, и будут обработаны дальше
                return;
            }
        } catch (IOException ex) {
            PvkLogger.getLogger(MyPylint.class.getName())
                    .error("Failed to compile Java code! " + ex.getMessage());
        }

        RunTestsForExe(sb, data);
        //removeTempFiles(data);
    }

    private void RunTestsForExe(StringBuilder sb, FileAndItsTest data) {
        try {
            String relative = new File(workingDir).toURI().relativize(new File(data.fileName).toURI()).getPath();

            ProcessBuilder builder = new ProcessBuilder("python.exe",
                    "csTestRunner.py", relative.replace("\\","."),
                    data.testName,
                    isInEasyMode() ? "True" : "");
            builder.redirectErrorStream(true);
            builder.directory(new File(workingDir));
            Process p = builder.start();

            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            //BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), "cp1251"));
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(MyPylint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void removePackageFromJavaFile(String fileName) {
        final String maiPackage = "ru.mai.";
        StringBuilder sb = new StringBuilder();
        try {
            List<String> linesOfFile = Files.readAllLines(new File(fileName).toPath());
            for (String line : linesOfFile) {
                if (line.trim().startsWith("package ")) continue;
                if (line.contains(maiPackage)) {
                    line = line.replace(maiPackage, "");
                }
                sb.append(line).append("\n");
            }
            Files.write(new File(fileName).toPath(), Collections.singleton(sb.toString()));
        } catch (IOException ex) {
            Logger.getLogger(MyPylint.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String getCorrectJavaName(String fileName) {
        //TODO: Fix for classes which don't have public modifier.
        try {
            List<String> linesOfFile = Files.readAllLines(new File(fileName).toPath());
            for (String line : linesOfFile) {
                if (!line.contains("public class ")) continue;
                String[] classHeader = line.split(" ");
                return classHeader[classHeader.length - 2];
            }
        } catch (IOException ex) {
            Logger.getLogger(MyPylint.class.getName()).log(Level.SEVERE, null, ex);
        }

        return fileName;
    }

    private boolean hasMainClass(String fileName) {
        try {
            List<String> linesOfFile = Files.readAllLines(new File(fileName).toPath());
            for (String line : linesOfFile) {
                if (!line.contains("public static void main(")) continue;
                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(MyPylint.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }
}
