package es.amplia.oda.core.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ScriptsLoaderImpl implements ScriptsLoader, AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptsLoaderImpl.class);

	static final String DELETE_DIR_COMMAND = "rm -r ";

	private final CommandProcessor commander;

	private String path;

	public ScriptsLoaderImpl(CommandProcessor commandProcessor) {
		commander = commandProcessor;
	}

	@Override
	public void loadScripts(String source, String path, String artifactId) throws CommandExecutionException, IOException {
		this.path = path;
		File deployDirectory = new File(source);
		File[] filesOfBundles = deployDirectory.listFiles();
		String jarToExtract = null;

		for (File checkingBundle : Objects.requireNonNull(filesOfBundles)) {
			if(checkingBundle.getName().contains(artifactId)) {
				jarToExtract = checkingBundle.getName();
				break;
			}
		}

		if (jarToExtract != null) {
			commander.execute("mkdir " + path);
			commander.execute("cp " + source + "/" + jarToExtract + " " + path);
			try (JarFile jar = new JarFile(path + "/" + jarToExtract)) {
				Enumeration enumEntries = jar.entries();
				while (enumEntries.hasMoreElements()) {
					JarEntry entry = (JarEntry) enumEntries.nextElement();
					File fileToCopy = new File(path + File.separator + entry.getName());
					if (entry.getName().contains(".sh")) {
						try (InputStream is = jar.getInputStream(entry); FileOutputStream fos = new FileOutputStream(fileToCopy)) {
							while (is.available() > 0) {
								fos.write(is.read());
							}
						}
						commander.execute("chmod u+x " + fileToCopy.getAbsolutePath());
					}
				}
			}
		} else {
			throw new CommandExecutionException("tar -xf JarFile -C logs", "Jar not found. Change the configuration",
					new FileNotFoundException("Jar not found"));
		}
		commander.execute("rm " + path + "/" + jarToExtract);
	}

	@Override
	public void close() {
		try {
			File scriptsFile = new File(path);
			if (scriptsFile.exists()) {
				commander.execute(DELETE_DIR_COMMAND + path);
			}
		} catch (CommandExecutionException e) {
			LOGGER.error("Error trying to close scripts loader", e);
		}
	}
}
