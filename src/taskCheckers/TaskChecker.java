package taskCheckers;

import informationsystem.XmlReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskChecker {
    protected String subject;
    protected String fileToManage;
    protected String workingDir = ".\\temp\\";

    protected static HashMap<String, String> knownTests;

    static {
        fillData();
    }

    private static void fillData() {
        XmlReader reader = new XmlReader(".\\TestsInfo_v2.xml");
        knownTests = reader.getAlltests();
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getNameForKnownTest(String subject) {
        return getTestName3(subject);
    }
    public String getNameForKnownTest() {
        return getTestName3(getSubject());
    }

    String getFileToManage() {
        return fileToManage;
    }

    void setFileToManage(String fileToManage) {
        this.fileToManage = fileToManage;
    }

    String getTestName3(String subjectFromIssue) {
        String testFolderName;
        testFolderName = knownTests.getOrDefault(subjectFromIssue, "");
        //еще одна попытка найти по неполному совпадению
        if (testFolderName.equals("")){
            for(String test : knownTests.keySet()){
                if (subjectFromIssue.toLowerCase().contains(test.toLowerCase()))
                {
                    testFolderName = knownTests.getOrDefault(test, "");
                }
            }
        }
        return testFolderName;
    }

    void removeTempFiles(FileAndItsTest data) {
        try {
            Files.delete(new File(workingDir + data.fileName).toPath());
            //Files.delete(new File(pyLintDir + data.fileTestName).toPath());
        } catch (IOException ex) {
            Logger.getLogger(PyTaskChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    FileAndItsTest copyFileToTempFolder(String fileToManage) {
        FileAndItsTest retVal = new FileAndItsTest();
        retVal.fileName = new File(fileToManage).toPath().getFileName().toString();
        try {
            Files.copy(new File(fileToManage).toPath(), new File(workingDir + retVal.fileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Logger.getLogger(PyTaskChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retVal;
    }

    class FileAndItsTest {
        String fileName = "file.py";
        String testName = "test";
    }

}
