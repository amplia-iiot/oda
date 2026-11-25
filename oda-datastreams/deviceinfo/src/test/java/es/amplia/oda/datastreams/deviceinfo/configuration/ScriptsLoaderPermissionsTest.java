package es.amplia.oda.datastreams.deviceinfo.configuration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class ScriptsLoaderPermissionsTest {

	private static final String SCRIPT = "#!/bin/sh\necho SN-1234\n";

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void extractedScriptsAreReadableAndExecutable() throws IOException {
		assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));

		File bundles = tmp.newFolder("bundles");
		writeJar(new File(bundles, "es.amplia.oda.datastreams.deviceinfo-4.16.2.jar"));
		Path scripts = tmp.getRoot().toPath().resolve("scripts");

		new ScriptsLoader().load(bundles.getPath(), scripts.toString());

		Path script = scripts.resolve("obtainSerialNumber.sh");
		Set<PosixFilePermission> perms = Files.getPosixFilePermissions(script);
		assertTrue(perms.contains(PosixFilePermission.OWNER_READ));
		assertTrue(perms.contains(PosixFilePermission.OWNER_EXECUTE));
		assertTrue(perms.contains(PosixFilePermission.GROUP_READ));
		assertTrue(perms.contains(PosixFilePermission.OTHERS_READ));
		assertEquals(SCRIPT, new String(Files.readAllBytes(script), StandardCharsets.UTF_8));
	}

	private static void writeJar(File jar) throws IOException {
		try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jar))) {
			out.putNextEntry(new JarEntry("obtainSerialNumber.sh"));
			out.write(SCRIPT.getBytes(StandardCharsets.UTF_8));
			out.closeEntry();
		}
	}
}
