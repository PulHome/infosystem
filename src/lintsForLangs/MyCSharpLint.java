/*
 * To change this license header, choose License Headers in Project RedmineConnectionProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lintsForLangs;
import tools.PvkLogger;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author user
 */
public class MyCSharpLint {
    public MyCSharpLint() {
    }

    public void startCsLint(String attachmentName) {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "python.exe", ".\\csharpLint\\csLintRunner.py", attachmentName);
            builder.redirectErrorStream(true);
            builder.redirectOutput(new File(attachmentName + "_errorReport.txt"));
            Process p = builder.start();
            p.waitFor();
            if (p.exitValue() != 0)
                throw new IOException("CSharp lint process exited abnormally, it is not expected.");
        } catch (IOException | InterruptedException ex) {
            PvkLogger.getLogger(MyCppLint.class.getSimpleName()).error(ex.toString());
        }
    }
}
