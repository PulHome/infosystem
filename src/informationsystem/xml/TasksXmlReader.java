package informationsystem.xml;

import informationsystem.tasksManager.TaskInfo;
import informationsystem.tasksManager.TasksKeeper;
import org.w3c.dom.*;
import tools.TextUtils;
import tools.Translit;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.List;
import java.util.stream.Collectors;

public class TasksXmlReader extends XmlReader {

    public TasksXmlReader(String xmlPath) {
        super(xmlPath);
    }

    public TasksKeeper getAllTests() {
        //HashMap<String, String> testsData = new HashMap<>();
        TaskInfo testData;
        TasksKeeper tasksKeeper = new TasksKeeper();

        try {
            Document doc = getDocument(filePath);
            //xpath for settings names
            XPathExpression subjectFromXml = xpath.compile(".//test");
            NodeList listOfTestNodes = (NodeList) subjectFromXml.evaluate(doc, XPathConstants.NODESET);

            //тут можно добавить более "умный" поиск, пока ищем только - полное совпадение
            for (int i = 0; i < listOfTestNodes.getLength(); i++) {
                testData = new TaskInfo();
                Element e = (Element) listOfTestNodes.item(i);
                testData.setTaskName(e.getAttribute("subject"));
                testData.setTaskPath(e.getAttribute("testSetFolder"));
                testData.setTaskId(e.getAttribute("id"));
                NodeList listOfDescr = e.getChildNodes();
                for (int j = 0; j < listOfDescr.getLength(); j++) {
                    Node node = (Node) listOfDescr.item(j);
                    if (node.getNodeName().equals("description")) {
                        testData.setTaskBody(node.getTextContent().trim());
                    }
                }
                testData.setIterationPath(
                        e.getParentNode().getAttributes().getNamedItem("name").getNodeValue());

                tasksKeeper.put(testData);
            }
        } catch (XPathExpressionException ex) {
            logger.info(ex.getMessage());
        }

        return tasksKeeper;
    }


    public void addIteration(String iterationName) {
        Element rootNode = doc.getDocumentElement();
        rootNode.normalize();
        Node node = doc.createTextNode("homework");
        ((Element) node).setAttribute("id", Translit.toTranslit(iterationName));
        rootNode.appendChild(node);
        try {
            saveXml(doc, filePath);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public void saveTask(TaskInfo selectedTask) {
        Element rootNode = doc.getDocumentElement();
        rootNode.normalize();

        Element foundElement = (Element) getTask(doc, selectedTask);

        //may be it already has a description
        String xPathString = String.format("./description");
        Element description = (Element) xPathEvaluate(foundElement, xPathString, xpath, XPathConstants.NODE);

        if (description != null) {
            Node newNode = description.getFirstChild();
            if (newNode != null) {
                newNode.setTextContent(selectedTask.getTaskBody());
                description.replaceChild(description.getFirstChild(), newNode);
            } else if (!TextUtils.isNullOrEmpty(selectedTask.getTaskBody())) {
                description.appendChild(
                        description.getOwnerDocument().createCDATASection(selectedTask.getTaskBody()));
            }
        } else {
            if (foundElement.getAttribute("subject").equalsIgnoreCase(selectedTask.getTaskName())) {
                Element newDescription = doc.createElement("description");
                CDATASection newDescriptionCdata = doc.createCDATASection(selectedTask.getTaskBody());
                newDescription.appendChild(newDescriptionCdata);
                foundElement.appendChild(foundElement.getOwnerDocument().importNode(newDescription, true));
            }
        }

        //set new subject
        foundElement.setAttribute("subject", selectedTask.getTaskName());
        if (!TextUtils.isNullOrEmpty(selectedTask.getTaskPath())) {
            foundElement.setAttribute("testSetFolder", selectedTask.getTaskPath());
        }

        try {
            saveXml(doc, filePath);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public boolean exists(TaskInfo task) {
        return getTask(doc, task) != null;
    }

    public void addTask(TaskInfo selectedTask) {
        try {
            Element rootNode = doc.getDocumentElement();
            rootNode.normalize();

            Element nodeForAnewTask = (Element) chooseNodeForANewTask(selectedTask, xpath, doc, rootNode);

            Element taskNode = doc.createElement("test");
            String taskId = !TextUtils.isNullOrEmpty(selectedTask.getTaskId()) ? selectedTask.getTaskId() :
                    getNextId(rootNode);
            selectedTask.setTaskId(taskId);
            taskNode.setAttribute("id", taskId);
            taskNode.setAttribute("testSetFolder", selectedTask.getTaskPath());
            taskNode.setAttribute("subject", selectedTask.getTaskName());
            Element newDescription = doc.createElement("description");
            CDATASection newDescriptionCdata = doc.createCDATASection(selectedTask.getTaskBody());
            newDescription.appendChild(newDescriptionCdata);
            taskNode.appendChild(newDescription);
            nodeForAnewTask.appendChild(taskNode);
            saveXml(doc, filePath);
        } catch (TransformerException | XPathExpressionException e) {
            logger.info(e.getMessage());
        }
    }

    public List<String> getTasksByIteration(String selected) {
        return getAllTests().getAllTasks().stream()
                .filter(task -> task.getIterationPath().equals(selected))
                .map(task -> task.getTaskName())
                .collect(Collectors.toList());
    }


    private Node getTask(Document doc, TaskInfo selectedTask) {
        Element foundElement = null;

        Element rootNode = doc.getDocumentElement();
        rootNode.normalize();

        //find the test Node
        String xPathStringElem = String.format(".//test[@id=\"%s\"]", selectedTask.getTaskId());
        foundElement = (Element) xPathEvaluate(rootNode, xPathStringElem, xpath, XPathConstants.NODE);
        if (foundElement == null) {
            xPathStringElem = String.format(".//test[@subject=\"%s\"]", selectedTask.getTaskName());
            foundElement = (Element) xPathEvaluate(rootNode, xPathStringElem, xpath, XPathConstants.NODE);
        }
        if (foundElement == null) {
            xPathStringElem = String.format(".//test[@subject=\"%s\"]", Translit.toTranslit(selectedTask.getTaskName()));
            foundElement = (Element) xPathEvaluate(rootNode, xPathStringElem, xpath, XPathConstants.NODE);
        }

        return foundElement;
    }

    private String getNextId(Element rootNode) {
        String xPathStringElem = ".//homework/test/@id";
        NodeList ids =
                (NodeList) xPathEvaluate(rootNode, xPathStringElem, xpath, XPathConstants.NODESET);
        int max = 0;
        for (int i = 0; i < ids.getLength(); i++) {
            if (Integer.parseInt(ids.item(i).getNodeValue()) > max) {
                max = Integer.parseInt(ids.item(i).getNodeValue());
            }
        }
        return String.valueOf(max + 1);
    }

    private Node chooseNodeForANewTask(TaskInfo selectedTask, XPath xpath, Document doc, Element rootNode) throws XPathExpressionException {
        Element pathforAnewTask = null;
        if (selectedTask.getIterationPath() != null || !selectedTask.getIterationPath().equals("")) {
            String xPathStringElem = String.format(".//homework[@name=\"%s\"]", selectedTask.getIterationPath());
            pathforAnewTask = (Element) xPathEvaluate(rootNode, xPathStringElem, xpath, XPathConstants.NODE);
            if (pathforAnewTask == null) {
                xPathStringElem = String.format(".//homework[@name=\"%s\"]", Translit.toTranslit(selectedTask.getIterationPath()));
                pathforAnewTask = (Element) xPathEvaluate(rootNode, xPathStringElem, xpath, XPathConstants.NODE);
            }

            if (pathforAnewTask == null) {
                pathforAnewTask = doc.createElement("homework");
                pathforAnewTask.setAttribute("name", selectedTask.getIterationPath());
                rootNode.appendChild(pathforAnewTask);
            }
        } else {
            String xPathStringElem = String.format(".//homework[@name=\"%s\"]", "");
            pathforAnewTask = (Element) xPathEvaluate(rootNode, xPathStringElem, xpath, XPathConstants.NODE);
            if (pathforAnewTask == null) {
                pathforAnewTask = doc.createElement("homework");
                pathforAnewTask.setAttribute("name", "");
                rootNode.appendChild(pathforAnewTask);
            }
        }
        return pathforAnewTask;
    }


}
