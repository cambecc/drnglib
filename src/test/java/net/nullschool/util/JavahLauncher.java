package net.nullschool.util;

import java.io.PrintWriter;
import java.lang.reflect.Method;


/**
 * 2013-01-15<p/>
 *
 * A launcher for <a href="http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javah.html">javah</a>
 * that forces a consistent line separator irrespective of the hosting platform. This ensures javah does not
 * replace an existing file simply due to differences in line separators, thus avoiding version control
 * noise.<p/>
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */
public class JavahLauncher {

    public static void main(String[] args) throws Exception {
        String original = System.getProperty("line.separator");
        try {
            System.setProperty("line.separator", "\n");

            // Find and invoke com.sun.tools.javah.Main.run(String[], PrintWriter). Doing this dynamically
            // means we can avoid polluting the project with a dependency on tools.jar.
            Class<?> clazz = Class.forName("com.sun.tools.javah.Main");
            Method method = clazz.getMethod("run", String[].class, PrintWriter.class);
            method.invoke(null, args, new PrintWriter(System.out));
        }
        finally {
            // Make sure to restore the original value since we may be running in a non-forked JVM.
            System.setProperty("line.separator", original);
        }
    }
}
