/*
 * Copyright (c) Libly - Terl Tech Ltd  • 24/08/2019, 16:01 • libly.co, goterl.com
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.libly.resourceloader;


import com.sun.jna.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Loads resources from a relative or absolute path
 * even if the file is in a JAR.
 */
public class ResourceLoader {

    private final Logger logger = LoggerFactory.getLogger("ResourceLoader");

    private final Collection<PosixFilePermission> writePerms = new ArrayList<>();
    private final Collection<PosixFilePermission> readPerms = new ArrayList<>();
    private final Collection<PosixFilePermission> execPerms = new ArrayList<>();

    ResourceLoader() {
        readPerms.add(PosixFilePermission.OWNER_READ);
        readPerms.add(PosixFilePermission.OTHERS_READ);
        readPerms.add(PosixFilePermission.GROUP_READ);

        writePerms.add(PosixFilePermission.OWNER_WRITE);
        writePerms.add(PosixFilePermission.OTHERS_WRITE);
        writePerms.add(PosixFilePermission.GROUP_WRITE);

        execPerms.add(PosixFilePermission.OWNER_EXECUTE);
        execPerms.add(PosixFilePermission.OTHERS_EXECUTE);
        execPerms.add(PosixFilePermission.GROUP_EXECUTE);
    }

    /**
     * Copies a file into a temporary directory regardless of
     * if it is in a JAR or not.
     * @param relativePath A relative path to a file or directory
     *                     relative to the resources folder.
     * @return The file or directory you want to load.
     * @throws IOException If at any point processing of the resource file fails.
     * @throws URISyntaxException If cannot find the resource file.
     */
    public File copyToTempDirectory(String relativePath, Class outsideClass) throws IOException, URISyntaxException {
        // If the file does not start with a separator,
        // then let's make sure it does!
        if (!relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }

        // Create a "main" temporary directory in which
        // everything can be thrown in.
        File mainTempDir = createMainTempDirectory();

        // Create the required directories.
        mainTempDir.mkdirs();

        // Is the user loading this in a JAR?
        URL jarUrl = getThisJarPath(outsideClass);
        if (isJarFile(jarUrl)) {
            // If so the get the file/directory
            // from a JAR
            try {
                return getFileFromJar(jarUrl, mainTempDir, relativePath);
            } catch (URISyntaxException e) {
                // Do not print a stacktrace here!
                // e.printStackTrace();
            }
        }

        // If not then get the file/directory
        // straight from the file system
        return getFileFromFileSystem(relativePath, mainTempDir);
    }

    private boolean isJarFile(URL jarUrl) {
        if (jarUrl != null) {
            try (JarFile jarFile = new JarFile(jarUrl.getPath())) {
                // Successfully opened the jar file. Check if there's a manifest
                // This is probably not necessary
                Manifest manifest = jarFile.getManifest();
                if (manifest != null) {
                    return true;
                }
            } catch (IOException | IllegalStateException | SecurityException e) {
                logger.debug("This is not a JAR file due to {}", e.getMessage());
            }
        }
        return false;
    }

    /**
     * Unzips a file/directory from a JAR if we're in a JAR. A JAR is simply
     * a zip file. We can unzip it and get our file successfully.
     * @param jarUrl This JAR's URL.
     * @param outputDir A directory of where to store our extracted files.
     * @param pathInJar A relative path to a file that is in our resources folder.
     * @return The file or directory that we requested.
     * @throws URISyntaxException If we could not ascertain our location.
     * @throws IOException If whilst unzipping we had some problems.
     */
    private File getFileFromJar(URL jarUrl, File outputDir, String pathInJar) throws URISyntaxException, IOException {
        File jar = urlToFile(jarUrl);
        unzip(jar.getAbsolutePath(), outputDir.getAbsolutePath());
        String filePath = outputDir.getAbsolutePath() + pathInJar;
        return new File(filePath);
    }

    /**
     * If we're not in a JAR then we can load directly from the file system
     * without all the unzipping fiasco present in {@see #getFileFromJar}.
     * @param relativePath A relative path to a file or directory in the resources folder.
     * @param outputDir A directory in which to store loaded files. Preferentially a temporary one.
     * @return The file or directory that was requested.
     * @throws IOException Could not find your requested file.
     */
    private File getFileFromFileSystem(String relativePath, File outputDir) throws IOException, URISyntaxException {
        final URL url = ResourceLoader.class.getResource(relativePath);
        final String urlString = url.getFile();
        final File file;
        if (Platform.isWindows()) {
            file = Paths.get(url.toURI()).toFile();
        } else {
            file = new File(urlString);
        }

        if (file.isFile()) {
            File resource = new File(relativePath);
            File resourceCopiedToTempFolder = new File(outputDir, resource.getName());
            doCopyFile(file, resourceCopiedToTempFolder);
            return resourceCopiedToTempFolder;
        } else {
            copyDirectory(file, outputDir);
            return outputDir;
        }
    }

    /**
     * From https://www.javadevjournal.com/java/zipping-and-unzipping-in-java/
     * @param zipFilePath An absolute path to a zip file
     * @param unzipLocation Where to unzip the zip file
     * @throws IOException If could not unzip.
     */
    private static void unzip(final String zipFilePath, final String unzipLocation) throws IOException {
        if (!(Files.exists(Paths.get(unzipLocation)))) {
            Files.createDirectories(Paths.get(unzipLocation));
        }
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                Path filePath = Paths.get(unzipLocation, entry.getName());
                if (!entry.isDirectory()) {
                    filePath.getParent().toFile().mkdirs();
                    unzipFiles(zipInputStream, filePath);
                } else {
                    Files.createDirectories(filePath);
                }

                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
        }
    }

    private static void unzipFiles(final ZipInputStream zipInputStream, final Path unzipFilePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(unzipFilePath.toAbsolutePath().toString()))) {
            byte[] bytesIn = new byte[1024];
            int read = 0;
            while ((read = zipInputStream.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }


    private static final long FILE_COPY_BUFFER_SIZE = 1000000 * 30;

    /**
     * From Apache Commons
     * @param srcFile The source file
     * @param destFile The destination file
     * @throws IOException
     */
    private static void doCopyFile(final File srcFile, final File destFile)
            throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        try (FileInputStream fis = new FileInputStream(srcFile);
             FileChannel input = fis.getChannel();
             FileOutputStream fos = new FileOutputStream(destFile);
             FileChannel output = fos.getChannel()) {
            final long size = input.size(); // TODO See IO-386
            long pos = 0;
            long count = 0;
            while (pos < size) {
                final long remain = size - pos;
                count = remain > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : remain;
                final long bytesCopied = output.transferFrom(input, pos, count);
                if (bytesCopied == 0) { // IO-385 - can happen if file is truncated after caching the size
                    break; // ensure we don't loop forever
                }
                pos += bytesCopied;
            }
        }

        final long srcLen = srcFile.length(); // TODO See IO-386
        final long dstLen = destFile.length(); // TODO See IO-386
        if (srcLen != dstLen) {
            throw new IOException("Failed to copy full contents from '" +
                    srcFile + "' to '" + destFile + "' Expected length: " + srcLen + " Actual: " + dstLen);
        }
    }

    /**
     * From Apache Commons
     * @param srcDir The source directory
     * @param destDir The destination directory
     * @throws IOException
     */
    private static void copyDirectory(final File srcDir, final File destDir) throws IOException {
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
            final File[] srcFiles = srcDir.listFiles();
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<>(srcFiles.length);
                for (final File srcFile : srcFiles) {
                    final File copiedFile = new File(destDir, srcFile.getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, exclusionList);
    }

    private static void doCopyDirectory(final File srcDir, final File destDir, final List<String> exclusionList)
            throws IOException {
        // recurse
        final File[] srcFiles = srcDir.listFiles();
        if (srcFiles == null) {  // null if abstract pathname does not denote a directory, or if an I/O error occurs
            throw new IOException("Failed to list contents of " + srcDir);
        }
        if (destDir.exists()) {
            if (destDir.isDirectory() == false) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs() && !destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
        }
        if (destDir.canWrite() == false) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        for (final File srcFile : srcFiles) {
            final File dstFile = new File(destDir, srcFile.getName());
            if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
                if (srcFile.isDirectory()) {
                    doCopyDirectory(srcFile, dstFile, exclusionList);
                } else {
                    doCopyFile(srcFile, dstFile);
                }
            }
        }
    }

    /**
     * Creates the main temporary directory for resource-loader.
     * @return A directory that you can store temporary resources in
     * @throws IOException Could not create a temporary directory
     */
    public static File createMainTempDirectory() throws IOException {
        Path path = Files.createTempDirectory("resource-loader");
        File dir = path.toFile();
        dir.mkdir();
        dir.deleteOnExit();
        return dir;
    }

    /**
     * Sets permissions on a file or directory. This allows all users
     * to read, write and execute.
     * @see #setPermissions(File, Set)
     * @param file A file to set global permissions on
     * @return The file with the global permissions set
     * @throws IOException Could not set permissions
     */
    public File setPermissions(File file) throws IOException {
        return setPermissions(file, new HashSet<>());
    }

    /**
     * Sets a file or directory's permissions. @{code filePermissions} can be null, in that
     * case then global read, wrote and execute permissions will be set, so use
     * with caution.
     * @param file The file to set new permissions on.
     * @param filePermissions New permissions.
     * @return The file with correct permissions set.
     * @throws IOException
     */
    public File setPermissions(File file, Set<PosixFilePermission> filePermissions) throws IOException {
        if (isPosixCompliant()) {
            // For posix set fine grained permissions.
            if (filePermissions.isEmpty()) {
                Set<PosixFilePermission> perms = new HashSet<>();
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                perms.add(PosixFilePermission.OWNER_EXECUTE);

                perms.add(PosixFilePermission.OTHERS_READ);
                perms.add(PosixFilePermission.OTHERS_WRITE);
                perms.add(PosixFilePermission.OTHERS_EXECUTE);

                perms.add(PosixFilePermission.GROUP_READ);
                perms.add(PosixFilePermission.GROUP_WRITE);
                perms.add(PosixFilePermission.GROUP_EXECUTE);
                filePermissions = perms;
            }
            Files.setPosixFilePermissions(file.toPath(), filePermissions);
        } else {
            // For non-posix like Windows find if any are true and
            // set the permissions accordingly.
            if (filePermissions.stream().anyMatch(readPerms::contains)) {
                file.setReadable(true);
            } else if (filePermissions.stream().anyMatch(writePerms::contains)) {
                file.setWritable(true);
            } else {
                file.setExecutable(true);
            }

        }
        return file;
    }

    /**
     * Mark the file or directory as "to be deleted".
     * @param file The file or directory to be deleted.
     */
    public void requestDeletion(File file) {
        if (isPosixCompliant()) {
            // The file can be deleted immediately after loading
            file.delete();
        } else {
            // Don't delete until last file descriptor closed
            file.deleteOnExit();
        }
    }

    /**
     * Is the system we're running on Posix compliant?
     * @return True if posix compliant.
     */
    protected boolean isPosixCompliant() {
        try {
            return FileSystems.getDefault()
                    .supportedFileAttributeViews()
                    .contains("posix");
        } catch (FileSystemNotFoundException | ProviderNotFoundException | SecurityException e) {
            return false;
        }
    }

    /**
     * Gets the base location of the given class.
     * From SciJava class.
     * <p>
     * If the class is directly on the file system (e.g.,
     * "/path/to/my/package/MyClass.class") then it will return the base directory
     * (e.g., "file:/path/to").
     * </p>
     * <p>
     * If the class is within a JAR file (e.g.,
     * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
     * path to the JAR (e.g., "file:/path/to/my-jar.jar").
     * </p>
     *
     * @param c The class whose location is desired.
     */
    public static URL getThisJarPath(final Class<?> c) {
        if (c == null) return null; // could not load the class

        // try the easy way first
        try {
            final URL codeSourceLocation =
                    c.getProtectionDomain().getCodeSource().getLocation();
            if (codeSourceLocation != null) return codeSourceLocation;
        }
        catch (final SecurityException e) {
            // NB: Cannot access protection domain.
        }
        catch (final NullPointerException e) {
            // NB: Protection domain or code source is null.
        }

        // NB: The easy way failed, so we try the hard way. We ask for the class
        // itself as a resource, then strip the class's path from the URL string,
        // leaving the base path.

        // get the class's raw resource path
        final URL classResource = c.getResource(c.getSimpleName() + ".class");
        if (classResource == null) return null; // cannot find class resource

        final String url = classResource.toString();
        final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
        if (!url.endsWith(suffix)) return null; // weird URL

        // strip the class's path from the URL string
        final String base = url.substring(0, url.length() - suffix.length());

        String path = base;

        // remove the "jar:" prefix and "!/" suffix, if present
        if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

        try {
            return new URL(path);
        }
        catch (final MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts the given {@link URL} to its corresponding {@link File}.
     * From SciJava class.
     * <p>
     * This method is similar to calling {@code new File(url.toURI())} except that
     * it also handles "jar:file:" URLs, returning the path to the JAR file.
     * </p>
     *
     * @param url The URL to convert.
     * @return A file path suitable for use with e.g. {@link FileInputStream}
     * @throws IllegalArgumentException if the URL does not correspond to a file.
     */
    public static File urlToFile(final URL url) {
        return url == null ? null : urlToFile(url.toString());
    }

    /**
     * Converts the given URL string to its corresponding {@link File}.
     * From SciJava class.
     * @param url The URL to convert.
     * @return A file path suitable for use with e.g. {@link FileInputStream}
     * @throws IllegalArgumentException if the URL does not correspond to a file.
     */
    public static File urlToFile(final String url) {
        String path = url;
        if (path.startsWith("jar:")) {
            // remove "jar:" prefix and "!/" suffix
            final int index = path.indexOf("!/");
            path = path.substring(4, index);
        }
        try {
            if (Platform.isWindows() && path.matches("file:[A-Za-z]:.*")) {
                path = "file:/" + path.substring(5);
            }
            return new File(new URL(path).toURI());
        }
        catch (final MalformedURLException e) {
            // NB: URL is not completely well-formed.
        }
        catch (final URISyntaxException e) {
            // NB: URL is not completely well-formed.
        }
        if (path.startsWith("file:")) {
            // pass through the URL as-is, minus "file:" prefix
            path = path.substring(5);
            return new File(path);
        }
        throw new IllegalArgumentException("Invalid URL: " + url);
    }
}
