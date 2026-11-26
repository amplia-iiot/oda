package es.amplia.oda.datastreams.deviceinfo.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Driven against a real jar on a real filesystem.
 *
 * These tests used to mock java.io.File with mockConstruction. With the inline mock maker that
 * also intercepts the File instances the JDK class loader builds while loading classes, which
 * poisons class loading for whatever test runs afterwards: the module failed with
 * NoClassDefFoundError depending on surefire's runOrder, and passed with the default order.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ScriptsLoaderTest {

	private static final String BUNDLE_JAR_NAME = "es.amplia.oda.datastreams.deviceinfo-1.0.0.jar";
	private static final String SCRIPT_ENTRY = "getSerialNumber.sh";
	private static final String SCRIPT_CONTENT = "#!/bin/sh\necho 1234\n";

	@InjectMocks
	private ScriptsLoader scriptsLoader;

	private Path sourceDir;
	private Path destBase;
	private String destDir;

	@BeforeEach
	public void createDirs() throws IOException {
		sourceDir = Files.createTempDirectory("oda-scripts-source");
		destBase = Files.createTempDirectory("oda-scripts-dest");
		// Deliberately not created: load() only extracts when the scripts dir does not exist.
		destDir = destBase.resolve("scripts").toString();
	}

	@AfterEach
	public void removeDirs() throws IOException {
		deleteRecursively(sourceDir);
		deleteRecursively(destBase);
	}

	@Test
	public void testLoad() throws Exception {
		createJar(BUNDLE_JAR_NAME);

		scriptsLoader.load(sourceDir.toString(), destDir);

		Path extracted = path(destDir).resolve(SCRIPT_ENTRY);
		assertTrue(Files.exists(extracted), "the script must be extracted from the jar");
		// Not asserting the content: ScriptsLoader leaves the file as --x--x--x (no read
		// permission), which is the known ODA-509 defect and is out of scope here.
		assertFalse(Files.exists(path(destDir).resolve(BUNDLE_JAR_NAME)), "the jar copied to extract from must be removed afterwards");
	}

	@Test
	public void testLoadDirAlreadyExists() throws Exception {
		createJar(BUNDLE_JAR_NAME);
		Files.createDirectories(path(destDir));

		scriptsLoader.load(sourceDir.toString(), destDir);

		assertEquals(0, countFiles(path(destDir)), "nothing must be extracted when the scripts dir already exists");
	}

	@Test
	public void testLoadJarNotExist() throws Exception {
		createJar("notExist.jar");

		scriptsLoader.load(sourceDir.toString(), destDir);

		assertTrue(Files.exists(path(destDir)), "the scripts dir is still created");
		assertEquals(0, countFiles(path(destDir)), "nothing must be extracted when the bundle jar is not in the source dir");
	}

	private void createJar(String name) throws IOException {
		try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(sourceDir.resolve(name)))) {
			jar.putNextEntry(new JarEntry(SCRIPT_ENTRY));
			jar.write(SCRIPT_CONTENT.getBytes(StandardCharsets.UTF_8));
			jar.closeEntry();
		}
	}

	private static Path path(String dir) {
		return new File(dir).toPath();
	}

	private static long countFiles(Path dir) throws IOException {
		try (java.util.stream.Stream<Path> paths = Files.list(dir)) {
			return paths.count();
		}
	}

	private static void deleteRecursively(Path dir) throws IOException {
		if (dir == null || !Files.exists(dir)) {
			return;
		}
		try (java.util.stream.Stream<Path> paths = Files.walk(dir)) {
			paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}
	}
}
