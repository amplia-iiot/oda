package es.amplia.oda.datastreams.deviceinfofx30.configuration;

import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ScriptsLoader implements AutoCloseable{

	private CommandProcessor commander;

	private String path;

	public ScriptsLoader(CommandProcessor commandProcessor) {
		commander = commandProcessor;
	}

	void load(String path) throws CommandExecutionException, IOException {
		this.path = path;
		File file = new File("deploy");
		File[] files = file.listFiles();
		String jarToExtract = null;

		for (File temp : files) {
			if(temp.getName().contains("es.amplia.oda.datastreams.deviceinfofx30")) {
				jarToExtract = temp.getName();
			}
		}

		if (jarToExtract != null) {
			commander.execute("mkdir " + path);
			commander.execute("cp deploy/" + jarToExtract + " scripts/");
			try (JarFile jar = new JarFile(path + "/" + jarToExtract)) {
				Enumeration enumEntries = jar.entries();
				while (enumEntries.hasMoreElements()) {
					JarEntry entry = (JarEntry) enumEntries.nextElement();
					File fileToCopy = new File(path + "/" + entry.getName());
					if (entry.getName().contains(".sh")) {
						try (InputStream is = jar.getInputStream(entry); FileOutputStream fos = new FileOutputStream(fileToCopy)) {
							while (is.available() > 0) {
								fos.write(is.read());
							}
						}
					}
				}
			}
		} else {
			throw new CommandExecutionException("tar -xf " + jarToExtract + " -C logs", "Jar not found. Change the configuration",
					new FileNotFoundException("Jar not found"));
		}
		commander.execute("rm " + path + "/" + jarToExtract);
	}

	@Override
	public void close() throws Exception {
		commander.execute("rm -r " + path);
	}
}
