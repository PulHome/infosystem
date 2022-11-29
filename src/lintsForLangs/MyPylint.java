/*
 * To change this license header, choose License Headers in Project RedmineConnectionProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lintsForLangs;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author user
 */
public class MyPylint {
    public MyPylint() {
    }

    public void startPylint(String attachmentName) {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    ".\\pylint\\plint_v2.bat", attachmentName);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                Logger.getLogger(MyPylint.class.getName()).info(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(MyPylint.class.getName()).info(ex.toString());
        }
    }
}
