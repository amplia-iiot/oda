package es.amplia.oda.hardware.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FtpClient.class)
public class FtpClientTest {

    private static final String FTP_IP = "127.0.0.1";
    private static final int FTP_PORT = 21;
    private static final String FTP_USER = "test";
    private static final String FTP_PASSWD = "passwd";

    @Mock
    FTPClient mockedApacheFtpClient;
    FtpClient ftpClient = new FtpClient(FTP_IP, FTP_PORT, FTP_USER, FTP_PASSWD, false);

    @Before()
    public void setup() throws Exception {
        PowerMockito.whenNew(FTPClient.class).withAnyArguments().thenReturn(mockedApacheFtpClient);
        Mockito.when(mockedApacheFtpClient.getReplyCode()).thenReturn(200);
        Mockito.when(mockedApacheFtpClient.login(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        boolean connectResult = ftpClient.connect();
        Assert.assertTrue(connectResult);

        Path destPath = Paths.get("src/test/resources/downloadedTest.txt");
        if(Files.exists(destPath)) {
            Files.delete(destPath);
        }
    }


    @Test
    public void testListFiles() throws IOException {

        FTPFile[] ftpFilesExpected = new FTPFile[1];
        ftpFilesExpected[0] = new FTPFile();
        ftpFilesExpected[0].setName("testFile");
        Calendar calendar = Calendar.getInstance();
        ftpFilesExpected[0].setTimestamp(calendar);

        Mockito.when(mockedApacheFtpClient.listFiles(Mockito.anyString())).thenReturn(ftpFilesExpected);

        List<FtpFile> ftpFilesReturned = ftpClient.listFiles("");

        Assert.assertEquals(ftpFilesExpected.length, ftpFilesReturned.size());
        Assert.assertEquals("testFile", ftpFilesReturned.get(0).getName());
        Assert.assertEquals(calendar.getTimeInMillis(), ftpFilesReturned.get(0).getTimestamp());
    }

    @Test
    public void testChangeDir() throws IOException {
        Mockito.when(mockedApacheFtpClient.changeWorkingDirectory(Mockito.anyString())).thenReturn(true);

        ftpClient.changeDir("");
    }

    @Ignore
    @Test
    public void testDownloadFile() throws IOException {
        String source = "src/test/resources/test.txt";
        String dest = "src/test/resources/downloadedTest.txt";

        ftpClient.downloadFile(source, dest);

        Path destPath = Paths.get(dest);
        Assert.assertTrue(Files.exists(destPath));
        Files.delete(destPath);
    }

    @Ignore
    @Test
    public void testDownloadFileKeepDate() throws IOException {
        FtpFile source = new FtpFile();
        source.setName("test.text");
        long currentTime = System.currentTimeMillis();
        source.setTimestamp(currentTime);
        String dest = "src/test/resources/downloadedTest.txt";

        ftpClient.downloadFileKeepDate(source, dest);

        // check file exists
        Path destPath = Paths.get(dest);
        Assert.assertTrue(Files.exists(destPath));

        // format current date
        Date expectedDate = new Date(currentTime);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        // get current file date in the same format as current date
        String resultDate = Files.getAttribute(destPath, "lastAccessTime").toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        LocalDateTime date = LocalDateTime.parse(resultDate, formatter);
        Date returnedDate = Date.from(date.atZone(TimeZone.getTimeZone("UTC").toZoneId()).toInstant());

        Assert.assertEquals(dateFormatter.format(expectedDate), dateFormatter.format(returnedDate));
        Files.delete(destPath);
    }

    @Test
    public void testDisconnect() throws IOException {
        ftpClient.disconnect();
    }
}
