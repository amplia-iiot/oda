package es.amplia.oda.operation.update.internal;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.operation.update.DownloadManager;
import es.amplia.oda.operation.update.FileManager;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;
import static es.amplia.oda.operation.update.FileManager.FileException;

public class DownloadManagerImpl implements DownloadManager {

    static final String SOFTWARE_INSTALL_FOLDER = "deploy/";
    static final String CONFIGURATION_INSTALL_FOLDER = "configuration/";

    static final String API_KEY_HEADER = "X-ApiKey";
    static final String DOWNLOAD_FOLDER = "downloads/";

    private static final Logger logger = LoggerFactory.getLogger(DownloadManagerImpl.class);

    private final DeviceInfoProvider deviceInfoProvider;

    private final FileManager fileManager;

    private final Map<DeploymentElement, String> downloadedFiles = new HashMap<>();

    private String rulesPath;

    public DownloadManagerImpl(DeviceInfoProvider deviceInfoProvider, FileManager fileManager) {
        this.deviceInfoProvider = deviceInfoProvider;
        this.fileManager = fileManager;
    }

    public void createDownloadDirectory() throws DownloadException {
        if (!fileManager.exist(DOWNLOAD_FOLDER)) {
            try {
                fileManager.createDirectory(DOWNLOAD_FOLDER);
            } catch (FileException exception) {
                throw new DownloadException("Can not create download folder");
            }
        }
    }

    @Override
    public void download(DeploymentElement deploymentElement) throws DownloadException {
        String localFilePath = getDeploymentElementLocalFilePath(deploymentElement);
        String remoteUrl = deploymentElement.getDownloadUrl();

        HttpGet httpGet = new HttpGet(remoteUrl);
        httpGet.addHeader(API_KEY_HEADER, deviceInfoProvider.getApiKey());

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build();
             CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
             DataInputStream reader = new DataInputStream(httpResponse.getEntity().getContent());
             DataOutputStream writer = new DataOutputStream(new FileOutputStream(new File(localFilePath)))) {

            StatusLine statusLine = httpResponse.getStatusLine();
            int responseCode = statusLine.getStatusCode();
            if (responseCode != 200 && responseCode != 201) {
                throw new DownloadException(
                        String.format("HTTP error getting resource %s: %d, %s", remoteUrl, responseCode,
                                statusLine.getReasonPhrase()));
            }

            byte[] buffer = new byte[4096];
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }

            downloadedFiles.put(deploymentElement, localFilePath);
        } catch (IOException exception) {
            throw new DownloadException(String.format("Error downloading file %s to %s: %s", remoteUrl, localFilePath,
                    exception.getMessage()));
        }
    }

    private String getDeploymentElementLocalFilePath(DeploymentElement deploymentElement) {
        switch (deploymentElement.getPath()) {
            case SOFTWARE_INSTALL_FOLDER:
                return DOWNLOAD_FOLDER + deploymentElement.getName() + "-" + deploymentElement.getVersion() + ".jar";
            case CONFIGURATION_INSTALL_FOLDER:
                return DOWNLOAD_FOLDER + deploymentElement.getName() + ".cfg";
            default:
                if (deploymentElement.getPath().startsWith(rulesPath)) {
                    return DOWNLOAD_FOLDER + deploymentElement.getName() + ".js";
                }
                return DOWNLOAD_FOLDER + deploymentElement.getName();
        }
    }

    @Override
    public String getDownloadedFile(DeploymentElement deploymentElement) {
        if (downloadedFiles.containsKey(deploymentElement)) {
            return downloadedFiles.get(deploymentElement);
        }
        return null;
    }

    @Override
    public void deleteDownloadedFiles() {
        downloadedFiles.values().stream()
                .filter(Objects::nonNull)
                .forEach(downloadedFile -> {
                    try {
                        fileManager.delete(downloadedFile);
                    } catch (FileException exception) {
                        logger.warn("Can not delete downloaded file {}: {}", downloadedFile, exception.getMessage());
                    }
                });
        downloadedFiles.clear();
    }

    @Override
    public void loadConfig(String rulesPath) {
        this.rulesPath = rulesPath;
    }
}
