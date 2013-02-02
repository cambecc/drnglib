package net.nullschool.util;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;

import static org.junit.Assert.*;

/**
 * 2013-01-18<p/>
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */
public class RdrandEngineTest {

    private static final int SAMPLE_SIZE = 1000;

    /**
     * Returns an object output stream that compresses its output with ZLIB.
     */
    private static ObjectOutputStream newCompressor(OutputStream out) throws IOException {
        return new ObjectOutputStream(new DeflaterOutputStream(out));
    }

    /**
     * Returns the specified declared field.
     */
    private static Field getField(Class<?> clazz, String name) throws Exception {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        // Allow setting of static final fields.
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        return field;
    }

    /**
     * Returns the number of zero bytes.
     */
    private static int zeroCount(byte[] bytes) {
        int result = 0;
        for (byte b : bytes) {
            if (b == 0) {
                result++;
            }
        }
        return result;
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        // If this throws, then native method linking failed, which means we can't even ask the CPU if
        // it supports rdrand.
        RdrandEngine.isSupported();
    }

    @Before
    public void beforeMethod() throws IOException {
        Assume.assumeTrue("Rdrand not supported by this CPU.", RdrandEngine.isSupported());
    }

    @Test
    public void test_byte_randomness() throws IOException {
        // A blob of random samples should not be compressible.
        RdrandEngine re = new RdrandEngine();
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (ObjectOutputStream out = newCompressor(compressed)) {
            byte[] bytes = new byte[SAMPLE_SIZE];
            re.engineNextBytes(bytes);
            out.write(bytes);
        }
        assertTrue(
            "Unexpected compressed size: " + compressed.size(),
            compressed.size() > SAMPLE_SIZE);
    }

    @Test
    public void test_int_randomness() throws IOException {
        // A blob of random samples should not be compressible.
        RdrandEngine re = new RdrandEngine();
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (ObjectOutputStream out = newCompressor(compressed)) {
            for (int i = 0; i < SAMPLE_SIZE; i++) {
                out.writeInt(re.engineNextInt());
            }
        }
        assertTrue(
            "Unexpected compressed size: " + compressed.size(),
            compressed.size() > SAMPLE_SIZE * 4);
    }

    @Test
    public void test_long_randomness() throws IOException {
        // A blob of random samples should not be compressible.
        RdrandEngine re = new RdrandEngine();
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (ObjectOutputStream out = newCompressor(compressed)) {
            for (int i = 0; i < SAMPLE_SIZE; i++) {
                out.writeLong(re.engineNextLong());
            }
        }
        assertTrue(
            "Unexpected compressed size: " + compressed.size(),
            compressed.size() > SAMPLE_SIZE * 8);
    }

    @Test
    public void test_seed_randomness() throws IOException {
        // A blob of random seed data should not be compressible.
        RdrandEngine re = new RdrandEngine();
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (ObjectOutputStream out = newCompressor(compressed)) {
            out.write(re.engineGenerateSeed(SAMPLE_SIZE));
        }
        assertTrue(
            "Unexpected compressed size: " + compressed.size(),
            compressed.size() > SAMPLE_SIZE);
    }

    @Test
    public void test_compressible_data() throws IOException {
        // For fun, ensure that non-random data is highly compressible.
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (ObjectOutputStream out = newCompressor(compressed)) {
            out.write(new byte[SAMPLE_SIZE]); // all zeros
        }
        assertTrue(
            "Unexpected compressed size: " + compressed.size(),
            compressed.size() < (SAMPLE_SIZE / 10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_next_bytes_throws() {
        new RdrandEngine().engineNextBytes(null);
    }

    @Test
    public void test_next_bytes() {
        RdrandEngine re = new RdrandEngine();
        byte[] bytes;

        re.engineNextBytes(bytes = new byte[0]);
        assertEquals(0, bytes.length);

        re.engineNextBytes(bytes = new byte[1]);
        assertEquals(1, bytes.length);

        re.engineNextBytes(bytes = new byte[15]);
        assertEquals(15, bytes.length);
        assertTrue(zeroCount(bytes) < 8);

        re.engineNextBytes(bytes = new byte[16]);
        assertEquals(16, bytes.length);
        assertTrue(zeroCount(bytes) < 8);

        re.engineNextBytes(bytes = new byte[31]);
        assertEquals(31, bytes.length);
        assertTrue(zeroCount(bytes) < 8);

        re.engineNextBytes(bytes = new byte[32]);
        assertEquals(32, bytes.length);
        assertTrue(zeroCount(bytes) < 8);

        re.engineNextBytes(bytes = new byte[127]);
        assertEquals(127, bytes.length);
        assertTrue(zeroCount(bytes) < 8);

        re.engineNextBytes(bytes = new byte[128]);
        assertEquals(128, bytes.length);
        assertTrue(zeroCount(bytes) < 8);
    }

    @Test
    public void test_valid_seed_sizes() {
        RdrandEngine re = new RdrandEngine();
        byte[] seed;

        seed = re.engineGenerateSeed(0);
        assertEquals(0, seed.length);

        seed = re.engineGenerateSeed(1);
        assertEquals(1, seed.length);

        seed = re.engineGenerateSeed(15);
        assertEquals(15, seed.length);
        assertTrue(zeroCount(seed) < 8);

        seed = re.engineGenerateSeed(16);
        assertEquals(16, seed.length);
        assertTrue(zeroCount(seed) < 8);

        seed = re.engineGenerateSeed(31);
        assertEquals(31, seed.length);
        assertTrue(zeroCount(seed) < 8);

        seed = re.engineGenerateSeed(32);
        assertEquals(32, seed.length);
        assertTrue(zeroCount(seed) < 8);

        seed = re.engineGenerateSeed(127);
        assertEquals(127, seed.length);
        assertTrue(zeroCount(seed) < 8);

        seed = re.engineGenerateSeed(128);
        assertEquals(128, seed.length);
        assertTrue(zeroCount(seed) < 8);
    }

    @Test(expected = NegativeArraySizeException.class)
    public void test_negative_seed_size_throws() {
        new RdrandEngine().engineGenerateSeed(-1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_set_seed_throws() {
        new RdrandEngine().engineSetSeed(new byte[16]);
    }

    @Test
    public void test_unsupported_architecture_throws() throws Exception {
        String original = System.getProperty("os.arch");
        try {
            // Change to an unsupported architecture and clear flags.
            getField(EngineTools.class, "osarch").set(null, "Z80");
            Class<?> nativeMethods = Class.forName(RdrandEngine.class.getName() + "$NativeMethods");
            getField(nativeMethods, "isLinked").setBoolean(null, false);
            getField(RdrandEngine.class, "isSupported").setBoolean(null, false);

            // Confirm native library load fails on this unexpected architecture.
            try {
                new RdrandEngine();
                fail();
            }
            catch (UnsupportedOperationException e) {
                assertTrue(Pattern.matches("Cannot find resource.*Z80.*", e.getCause().getMessage()));
            }

            // Confirm it again. Should get the same failure.
            try {
                new RdrandEngine();
                fail();
            }
            catch (UnsupportedOperationException e) {
                assertTrue(Pattern.matches("Cannot find resource.*Z80.*", e.getCause().getMessage()));
            }
        }
        finally {
            getField(EngineTools.class, "osarch").set(null, original);
        }

        // Confirm native library load now works after restoring original architecture.
        new RdrandEngine();
    }
}
