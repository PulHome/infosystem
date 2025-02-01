package informationsystem.xml;

import data.LintReportMode;
import data.Project;
import data.ProjectOwner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsXmlReader extends XmlReader {
    private String userName = "";
    private String apiKey = "";
    private String projectKey = "";
    private ArrayList<String> usersNameList = new ArrayList<String>();
    private ArrayList<String> apiKeysList = new ArrayList<String>();
    private ArrayList<String> projectNameList = new ArrayList<String>();
    private ArrayList<ProjectOwner> owners = new ArrayList<ProjectOwner>();
    private List<String> urls = new ArrayList<>();

    private String currentRedmineUrl;
    private boolean isEasyMode;
    private boolean checkAll;


    private String selectedSupervisor = "";
    private Project selectedProject;
    private String selectedVersion = "";
    private LintReportMode selectedLintMode;


    public SettingsXmlReader(String xmlPath) {
        super(xmlPath);
    }


    public LintReportMode getSelectedLintMode() {
        return selectedLintMode;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the apiKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @return the projectKey
     */
    public String getProjectKey() {
        return projectKey;
    }

    /**
     * @return the usersNameList
     */
    public ArrayList<String> getUsersNameList() {
        return usersNameList;
    }

    /**
     * @param usersNameList the usersNameList to set
     */
    public void setUsersNameList(ArrayList<String> usersNameList) {
        this.usersNameList = usersNameList;
    }

    /**
     * @return the projectNameList
     */
    public ArrayList<String> getProjectNameList(String ownerName) {
        projectNameList.clear();
        for (ProjectOwner owner : this.owners) {
            if (owner.getName().equals(ownerName)) {
                for (Project project : owner.getHisProjects()) {
                    projectNameList.add(project.getProjectName());
                }
            }
        }
        return projectNameList;
    }

    public String getSelectedSupervisor() {
        return selectedSupervisor;
    }

    /**
     * @param projectNameList the projectNameList to set
     */
    public void setProjectNameList(ArrayList<String> projectNameList) {
        this.projectNameList = projectNameList;
    }

    /**
     * @return the apiKeysList
     */
    public ArrayList<String> getApiKeysList() {
        return apiKeysList;
    }

    /**
     * @param apiKeysList the apiKeysList to set
     */
    public void setApiKeysList(ArrayList<String> apiKeysList) {
        this.apiKeysList = apiKeysList;
    }

    /**
     * @return the owners
     */
    public ArrayList<ProjectOwner> getOwners() {
        return owners;
    }

    /**
     * @param owners the owners to set
     */
    public void setOwners(ArrayList<ProjectOwner> owners) {
        this.owners = owners;
    }

    public boolean isEasyMode() {
        return isEasyMode;
    }

    public boolean needCheckAllIterations() {
        return checkAll;
    }

    public Project getSelectedProject() {
        return selectedProject;
    }

    public String getSelectedVersion() {
        return selectedVersion;
    }


    public void setUserName(String name) {
        this.userName = name;
    }

    public void setApiKey(String apikey) {
        this.apiKey = apikey;
    }

    public void setProjectKey(String projectkey) {
        this.projectKey = projectkey;
    }

    public void readConfigXML(String filePath) {
        try {
            Document configDoc = getDocument(filePath);

            //xpath for getting names
            XPathExpression exprOwners = xpath.compile(".//redmine[@url='" + currentRedmineUrl + "']//supervisor/@name");
            NodeList nl = (NodeList) exprOwners.evaluate(configDoc, XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                String owner = nl.item(i).getTextContent();
                getOwners().add(new ProjectOwner(owner));
                //get all project by supervisor
                XPathExpression exprProjects = xpath.compile(".//redmine[@url='" + currentRedmineUrl + "']//supervisor[@name='" + owner + "']/project");
                NodeList hisProjects = (NodeList) exprProjects.evaluate(configDoc, XPathConstants.NODESET);
                for (int j = 0; j < hisProjects.getLength(); j++) {
                    Element e = (Element) hisProjects.item(j);
                    String name = e.getAttribute("name");
                    String id = e.getAttribute("id");
                    getOwners().get(i).getHisProjects().add(new Project(name, id));
                }

                //add apikey
                XPathExpression exprApiKey = xpath.compile(".//redmine[@url='" + currentRedmineUrl + "']//supervisor[@name='" + owner + "']/apiKey");
                NodeList hisApikey = (NodeList) exprApiKey.evaluate(configDoc, XPathConstants.NODESET);
                getOwners().get(i).setApiKey(hisApikey.item(0).getTextContent());
            }

            XPathExpression isEasyModeXpath = xpath.compile(".//isEasyMode");
            Node easyModeNode = (Node) isEasyModeXpath.evaluate(configDoc, XPathConstants.NODE);
            if (easyModeNode != null) {
                isEasyMode = Boolean.parseBoolean(easyModeNode.getTextContent());
            }

            XPathExpression isCheckAllXpath = xpath.compile(".//checkAllIterations");
            Node isCheckAllNode = (Node) isCheckAllXpath.evaluate(configDoc, XPathConstants.NODE);
            if (isCheckAllNode != null) {
                checkAll = Boolean.parseBoolean(isCheckAllNode.getTextContent());
            }

            XPathExpression selectedSupervisorXpath = xpath.compile(".//SelectedSupervisor");
            Node selectedSupervisorNode = (Node) selectedSupervisorXpath.evaluate(configDoc, XPathConstants.NODE);
            if (selectedSupervisorNode != null) {
                selectedSupervisor = selectedSupervisorNode.getTextContent();
            }

            XPathExpression selectedProjectXPath = xpath.compile(".//SelectedProjectName");
            XPathExpression selectedProjectIdXPath = xpath.compile(".//SelectedProjectId");
            Node selectedProjectNode = (Node) selectedProjectXPath.evaluate(configDoc, XPathConstants.NODE);
            Node selectedProjectIdNode = (Node) selectedProjectIdXPath.evaluate(configDoc, XPathConstants.NODE);
            if (selectedProjectNode != null) {
                selectedProject = new Project(selectedProjectNode.getTextContent(), selectedProjectIdNode.getTextContent());
            }

            Node selectedVersionNode = (Node) xpath.compile(".//SelectedIteration").evaluate(configDoc, XPathConstants.NODE);
            if (selectedVersionNode != null) {
                selectedVersion = selectedVersionNode.getTextContent();
            }

            XPathExpression selectedLintModeXpath = xpath.compile(".//SelectedLintMode");
            Node selectedLintModeNode = (Node) selectedLintModeXpath.evaluate(configDoc, XPathConstants.NODE);
            if (selectedLintModeNode != null) {
                selectedLintMode = LintReportMode.valueOf(selectedLintModeNode.getTextContent());
            }

        } catch (XPathExpressionException ex) {
            logger.info(ex.getMessage());
        }
    }

    public void saveSettings(String xmlPath, Map<String, String> settingsToSave) {
        try {
            Document doc = getDocument(xmlPath);
            Element settingsNode = doc.getDocumentElement();
            settingsNode.normalize();
            for (Map.Entry m : settingsToSave.entrySet()) {
                if (settingsNode.getElementsByTagName((String) m.getKey()).getLength() == 0) {
                    Element aSetting = doc.createElement((String) m.getKey());
                    aSetting.appendChild(doc.createTextNode((String) m.getValue()));
                    settingsNode.appendChild(aSetting);
                } else {
                    Node el = settingsNode.getElementsByTagName((String) m.getKey()).item(0);
                    el.setTextContent((String) m.getValue());
                }
            }
            //save xml
            saveXml(doc, xmlPath);

        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }


    public List<String> getUrls() {
        return urls;
    }

    public String getCurrentRedmineUrl() {
        return currentRedmineUrl;
    }

    public void setCurrentRedmineUrl(String currentRedmineUrl) {
        this.currentRedmineUrl = currentRedmineUrl;
    }

    public void readUrls(String projectKeyXml) {
        try {
            Document configDoc = getDocument(filePath);

            //xpath for getting urls
            XPathExpression exprORedmineUrls = xpath.compile(".//redmine/@url");
            NodeList nlUrls = (NodeList) exprORedmineUrls.evaluate(configDoc, XPathConstants.NODESET);
            for (int i = 0; i < nlUrls.getLength(); i++) {
                urls.add(nlUrls.item(i).getTextContent());
            }
        } catch (XPathExpressionException ex) {
            logger.severe(ex.getMessage());
        }
    }
}
