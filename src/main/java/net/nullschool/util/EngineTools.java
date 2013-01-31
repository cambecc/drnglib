package net.nullschool.util;

import javax.crypto.Mac;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * 2013-01-25<p/>
 *
 * @author Cameron Beccario
 */
final class EngineTools {

    private static final List<String> X86 = Arrays.asList("x86", "i386");
    private static final List<String> X64 = Arrays.asList("x86_64", "amd64");
    private static final Random rand = new Random();
    private static final Path tmpdir =
        Paths.get(System.getProperty("java.io.tmpdir")).resolve("1bd31d66-eda2-4395-a2a7-510bd581e3ab");
    private static final String osname = System.getProperty("os.name", "unknown");
    private static final String osarch = System.getProperty("os.arch", "unknown");


    private EngineTools() {
    }

    private static String packageOf(Class<?> clazz) {
        return clazz.getName().substring(0, clazz.getName().lastIndexOf('.') + 1);
    }

    private static void deleteIfExists(Path dir, String glob) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, glob)) {
            for (Path p : stream) {
                Files.deleteIfExists(p);
            }
        }
        catch (IOException ignore) {
            // Deletion is best-effort, so silently continue.
        }
    }

    static Path unpackResource(Class<?> clazz, String name, Path target) throws IOException {
        try (InputStream in = clazz.getResourceAsStream(name)) {
            if (in != null) {
                Files.createDirectories(target.getParent());
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                return target;
            }
            else {
                return null;
            }
        }
    }

    static Path unpackTemporaryResource(Class<?> clazz, String name) throws IOException {
        String prefix = (name.startsWith("/") ? name.substring(1) : packageOf(clazz) + name).replace('/', '.') + '_';
        deleteIfExists(tmpdir, prefix + "*");
        Path target = tmpdir.resolve(prefix + Math.abs(rand.nextLong()));
        Path result = unpackResource(clazz, name, target);
        if (result != null) {
            result.toFile().deleteOnExit();
        }
        return result;
    }

    static String deriveRdrandLibraryName() {
        String path = "unknown/";
        String extension = ".unknown";
        String arch = X86.contains(osarch) ? "-x86" : X64.contains(osarch) ? "-x64" : "-unknown";
        if (osname.startsWith("Win")) {
            path = "windows/";
            extension = ".dll";
        }
        else if (osname.startsWith("Mac")) {
            path = "macosx/";
            extension = ".dylib";
        }
        else if (osname.startsWith("Lin")) {
            path = "linux/";
            extension = ".so";
        }
        else if (osname.startsWith("Sun")) {
            path = "solaris/";
            extension = ".so";
        }
        return path + "drnglib" + arch + extension;
    }

    static void loadRdrandNativeLibrary() throws IOException {
        String library = deriveRdrandLibraryName();

        if (RdrandEngine.class.getResource(library) == null) {
            throw new IllegalArgumentException(
                String.format("Cannot find resource '%s' for %s %s architecture.", library, osname, osarch));
        }

        for (int i = 0; i < 10; i++) {  // UNDONE: magic number 10?
            Path p = unpackTemporaryResource(RdrandEngine.class, library);
            if (p == null || !Files.exists(p)) {
                break;
            }
            try {
                System.load(p.toString());
                return;
            }
            catch (UnsatisfiedLinkError e) {
                if (Files.exists(p)) {
                    throw e;
                }
            }
        }
        throw new UnsatisfiedLinkError("Failed to load " + osname);
    }

    static byte[] hashSHA256(Key key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            return mac.doFinal(data);
        }
        catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
