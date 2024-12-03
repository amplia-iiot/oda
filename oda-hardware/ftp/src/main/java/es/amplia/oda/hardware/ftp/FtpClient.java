package es.amplia.oda.hardware.ftp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FtpClient {

    private final String server;
    private final int port;
    private final String user;
    private final String password;
    private final boolean passiveMode;
    private FTPClient ftpClient;

    public FtpClient(String server, int port, String user, String password, boolean passiveMode) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
        this.passiveMode = passiveMode;
    }

    public boolean connect() throws IOException {
        ftpClient = new FTPClient();

        // add this to redirect ftp commands and answers to log
        ftpClient.addProtocolCommandListener(new ProtocolCommandListener() {
            @Override
            public void protocolCommandSent(ProtocolCommandEvent protocolCommandEvent) {
                log.trace("{} - {}", protocolCommandEvent.getCommand(), protocolCommandEvent.getMessage());
            }

            @Override
            public void protocolReplyReceived(ProtocolCommandEvent protocolCommandEvent) {
                log.trace("Reply received : {}", protocolCommandEvent.getMessage());
            }
        });

        // configuration for ftp server to be unix
        ftpClient.configure(new FTPClientConfig(FTPClientConfig.SYST_UNIX));

        // connect to ftp
        ftpClient.connect(server, port);
        if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            ftpClient.disconnect();
            log.error("Error connecting to FTP server {}", this.server);
            return false;
        }

        // login in ftp server
        if(!ftpClient.login(user, password)){
            log.error("Error logging to FTP server {}", this.server);
            return false;
        }

        // set file type for downloads (in some servers must be done after logging)
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        if(this.passiveMode) {
            ftpClient.enterLocalPassiveMode();
        }

        return true;
    }

    public void disconnect() throws IOException {
        ftpClient.disconnect();
    }

    public List<FtpFile> listFiles(String path) throws IOException {
        FTPFile[] files = ftpClient.listFiles(path);
        return Arrays.stream(files)
                .map(this::mapFtpFile)
                .collect(Collectors.toList());
    }

    public void changeDir(String path) throws IOException {
        boolean result = ftpClient.changeWorkingDirectory(path);
        if (!result) {
            log.error("Error changing working directory in Ftp server");
        }
    }

    public void downloadFileKeepDate(FtpFile source, String destination) throws IOException {
        FileOutputStream out = new FileOutputStream(destination);
        ftpClient.retrieveFile(source.getName(), out);
        out.close();

        // update timestamp of the downloaded file to math the one in the server
        new File(destination).setLastModified(source.getTimestamp());
    }

    public void downloadFile(String source, String destination) throws IOException {
        FileOutputStream out = new FileOutputStream(destination);
        ftpClient.retrieveFile(source, out);
        out.close();
    }

    public void uploadFile(String source, String destination) throws IOException {
        FileInputStream in = new FileInputStream(source);
        ftpClient.storeFile(destination, in);
        in.close();
    }

    private FtpFile mapFtpFile(FTPFile fileFromFtpServer) {
        FtpFile newFtpFile = new FtpFile();
        newFtpFile.setName(fileFromFtpServer.getName());
        newFtpFile.setTimestamp(fileFromFtpServer.getTimestamp().getTimeInMillis());
        return newFtpFile;
    }
}
