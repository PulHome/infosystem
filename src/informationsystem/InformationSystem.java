package informationsystem;

import informationsystem.xml.OnlineXmlReader;
import informationsystem.xml.SettingsXmlReader;
import informationsystem.xml.XmlReader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import tools.TextUtils;
import tools.ZipFile;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Zerg0s
 */
public class InformationSystem extends Application {
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("./FXMLDocument.fxml"));
            Parent root = loader.load();
            FXMLDocumentController controller = loader.getController();
            loader.setRoot(this);
            loader.setClassLoader(getClass().getClassLoader());

            Scene scene = new Scene(root);
            stage.setTitle("Проверка задач в системе Redmine");
            stage.setScene(scene);
            stage.setOnHidden(e -> controller.shutdown());
            stage.getIcons().add(new Image(Objects.requireNonNull(InformationSystem.class.getResourceAsStream("robot.png"))));
            stage.show();
            String oldVersion = ((Text) root.lookup("#versionName"))
                    .getText().replaceAll("[^\\d.]", "");
            // new Thread(() -> checkNewVersion(oldVersion)).start();
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(InformationSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int getUploadId(String readXml) {
        if (TextUtils.isNullOrEmpty(readXml)) {
            return 0;
        }
        try {
            String idString = XmlReader.getTextTagValue("uploadId", XmlReader.loadXMLFromString(readXml));
            if (!TextUtils.isNullOrEmpty(idString)) {
                return Integer.parseInt(idString);
            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            logger.log(Level.FINE, e.getMessage());
        }
        return 0;
    }

    private void saveTestUploadStatus(int uploadId) {
        final String settingsXml = "ProjectKey.xml";
        SettingsXmlReader reader = new SettingsXmlReader(settingsXml);
        reader.saveSettings(settingsXml, new HashMap<>() {{
            put("UploadId", String.valueOf(uploadId));
        }});
    }

    private int getOldUploadId(String settingsXml) {
        try {
            Document xmlFromFile = XmlReader.loadXMLFromString(Files.readString(Path.of(settingsXml)));
            String idString = XmlReader.getTextTagValue("UploadId",
                    xmlFromFile);
            return Integer.parseInt(idString);
        } catch (NumberFormatException | IOException | SAXException | ParserConfigurationException ex) {
            //ex.printStackTrace();
        }
        return 0;
    }

    private boolean uploadTests(String whereToUpload, String version) {
        String charset = "UTF-8";
        String param = "value";
        File textFile = new File("TestsInfo_v2.xml");
        File binaryFile = new File("tests.zip");
        String boundary = Long.toHexString(System.currentTimeMillis()); //just need a unique value
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.

        URLConnection connection = null;
        try {
            connection = new URL(whereToUpload).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        } catch (IOException e) {

        }

        if (connection == null) {
            return false;
        }

        try (
                OutputStream output = Objects.requireNonNull(connection).getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
        ) {
            // Send normal param.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
            writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
            writer.append(CRLF).append(param).append(CRLF).flush();

            // Send xml file - testsInfo.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"TestsInfo\"; filename=\"" + getFileNameWithOwner(textFile.getName(), version) + "\"").append(CRLF);
            writer.append("Content-Type: text/xml; charset=" + charset).append(CRLF);
            writer.append(CRLF).flush();
            Files.copy(textFile.toPath(), output);
            output.flush();
            writer.append(CRLF).flush();

            // Send binary file - tests.zip.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"testsZip\"; filename=\"" + getFileNameWithOwner(binaryFile.getName(), version) + "\"").append(CRLF);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();
            Files.copy(binaryFile.toPath(), output);
            output.flush();
            writer.append(CRLF).flush();

            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF).flush();
            // Request is lazily fired whenever you need to obtain information about response.
            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            /* Print Server's response for debug reasons
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    ((HttpURLConnection) connection).getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
            in.close();
            */
            return responseCode == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String getFileNameWithOwner(String name, String version) {
        return String.format("%s-%s-%s", System.getProperty("user.name"), version, name);
    }

    private boolean checkUploadIsRequired(int uploadId, int old) {
        return uploadId > old;
    }

    private Iterable<String> updateUpdatersList(String[] updatersList) {
        List<String> sources = new ArrayList<>();
        try {
            sources = Files.readAllLines(Path.of("updater.dat"));
        } catch (IOException e) {
            //e.printStackTrace();
        }

        List<String> updateSources = Stream.concat(Arrays.stream(updatersList).sequential(),
                sources.stream()).collect(Collectors.toList());
        return Stream.concat(updateSources.stream(),
                        updateSources.stream().map((s -> s.endsWith("/") ? s.concat("version.xml")
                                : s.concat("/version.xml"))))
                .distinct()
                .collect(Collectors.toList());
    }

    private String downloadOnlineXml(String path) {
        try (InputStream in = new URL(path).openStream()) {
            byte[] bytes = in.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            //e.printStackTrace(); //not intended to be used.
            return "";
        }
    }

    private String getNewVersionDescription(String readXml) {
        try {
            List<String> descriptions = OnlineXmlReader.getDescription(readXml, "description", "point");
            return String.join("", descriptions);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) {
        System.setProperty("java.net.useSystemProxies", "true");
        // открытие сцены
        launch(args);
    }

    private static Logger logger = Logger.getLogger(FXMLDocumentController.class.getSimpleName());
}
