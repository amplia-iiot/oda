package es.amplia.oda.datastreams.deviceinfo.configuration;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class ScriptsLoader {

	String jarFileToSearch = "es.amplia.oda.datastreams.deviceinfo";

	void load(String source, String path) {

		// source = deployed bundles directory
		// path =  scripts directory path

		// check if scripts directory already exists
		// if it exists, don't do anything
		// if it doesn't exist, create directory and extract scripts from jar
		File scriptsDir = new File(path);
		if (!scriptsDir.exists()) {
			try {
				log.info("Scripts dir '{}' don't exist. Extract default scripts from '{}' jar file", path, jarFileToSearch);
				Files.createDirectories(Paths.get(path));
			} catch (IOException e) {
				log.error(e.getMessage());
			}
			extractScriptsFromJar(source, path);
		} else {
			log.info("Scripts dir '{}' already exist. Don't do anything", path);
		}
	}

	private void extractScriptsFromJar(String source, String path) {

		File sourceFile = new File(source);
		File[] files = sourceFile.listFiles();
		File jarToExtract = null;

		// search in source directory for bundle full name
		for (File temp : Objects.requireNonNull(files)) {
			if (temp.getName().contains(jarFileToSearch)) {
				jarToExtract = temp;
			}
		}

		if (jarToExtract != null) {
			try {
				// copy jar to extract to scripts folder
				File scriptsDestFile = new File(path + File.separator + jarToExtract.getName());
				Files.copy(jarToExtract.toPath(), scriptsDestFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

				try (JarFile jar = new JarFile(scriptsDestFile)) {
					Enumeration<JarEntry> enumEntries = jar.entries();
					while (enumEntries.hasMoreElements()) {
						JarEntry entry = enumEntries.nextElement();
						File fileToCopy = new File(path + File.separator + entry.getName());
						if (entry.getName().contains(".sh")) {
							try (InputStream is = jar.getInputStream(entry); FileOutputStream fos = new FileOutputStream(fileToCopy)) {
								while (is.available() > 0) {
									fos.write(is.read());
								}
							}
						}
					}
				}

				// remove jar from destiny directory
				Files.delete(scriptsDestFile.toPath());

			} catch (IOException e) {
				log.error(e.getMessage());
			}

		} else {
			log.error("Jar '{}' not found. Change the configuration", jarFileToSearch);
		}
	}
}
