package redmineManagement;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.*;
import informationsystem.FXMLDocumentController;
import informationsystem.RedmineConnectionProperties;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class DownloaderTodosIssuesFromAllAvailableProjects {
    public static String[] PulsProjects = {
            "pvk_2013-2014_2",
            "2018-2019",
            "team-2018-1",
            "team-2018-2",
            "pvk_319_programming_course_21_22",
            "pvk_319_programming_ap_21",
            "pvk_319_programming_ct_22_23",
            "pvk_319_programming_oop_21_22",
            "pvk_319_programming_rpo_21_22",
            "pvk_319_programming_tp_21",
            "bit-2-006",
            "bit-2-007",
            "bit-2-021",
            "bit",
            "bit-251",
            "team-6",
            "team-5",
            "bit-252",
            "team-1",
            "team-2",
            "team-7",
            "calendar-v1",
            "team-8",
            "bit-269",
            "bit233",
            "group-1",
            "group-3",
            "m3o-121m-21-oppps",
            "pvk-m-031",
            "pvk-m-121-122",
            "fifthth_grade_2020",
            "pvk-m-2018_2019",
            "pvk-m-221-222-19",
            "python-quest-2018-2019",
            "t40-105bk-16",
            "t40-105bk-16-tp",
            "vti-232",
            "mai-vti319-oop-2020-2021",
            "mai-vti319-oop-2021-2022",
            "mai-vti319-18-2019-2020",
            "mai-319-vti235-2020-2021",
            "m3o-235b-20-razrabotka-po",
            "m3o-235b-20-razrabotka-po-team1",
            "m3o-235b-20-razrabotka-po-team2",
            "m3o-235b-20-razrabotka-po-team3",
            "pvk-m_opps_2021",
            "mai-bit319-16-2019-2020",
    };

    public static String[] PulsProjects2 = {
            "team-8",
            "bit-269",
            "vti-268",
            "m3o-121m-21-oppps",
            "pvk-m-031",
            "pvk-m-121-122",
            "fifthth_grade_2020",
            "pvk-m-2020_2021",
            "pvk-m-2021_2022",
            "pvk_319_programming_rpo_22",
            "pvk-m-2022_2023",
            "pvk-m-2018_2019",
            "pvk-m-221-222-19",
            "python-quest-2018-2019",
            "vti-232",
            "vti-250",
            "vti-4-268",
            "vti-106",
            "mai-vti319-oop-2020-2021",
            "mai-vti319-oop-2021-2022",
            "mai-vti319-18-2019-2020",
            "mai-319-vti235-2020-2021",
            "m3o-235b-20-razrabotka-po",
            "m3o-235b-20-razrabotka-team1",
            "pvk-m_opps_2021",
            "mai-bit319-16-2019-2020"
    };
    private static RedmineConnectionProperties testProps;
    private static String myFilesDir = ".\\myFiles2\\";

    static {
        testProps = new RedmineConnectionProperties();
        testProps.url = "https://hostedredmine.com";
        testProps.projectKey = "bit-269";
        testProps.apiAccessKey = "7f566bb8c0027ec71d0db820b139db2f8f3525e2";
    }

    private ConnectionWithRedmine connectionToRedmine;
    private RedmineAlternativeReader redmineAlternativeReader;
    private RedmineConnectionProperties props;
    private String professorName = "Ekaterina Politsyna";

    public DownloaderTodosIssuesFromAllAvailableProjects(RedmineConnectionProperties props) {
        this.props = props;
        connectionToRedmine = new ConnectionWithRedmine(props.apiAccessKey, props.projectKey, props.url);
        redmineAlternativeReader = new RedmineAlternativeReader(props.url, props.apiAccessKey);
    }

    public ConnectionWithRedmine getConnection() {
        return connectionToRedmine;
    }

    private boolean isKnownAttachExtention(String attachName) {
        return attachName.endsWith(".py") || attachName.endsWith(".java")
                || attachName.endsWith(".zip") || attachName.endsWith(".cpp");
    }

    public static void main(String[] args) throws RedmineException {
        DownloaderTodosIssuesFromAllAvailableProjects checker = new DownloaderTodosIssuesFromAllAvailableProjects(testProps);
        ConnectionWithRedmine connect = checker.getConnection();

//       Получение всех проектов, доступных пользователю.
//                User user = connect.getRedmineManager().getUserManager().getCurrentUser();
//        List<Project> projects = connect.getRedmineManager().getProjectManager().getProjects();

        /*for (Project curPrj : projects) {
            connect.getProjectManager().getProjectMembers(curPrj.getIdentifier()).stream().filter(m ->
                            m.getUserName() != null ?
                                    m.getUserName().equalsIgnoreCase(checker.professorName) : false)
                    .forEach(m -> System.out.println(curPrj.getIdentifier() + m.getUserName()));
        }*/
        for (String projectId : PulsProjects) {
            System.out.println(projectId);
            checker.downloadAllKnownAttachments(projectId);
        }
        System.out.println("Finished");
    }

    private void downloadAllKnownAttachments(String projectId) {
        connectionToRedmine.setProjectKey(projectId);

        for (String version : connectionToRedmine.getVersions()) {
            for (Issue issue : connectionToRedmine.getClosedIssues(version)) {
                Collection<Attachment> issueAttachment = issue.getAttachments();
                List<Attachment> issueAttachments = new ArrayList<Attachment>(issueAttachment);
                new File(myFilesDir).mkdirs();

                //Check only latest attach, the rest are already history
                Optional<Attachment> nullableAttach = connectionToRedmine.getLatestCheckableAttach(issueAttachments);
                Attachment attach = null;
                if (!nullableAttach.isPresent()) {
                    continue;
                }

                attach = nullableAttach.get();
                String fileToManage = myFilesDir + connectionToRedmine.makeUsableFileName(
                        attach.getFileName(),
                        attach.getAuthor().getFullName(),
                        issue.getSubject());
                try {
                    connectionToRedmine.
                            downloadAttachments(attach.getContentURL(), testProps.apiAccessKey, fileToManage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static Logger logger = Logger.getLogger(FXMLDocumentController.class.getSimpleName());
}
