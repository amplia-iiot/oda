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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;
import static es.amplia.oda.operation.update.FileManager.FileException;

public class DownloadManagerImpl implements DownloadManager {

    static final String API_KEY_HEADER = "X-ApiKey";

    private static final Logger logger = LoggerFactory.getLogger(DownloadManagerImpl.class);

    private final DeviceInfoProvider deviceInfoProvider;

    private final FileManager fileManager;

    private final Map<DeploymentElement, String> downloadedFiles = new HashMap<>();

    private String rulesPath;
    private String rulesUtilsPath;
    private String deployPath= "deploy/";
    private String configurationPath= "configuration/";
    private String downloadsPath= "downloads/";

    public DownloadManagerImpl(DeviceInfoProvider deviceInfoProvider, FileManager fileManager) {
        this.deviceInfoProvider = deviceInfoProvider;
        this.fileManager = fileManager;
    }

    public void createDownloadDirectory() throws DownloadException {
        if (!fileManager.exist(downloadsPath)) {
            try {
                fileManager.createDirectory(downloadsPath);
            } catch (FileException exception) {
                throw new DownloadException("Can not create download folder");
            }
        }
    }

    @Override
    public void download(DeploymentElement deploymentElement) throws DownloadException {
        String localFilePath = getDeploymentElementLocalFilePath(deploymentElement);
        String remoteUrl = deploymentElement.getDownloadUrl();

        if (remoteUrl.startsWith("http://")) remoteUrl = remoteUrl.replace("http://", "https://");

        HttpGet httpGet = new HttpGet(remoteUrl);
        httpGet.addHeader(API_KEY_HEADER, deviceInfoProvider.getApiKey());

        // For adding SSL No Verification
        TrustManager trm = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {

            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        };

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { trm }, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }


        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setSSLContext(sslContext).build();
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

            logger.debug("File downloaded " + localFilePath);
            downloadedFiles.put(deploymentElement, localFilePath);
        } catch (IOException exception) {
            throw new DownloadException(String.format("Error downloading file %s to %s: %s", remoteUrl, localFilePath,
                    exception.getMessage()));
        }
    }

    private String getDeploymentElementLocalFilePath(DeploymentElement deploymentElement) {
        if (deploymentElement.getPath().equals(deployPath)) {
            return downloadsPath + deploymentElement.getName() + "-" + deploymentElement.getVersion() + ".jar";
        } else if (deploymentElement.getPath().equals(configurationPath)) {
            return downloadsPath + deploymentElement.getName() + ".cfg";
        } else {
            if (deploymentElement.getPath().startsWith(rulesPath)
                    || deploymentElement.getPath().startsWith(rulesUtilsPath)) {
                return downloadsPath + deploymentElement.getName() + ".js";
            }
            return downloadsPath + deploymentElement.getName();
        }
    }

    @Override
    public String getDownloadedFile(DeploymentElement deploymentElement) {
        if (downloadedFiles.containsKey(deploymentElement)) {
            return downloadedFiles.get(deploymentElement);
        }
        logger.warn("No file found for deployment element " + deploymentElement.getDownloadUrl());
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
    public void loadConfig(String rulesPath, String rulesUtilsPath, String deployPath, String configurationPath,
                           String downloadsPath) {
        this.rulesPath = rulesPath;
        this.rulesUtilsPath = rulesUtilsPath;
        this.deployPath = deployPath;
        this.configurationPath = configurationPath;
        this.downloadsPath = downloadsPath;
    }
}
