package net.nullschool.util;

import org.junit.Test;

import java.io.*;
import java.nio.file.Path;

import static org.junit.Assert.*;
import static net.nullschool.util.EngineTools.*;

/**
 * 2013-01-07<p/>
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */
public class EngineToolsTest {

//    @Test
//    public void test_absolute_resource_as_file() throws Exception {
//        Path resource = unpackTemporaryResource(NetPackage.class, "/net/nullschool/util/sample.a.txt");
//        assertNotNull(resource);
//        assertTrue(resource.getFileName().toString().startsWith("net.nullschool.util.sample.a.txt_"));
//        try (BufferedReader reader = new BufferedReader(new FileReader(resource.toFile()))) {
//            assertEquals("hello world", reader.readLine());
//        }
//    }
//
//    @Test
//    public void test_relative_resource_as_file() throws Exception {
//        Path resource = unpackTemporaryResource(NetPackage.class, "nullschool/util/sample.a.txt");
//        assertNotNull(resource);
//        assertTrue(resource.getFileName().toString().startsWith("net.nullschool.util.sample.a.txt_"));
//        try (BufferedReader reader = new BufferedReader(new FileReader(resource.toFile()))) {
//            assertEquals("hello world", reader.readLine());
//        }
//    }
//
//    @Test
//    public void test_absolute_no_package_resource_as_file() throws Exception {
//        Class<?> clazz = EngineTools.class.getClassLoader().loadClass("NoPackage");
//        Path resource = unpackTemporaryResource(clazz, "/sample.b.txt");
//        assertNotNull(resource);
//        assertTrue(resource.getFileName().toString().startsWith("sample.b.txt_"));
//        try (BufferedReader reader = new BufferedReader(new FileReader(resource.toFile()))) {
//            assertEquals("hello world", reader.readLine());
//        }
//    }
//
//    @Test
//    public void test_relative_no_package_resource_as_file() throws Exception {
//        Class<?> clazz = EngineTools.class.getClassLoader().loadClass("NoPackage");
//        Path resource = unpackTemporaryResource(clazz, "sample.b.txt");
//        assertNotNull(resource);
//        assertTrue(resource.getFileName().toString().startsWith("sample.b.txt_"));
//        try (BufferedReader reader = new BufferedReader(new FileReader(resource.toFile()))) {
//            assertEquals("hello world", reader.readLine());
//        }
//    }
//
//    @Test
//    public void test_bad_resource() throws Exception {
//        assertNull(unpackTemporaryResource(EngineToolsTest.class, "doesn't exist"));
//    }
//
//    @Test()
//    public void test_load_library_action() throws Exception {
//        final boolean[] flag = new boolean[1];
//        loadDynamicLibrary(
//            EngineToolsTest.class,
//            "sample.a.txt",
//            new LoadLibraryAction() {
//                @Override public void load(String filename) {
//                    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
//                        assertEquals("hello world", reader.readLine());
//                        flag[0] = true;
//                    }
//                    catch (Exception e) {
//                        fail();
//                    }
//                }
//            });
//        assertTrue(flag[0]);
//    }
//
//    @Test(expected = UnsatisfiedLinkError.class)
//    public void test_load_bad_dynamic_library() throws Exception {
//        loadDynamicLibrary(
//            EngineToolsTest.class,
//            "doesn't exist",
//            new LoadLibraryAction() {
//                @Override public void load(String filename) {
//                    fail();
//                }
//            });
//    }
}
