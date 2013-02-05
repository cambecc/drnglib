package net.nullschool.util;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * 2013-01-15<p/>
 *
 * A launcher for <a href="http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javah.html">
 * javah</a> that normalizes the output to be consistent between platforms. Having platform-specific
 * behavior causes version control noise when nothing has changed and is confusing.<p/>
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */
public class JavahLauncher {

    /**
     * Javah outputs different text depending on the operating system. For example,
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5085132. Having platform-specific
     * behavior causes version control noise when nothing has changed and is confusing. This
     * method opens the specified file, normalizes the contents, then overwrites the original
     * if modifications are required.
     *
     * @param file the file to normalize
     * @throws IOException
     */
    private static void normalize(Path file) throws IOException {
        String original = new String(Files.readAllBytes(file), "UTF-8");
        String modified = original;

        // Use C99 standard suffix "LL" instead of "i64" for 64-bit numeric literals.
        modified = modified.replaceAll("(\\d)i64", "$1LL");
        // Use UNIX-style line endings.
        modified = modified.replaceAll("\\r\\n", "\n");

        if (!modified.equals(original)) {
            Files.write(file, modified.getBytes("UTF-8"));
        }
    }

    public static void main(String[] args) throws Exception {
        // Find and invoke com.sun.tools.javah.Main.run(String[], PrintWriter). Doing this dynamically
        // means we can avoid polluting the project with a dependency on tools.jar.
        Class<?> clazz = Class.forName("com.sun.tools.javah.Main");
        Method method = clazz.getMethod("run", String[].class, PrintWriter.class);
        method.invoke(null, args, new PrintWriter(System.out));

        for (int i = 0; i < args.length; i++) {
            // Find the output file argument:  -o <file>
            if ("-o".equals(args[i]) && (i + 1 < args.length)) {
                normalize(Paths.get(args[i + 1]));
                break;
            }
        }
    }
}
