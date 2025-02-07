package es.amplia.oda.hardware.ftp;

import java.io.IOException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SftpClient {

    private final String server;
    private final int port;
    private final String user;
    private final String password;
    private final String privateKeyPath;

    private Session session;
    private ChannelSftp channel;

    public static final int OVERWRITE = ChannelSftp.OVERWRITE;
    public static final int RESUME = ChannelSftp.RESUME;
    public static final int APPEND = ChannelSftp.APPEND;

    public SftpClient (String server, String user, String password, String privateKeyPath) {
        this(server, 22, user, password, privateKeyPath);
    }

    public SftpClient (String server, int port, String user, String password, String privateKeyPath) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
        this.privateKeyPath = privateKeyPath;
    }

    public void connect() throws Exception {
        connect(10000);
    }
    public void connect(int timeout) throws Exception {
        JSch jsch = new JSch();
        if (privateKeyPath != null && !privateKeyPath.equals(""))
            jsch.addIdentity(privateKeyPath);

        session = jsch.getSession(user, server, port);
        if (password != null && !password.equals(""))
            session.setPassword(password);

        session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(timeout);
        session.connect();
        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
    }

    public void disconnect() throws IOException {
        session.disconnect();
        channel.exit();
    }

    public void downloadFile(String source, String destination) throws Exception {
        channel.get(source, destination);
    }

    public void uploadFile(String source, String destination) throws Exception {
        channel.put(source, destination);
    }

    public void uploadFile(String source, String destination, int mode) throws Exception {
        channel.put(source, destination, null, mode);
    }

    public void moveFile(String oldPath, String newPath) throws Exception {
        channel.rename(oldPath, newPath);
    }

}
