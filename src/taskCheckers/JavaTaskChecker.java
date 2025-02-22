package taskCheckers;

import lintsForLangs.MyPylint;
import tools.PvkLogger;
import tools.ZipFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class JavaTaskChecker extends TaskChecker {
    public JavaTaskChecker(String subject, String fileToManage, boolean easyMode) {
        this.setSubject(subject);
        this.setFileToManage(fileToManage);
        this.isEasyMode = easyMode;
        workingDir = ".\\JavaDir\\";
    }

    public static void main(String[] args) {
        JavaTaskChecker checker = new JavaTaskChecker("Гражданская оборона(*)", "myFiles/nikitautochkin_grazhdanskayaoborona_civildefense.zip", true);
        checker.startJavaCheck();
    }

    public String startJavaCheck() {
        return startJavaCheck(this.getSubject(), this.getFileToManage());
    }

    public String startJavaCheck(String subject, String fileToManage) throws UnsupportedOperationException {
        StringBuilder sbResultOfTests = new StringBuilder();
        FileAndItsTest data = copyFileToTempFolder(fileToManage);
        data.testName = getTestName3(subject);

        if (data.testName.equals("")) {
            throw new UnsupportedOperationException("Not supported");
        }

        if (data.fileName.endsWith(".java")) {
            JavaCompileSingleJavaFile(sbResultOfTests, data);
        }

        if (data.fileName.endsWith(".zip")) {
            ZipFile zip = new ZipFile();
            File unzipTo = new File(Paths.get(workingDir.substring(2) + data.fileName.split(".zip")[0]).toUri());
            try {
                Files.createDirectories(unzipTo.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            boolean unzipResult = zip.unzipFileToDir(workingDir + data.fileName, unzipTo);

            try (Stream<Path> walk = Files.walk(unzipTo.toPath())) {
                List<String> result = walk.map(Path::toString)
                        .filter(f -> f.endsWith(".java")).toList();
                File mainFile = null;
                boolean wasMainFound = false;
                for (String file : result) {
                    File javaFileFromArchive = new File(file);
                    // check and place a file into the correct subfolder which corresponds to its package
                    String fileNameWithPackage = zip.getPackageName(javaFileFromArchive).replace('.', '\\') +
                            "\\" + javaFileFromArchive.toPath().getFileName();

                    Path pathFrom = Paths.get(javaFileFromArchive.getAbsolutePath());
                    Path pathTo = Paths.get(new File(workingDir.substring(2) + fileNameWithPackage).getAbsolutePath());

                    if (!Files.exists(pathTo.getParent())) {
                        Files.createDirectories(pathTo.getParent());
                    }
                    Files.move(pathFrom, pathTo, StandardCopyOption.REPLACE_EXISTING);

                    //choose the correct startup file
                    if (!wasMainFound) {
                        if (hasMainClass(pathTo.toString())) {
                            mainFile = new File(workingDir.substring(2) + fileNameWithPackage);
                            wasMainFound = true;
                        }
                    }
                }
                //compile 'main' file
                if (mainFile != null) {
                    data.fileName = mainFile.getAbsolutePath();
                }

                //send to python check .class with full path of the main file
                JavaCompileSingleJavaFile(sbResultOfTests, data, false);

            } catch (IOException e) {
                PvkLogger.getLogger(this.getClass().getSimpleName()).error(e.toString());
            }
        }
        //Logger.getLogger(MyPylint.class.getName()).log(Level.INFO, sb.toString());
        return sbResultOfTests.toString();
    }

    private void JavaCompileSingleJavaFile(StringBuilder sb, FileAndItsTest data) {
        JavaCompileSingleJavaFile(sb, data, true);
    }

    private void JavaCompileSingleJavaFile(StringBuilder sb, FileAndItsTest data, boolean needManualProcessing) {
        String javaFileToCompile = data.fileName;
        if (!Files.exists(Paths.get(javaFileToCompile))) {
            javaFileToCompile = workingDir + data.fileName;
        }
        if (!Files.exists(Paths.get(javaFileToCompile))) {
            return;
        }
        try {
            if (needManualProcessing) {
                removePackageFromJavaFile(javaFileToCompile);
                String correctJavaName = getCorrectJavaName(javaFileToCompile) + ".java";
                Path pathFrom = new File(javaFileToCompile).toPath();
                Path pathTo = new File(workingDir.substring(2) + correctJavaName).toPath();

                Files.move(pathFrom, pathTo, StandardCopyOption.REPLACE_EXISTING);
                data.fileName = pathTo.toAbsolutePath().toString();
            }
            String javaCompilerBat = new File(workingDir.substring(2) + "runjavac.bat").getAbsolutePath();

            ProcessBuilder builder = new ProcessBuilder(javaCompilerBat, new File(data.fileName).getAbsolutePath());
            builder.redirectErrorStream(true);
            builder.directory(new File(workingDir));
            Process p = builder.start();

            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                if (line.contains("C:\\")) {
                    line = line.substring(line.lastIndexOf('\\'));
                }
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

        RunTestsForMainClass(sb, data);
        //removeTempFiles(data);
    }

    private void RunTestsForMainClass(StringBuilder sb, TaskChecker.FileAndItsTest data) {
        try {
            String relative = new File(workingDir).toURI().relativize(new File(data.fileName).toURI()).getPath();

            ProcessBuilder builder = new ProcessBuilder("python.exe",
                    "javaTestRunner.py", relative.replace(".java", ".class").replace("\\","."),
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
