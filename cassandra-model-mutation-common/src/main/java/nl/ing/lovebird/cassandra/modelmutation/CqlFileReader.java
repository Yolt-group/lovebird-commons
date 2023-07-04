package nl.ing.lovebird.cassandra.modelmutation;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static java.util.stream.Collectors.toList;

@Slf4j
public class CqlFileReader {

    private CqlFileReader() {
    }

    public static List<String> cqlFiles(final String dirPath) throws IOException {
        final ClassLoader classLoader = CqlFileReader.class.getClassLoader();
        final URL resource = classLoader.getResource(dirPath);

        if (resource == null) {
            throw new FileNotFoundException(dirPath);
        }

        final List<String> cqlFileNames;
        if (resource.getProtocol().equals("jar")) {
            // Loading the file names from the JAR file
            final JarFile jarFile = ((JarURLConnection) resource.openConnection()).getJarFile();
            final Enumeration<JarEntry> entries = jarFile.entries();
            cqlFileNames = Collections.list(entries).stream()
                    .map(ZipEntry::getName)
                    .filter(name -> name.startsWith(dirPath) && !name.equals(dirPath + "/")) // Not the directory itself.
                    .map(s -> Paths.get(s).getFileName().toString())
                    .collect(toList());
        } else {
            // Resource is just a file in the file system
            cqlFileNames = getResourceFiles(resource);
        }

        if (cqlFileNames.isEmpty()) {
            log.error("Directory {} does not contain any Cassandra update files, so there is no need to wait until updates are applied.", dirPath);
            return new ArrayList<>();
        } else {
            log.info("Found cassandra updates files to wait for {}", cqlFileNames);
            return cqlFileNames;
        }
    }

    private static List<String> getResourceFiles(URL path) throws IOException {
        List<String> filenames = new ArrayList<>();

        try (InputStream in = path.openStream()) {
            if (in == null) {
                throw new FileNotFoundException(path.toString());
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

                String resource;

                while ((resource = br.readLine()) != null) {
                    filenames.add(resource);
                }
            }
        }

        return filenames;
    }

}
