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

        if (data.testName.isEmpty()) {
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
        String csFileToCompile = data.fileName;
        if (!Files.exists(Paths.get(csFileToCompile))) {
            csFileToCompile = workingDir + data.fileName;
        }

        if (!Files.exists(Paths.get(csFileToCompile))) {
            return;
        }

        try {
            Path pathToFileToCompile = new File(csFileToCompile).toPath();
            data.fileName = pathToFileToCompile.toAbsolutePath().toString();

            //ToDO: read CSC PAth from settings
            String csCompilerPath = "C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\csc.exe";

            ProcessBuilder builder = new ProcessBuilder(csCompilerPath, new File(data.fileName).getAbsolutePath());
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
            PvkLogger.getLogger(CsTaskChecker.class.getName())
                    .error("Failed to compile CS code! " + ex.getMessage());
            return;
        }

        RunTestsForExe(sb, data);
        //removeTempFiles(data);
    }

    private void RunTestsForExe(StringBuilder sb, FileAndItsTest data) {
        try {
            String relative = new File(workingDir).toURI()
                    .relativize(new File(data.fileName).toURI()).getPath()
                    .replace(".cs", ".exe");

            ProcessBuilder builder = new ProcessBuilder("python.exe",
                    "csTestRunner.py", relative,
                    data.testName,
                    isInEasyMode() ? "True" : "");
            builder.redirectErrorStream(true);
            builder.directory(new File(workingDir));
            Process p = builder.start();

            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            sb.delete(0, sb.length());
            while ((line = r.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(MyPylint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
