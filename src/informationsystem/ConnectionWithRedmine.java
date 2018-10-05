/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package informationsystem;

import com.taskadapter.redmineapi.AttachmentManager;
import com.taskadapter.redmineapi.Include;
import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.MembershipManager;
import com.taskadapter.redmineapi.ProjectManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.UserManager;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.Charsets;
import org.apache.http.entity.ContentType;
import com.taskadapter.redmineapi.bean.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author user
 */
public class ConnectionWithRedmine {

    private String url;
    private String apiAccessKey;
    private String projectKey;
    private Integer queryId = null;
    private Integer neededJavaErrorAmount;
    private float neededPythonRating = (float) 10.0;
    boolean perevod;
    private String proverka = "";
    private Integer issueStatus;
    private String studentName = "";
    private String professorName = "";
    private String assigneeName = "";

    private RedmineManager mgr;
    private IssueManager issueManager;
    private AttachmentManager attachmentManager;
    private List<Issue> issues;
    private ProjectManager projectManager;
    private List<Project> projects;
    private UserManager userManager;
    private List<User> users;
    private List<Membership> projectsUsers;
    private List<Version> versions;

    public ConnectionWithRedmine(String key, String key2, String url) {
        this.apiAccessKey = key;
        this.projectKey = key2;
        this.url = url;
        this.mgr = RedmineManagerFactory.createWithApiKey(url, apiAccessKey);
        this.issueManager = mgr.getIssueManager();
        this.attachmentManager = mgr.getAttachmentManager();
        this.projectManager = mgr.getProjectManager();
        this.userManager = mgr.getUserManager();

        try {
            projectsUsers = mgr.getMembershipManager().getMemberships(this.projectKey);
        } catch (RedmineException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setRating(Float value) {
        this.neededPythonRating = value;
    }

    public void setJavaErrorsAmount(Integer value) {
        this.neededJavaErrorAmount = value;
    }

    public void setPerevod(Boolean value) {
        this.perevod = value;
    }

    public void setIssueStatus(Integer value) {
        this.issueStatus = value;
    }

    public void setProverka(String value) {
        this.proverka = value;
    }

    public void setStudentName(String value) {
        this.studentName = value;
    }

    public void setProfessorName(String value) {
        this.professorName = value;
    }

    public void setAssigneeName(String value) {

        this.assigneeName = value;

    }

    public void setIssueAssigneeName(Issue issue, String value) {
        int id = 0;
        for (Membership user : projectsUsers) {
            if (user.getUserName().equalsIgnoreCase(value)) {
                id = user.getUserId();
                break;
            }
        }
        issue.setAssigneeId(id);
        issue.setAssigneeName(value);
    }

    public Issue setIssueAssigneeNameForIssue(Issue issue, String value) {
        int id = 0;
        for (Membership user : projectsUsers) {
            if (user.getUserName().equalsIgnoreCase(value)) {
                id = user.getUserId();
                break;
            }
        }
        issue.setAssigneeId(id);
        issue.setAssigneeName(value);
        return issue;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void saveAttachment(Issue issue) throws IOException {
        Collection<Attachment> issueAttachment = issue.getAttachments();
        ArrayList<Attachment> issueAttachments = new ArrayList<>(issueAttachment);
        File dir = new File(".\\myFiles\\");
        dir.mkdirs();

        for (Attachment attach : issueAttachments) {

            if (attach.getFileName().endsWith(".py") || attach.getFileName().endsWith(".java") || attach.getFileName().endsWith(".cpp")) {

                if (checkAttachmentID(attach.getId()) == 0) {

                    String fileToManage = ".\\myFiles\\" + makeUsableFileName(attach.getFileName());
                    downloadAttachments(attach.getContentURL(),
                            apiAccessKey,
                            fileToManage);
                    
                    if (attach.getFileName().endsWith(".py")) {
                        new MyPylint().startPylint(makeUsableFileName(attach.getFileName()));
                        String attachName = ".\\myFiles\\" + makeUsableFileName(attach.getFileName()) + "_errorReport.txt";
                        String lastLineInReport = readLastLineInFile(attachName);
                        float studentPythonRating = this.pythonRatingCheck(lastLineInReport);

                        if (perevod == true && neededPythonRating <= studentPythonRating) {
                            System.out.println(neededPythonRating + " menshe " + studentPythonRating);
                            issue.setStatusId(issueStatus);
                            this.setIssueAssigneeName(issue, assigneeName);
                            this.uploadAttachment(issue, attachName);
                        }
                        if (neededPythonRating > studentPythonRating) {
                            String studentsName = getStudentsName(issue.getId(), this.professorName);
                            System.out.println("(assigning back to Student) - " + studentsName + " " + lastLineInReport);
                            this.setIssueAssigneeName(issue, studentsName);
                            lastLineInReport += "\n Some corrections are required! Keep trying!";
                            this.uploadAttachment(issue, attachName);
                        } else {
                            lastLineInReport += "\n " + generateSuccessMsg();
                            PyTaskChecker pyChecker = new PyTaskChecker(issue.getSubject(), fileToManage);
                            
                        }
                        issue.setNotes(lastLineInReport);
                        this.updateIssue(issue);
                    }

                    if (attach.getFileName().endsWith(".java")) {
                        new MyCheckStyle().startCheckStyle(attach.getFileName());
                        // this.uploadAttachment(issue, ".\\myFiles\\" + attach.getFileName() + "_errorReport.txt");
                        String lastLine = readLastLineInFile(".\\myFiles\\" + attach.getFileName() + "_errorReport.txt");
                        int studentJavaErrorAmount = this.javaErrorAmountDetectionInFile(lastLine);
                        System.out.println("we have:" + lastLine);
                        System.out.println("we have:" + studentJavaErrorAmount);
                        /*if (perevod == true) {
                            System.out.println("we need " + neededJavaErrorAmount + " but there are " + studentJavaErrorAmount);
                            issue.setStatusId(issueStatus);
                            issue.setAssigneeName(assigneeName);
                        }*/
                        //issue.setNotes(lastLine);
                        //this.updateIssue(issue);
                    }

                    if (attach.getFileName().endsWith(".cpp")) {
                        new MyCppLint().startCpplint(attach.getFileName());
                        this.uploadAttachment(issue, ".\\myFiles\\" + attach.getFileName() + "_errorReport.txt");
                        String lastLine = readLastLineInFile(".\\myFiles\\" + attach.getFileName() + "_errorReport.txt");
                        int studentCppErrorAmount = this.cppErrorAmountDetectionInFile(lastLine);
                        System.out.println("we have " + studentCppErrorAmount);
                        System.out.println("we have line " + lastLine);
                        issue.setNotes(lastLine);
                        this.updateIssue(issue);
                    }

                    // cleanDirectory(new File(".\\myFiles\\"));
                }
            } else {
                continue;
            }

        }
    }

    private void downloadAttachments(String url, String apikey, String fileName) throws MalformedURLException, IOException {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-redmine-api-key", apikey)
                .addHeader("cache-control", "no-cache") //не обязательно
                .build();

        Response response = client.newCall(request).execute();

        try (InputStream in = response.body().byteStream()) {
            Path to = Paths.get(fileName); //convert from String to Path
            Files.copy(in, to, StandardCopyOption.REPLACE_EXISTING);
        }

    }

    public List<Issue> getIssues() throws RedmineException {
        issues = issueManager.getIssues(projectKey, queryId, Include.journals, Include.attachments, Include.changesets);
        return issues;
    }

    public List<Issue> getIssues(String iterationName) throws RedmineException {
        //15306
        String iterationid = getIterationIdByName(iterationName);

        final Map<String, String> params = new HashMap<String, String>();
        params.put("project_id", projectKey);
        params.put("fixed_version_id", iterationid);
        params.put("limit", "100");
        params.put("include", "attachments, journals");
        issues = issueManager.getIssues(params).getResults();
        return issues;
    }

    private String getIterationIdByName(String iterationName) {
        String retVal = "";
        int projectKeyInt = 0;
        List<Version> allVersions = null;
        try {
            projectKeyInt = mgr.getProjectManager().getProjectByKey(projectKey).getId();
            allVersions = this.mgr.getProjectManager().getVersions(projectKeyInt);
        } catch (RedmineException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (Version version : allVersions) {
            if (version.getName().equals(iterationName)) {
                retVal = version.getId().toString();
                break;
            }
        }
        return retVal;
    }

    public Collection<Version> getVersions(String projectKey) throws RedmineException {
        Project project = projectManager.getProjectByKey(projectKey);
        int projectID = project.getId();
        versions = projectManager.getVersions(projectID);
        return versions;
    }

    public Issue getIssueByID(int issueID) throws RedmineException {
        Issue issue = issueManager.getIssueById(issueID, Include.journals);
        return issue;
    }

    /*public User getUser() throws RedmineException {
        User user = userManager.getCurrentUser();
        return user;
    }*/
    public void uploadAttachment(Issue issue, String path) {

        try {
            String filename = path;
            File file = new File(filename);
            attachmentManager.addAttachmentToIssue(issue.getId(), file, ContentType.TEXT_PLAIN.getMimeType());
        } catch (RedmineException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public int checkAttachmentID(Integer id) throws IOException {
        List<String> attachmentIDs = new ArrayList<String>();
        attachmentIDs = Files.readAllLines(Paths.get("AttachmentID.txt"), Charsets.UTF_8);
        int response = 0;
        int attachWasCheckedBefore = 1;
        int attachIsNew = 0;

        for (String attach : attachmentIDs) {
            if (attach.equals("")) {
                continue;
            }
            long idFromFile = Long.parseLong(attach);
            if (idFromFile == id) {
                response = attachWasCheckedBefore;
                break;
            } else {
                response = attachIsNew;
            }
        }
        if (response == attachIsNew) {
            String fromIntToString = Integer.toString(id) + "\r\n";
            Files.write(Paths.get("AttachmentID.txt"), fromIntToString.getBytes(), StandardOpenOption.APPEND);

        }
        return response;
    }

    private void removeDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File aFile : files) {
                    removeDirectory(aFile);
                }
            }
            dir.delete();
        } else {
            dir.delete();
        }
    }

    private void cleanDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File aFile : files) {
                    removeDirectory(aFile);
                }
            }
        }
    }

    private String readLastLineInFile(String fileDir) {
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(Paths.get(fileDir), Charsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        }
        String result = "";
        for (int i = lines.size() - 1; i >= 0; i--) {
            if (!lines.get(i).isEmpty()) {
                result = lines.get(i);
                break;
            }
        }
        if (result.indexOf('(') != -1) {
            result = result.substring(0, result.indexOf('('));
        }

        return result;
    }

    public Integer javaErrorAmountDetectionInFile(String string) {

        ArrayList<String> words = new ArrayList<>();
        if (!string.isEmpty()) {
            for (String retval : string.split(" ")) {
                words.add(retval);
            }
        }

        String neededNumber = words.get((words.size()) - 2);
        int errorAmount = Integer.parseInt(neededNumber);

        return errorAmount;
    }

    public Integer cppErrorAmountDetectionInFile(String string) {

        ArrayList<String> words = new ArrayList<>();
        if (!string.isEmpty()) {
            for (String retval : string.split(" ")) {
                words.add(retval);
            }
        }

        String neededNumber = words.get((words.size()) - 1);
        int errorAmount = Integer.parseInt(neededNumber);

        return errorAmount;
    }

    public Float pythonRatingCheck(String lastStringInReport) {
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
        System.out.println(rateValue);
        return rateValue;
    }

    public void updateIssue(Issue issue) {
        try {
            issueManager.update(issue);
        } catch (RedmineException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setVersionForCheck(String inputTargetVersion, Issue issue) {

        Version version = issue.getTargetVersion();
        Collection<Attachment> attach = issue.getAttachments();

        if (inputTargetVersion.equals("All")) {
            try {
                this.saveAttachment(issue);
            } catch (IOException ex) {
                Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (version == null) {
            System.out.println("Issue without target version");
        } else if (version.getName().equals(inputTargetVersion)) {
            try {
                this.saveAttachment(issue);
            } catch (IOException ex) {
                Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    ArrayList<String> getProjectUsers() {
        MembershipManager membershipManager = mgr.getMembershipManager();

        List<Role> roles;
        List<Membership> memberships = new ArrayList<>();
        try {
            memberships = membershipManager.getMemberships(projectKey);
        } catch (RedmineException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        }
        ArrayList<String> retArray = new ArrayList<>();
        for (Membership m : memberships) {
            retArray.add(m.getUserName());
        }
        return retArray;
    }

    ArrayList<String> getIterationTasks(String iteration) {
        List<Issue> issues = new ArrayList<>();
        try {
            issues = getIssues(iteration);
        } catch (RedmineException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        }
        ArrayList<String> retVal = new ArrayList<>();
        for (Issue issue : issues) {
            retVal.add(issue.getId() + ": " + issue.getSubject());
        }
        return retVal;
    }

    void copyAndAssignIssue(int issueId, String nameTo) {
        Issue currIssue = new Issue();
        try {
            currIssue = getIssueByID(issueId);
        } catch (RedmineException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Issue newIssue = new Issue();
        newIssue.setSubject(currIssue.getSubject());
        newIssue.setTargetVersion(currIssue.getTargetVersion());
        newIssue.setCategory(currIssue.getCategory());
        newIssue.setProjectId(currIssue.getProjectId());
        newIssue.setPrivateIssue(true);
        newIssue.setAssigneeName(nameTo);
        newIssue.setDescription(currIssue.getDescription());
        newIssue = setIssueAssigneeNameForIssue(newIssue, nameTo);
        try {
            mgr.getIssueManager().createIssue(newIssue);
        } catch (RedmineException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String getStudentsName(int id, String managerName) {
        String st = new RedmineJournalsReader(url, this.apiAccessKey).getStudentsName(id, managerName);
        return st;
    }

    private String getStudentsName(int id) {
        return getStudentsName(id, professorName);
    }

    private String generateSuccessMsg() {
        String succ = new PhrasesGenerator().getSuccessRandomPhrase();
        return succ + " Waiting for further checks.";
    }

    private String makeUsableFileName(String fileName) {
        //
        //
        //тут надо что-то сделать с этим файлом: убить кириллицу и пробелы
        //
        //
        fileName = fileName.toLowerCase();
        String retStr = "";
        retStr = retStr.trim().toLowerCase().replaceAll(" ", "");
        for (int i = 0; i < fileName.length(); i++) {
            if (fileName.charAt(i) > 'a' && fileName.charAt(i) < 'z' || fileName.charAt(i)=='.') {
                retStr+=fileName.charAt(i);
            }
        }
        if (retStr.equalsIgnoreCase(".py")){
            retStr = "task.py";
        }
        return retStr;
    }
}
