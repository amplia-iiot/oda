package es.amplia.oda.service.zipcompress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.ZipFile;

public class ZipCompress {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipCompress.class);

    public static void zipFile (String zipFile, String file) throws IOException {
        // zip file
        try (ZipFile zip = new ZipFile(zipFile)) {
            zip.addFile(file);
        } catch (Exception e) {
            LOGGER.error("Error zip file " + zipFile + " adding file " + file, e);
            throw e;
        }
    }

    public static void zipFiles (String zipFile, List<String> files) throws IOException {
        // zip files
        try (ZipFile zip = new ZipFile(zipFile)) {
            List<File> fileList = new ArrayList<>();
            files.forEach(f -> fileList.add(new File(f)));
            zip.addFiles(fileList);
        } catch (Exception e) {
            LOGGER.error("Error zip file " + zipFile + " adding files " + files, e);
            throw e;
        }
    }

    public static void zipFolder (String zipFile, String folder) throws IOException {
        // zip folder
        try (ZipFile zip = new ZipFile(zipFile)) {
            zip.addFolder(new File(folder));
        } catch (Exception e) {
            LOGGER.error("Error zip file " + zipFile + " adding folder " + folder, e);
            throw e;
        }
    }

    public static void unzipFile(String folder, String zipFile) throws IOException {
        // unzip file
        try (ZipFile zip = new ZipFile(zipFile)) {
            zip.extractAll(folder);
        } catch (Exception e) {
            LOGGER.error("Error unzip file " + zipFile + " in folder " + folder, e);
            throw e;
        }
    }

}
