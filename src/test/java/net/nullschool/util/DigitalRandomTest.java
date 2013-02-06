package net.nullschool.util;

import org.junit.Test;

import java.lang.reflect.AccessibleObject;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * 2013-01-09<p/>
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */
public class DigitalRandomTest {

    static <T extends AccessibleObject> T setAccessible(T object) {
        object.setAccessible(true);
        return object;
    }

    static DigitalRandom mockInstance(long... values) {
        try {
            return setAccessible(DigitalRandom.class.getDeclaredConstructor(DigitalRandomSpi.class))
                .newInstance(new MockRdRandEngine(values));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test_next_boolean() {
        DigitalRandom dr = mockInstance(Integer.MAX_VALUE, 0, -1, Integer.MIN_VALUE);
        assertEquals(false, dr.nextBoolean());
        assertEquals(false, dr.nextBoolean());
        assertEquals(true, dr.nextBoolean());
        assertEquals(true, dr.nextBoolean());
    }

    @Test
    public void test_next_int() {
        DigitalRandom dr = mockInstance(Integer.MAX_VALUE, 1, 0, -1, Integer.MIN_VALUE);
        assertEquals(Integer.MAX_VALUE, dr.nextInt());
        assertEquals(1, dr.nextInt());
        assertEquals(0, dr.nextInt());
        assertEquals(-1, dr.nextInt());
        assertEquals(Integer.MIN_VALUE, dr.nextInt());
        assertEquals(Integer.MAX_VALUE, dr.nextInt());
        assertEquals(1, dr.nextInt());
        assertEquals(0, dr.nextInt());
        assertEquals(-1, dr.nextInt());
        assertEquals(Integer.MIN_VALUE, dr.nextInt());
    }

    @Test
    public void test_next_int_bound() {
        DigitalRandom dr = mockInstance(10, 9, -10, Integer.MAX_VALUE, 1, -1, 2, 1023, 1024, 1025);
        assertEquals(0, dr.nextInt(10));
        assertEquals(9, dr.nextInt(10));
        assertEquals(8, dr.nextInt(10));
        assertEquals(1, dr.nextInt(10));
        assertEquals(2, dr.nextInt(10));
        assertEquals(1023, dr.nextInt(1024));
        assertEquals(0, dr.nextInt(1024));
        assertEquals(1, dr.nextInt(1024));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_next_int_bad_0_bound() {
        mockInstance(0).nextInt(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_next_int_bad_negative_bound() {
        mockInstance(0).nextInt(-1);
    }

    @Test
    public void test_next_int_least_bound() {
        DigitalRandom dr = mockInstance(10, 9, -10, Integer.MAX_VALUE, 1, -1, 2, 1023, 1024, 1025);
        assertEquals(-5, dr.nextInt(-5, 5));
        assertEquals(4, dr.nextInt(-5, 5));
        assertEquals(3, dr.nextInt(-5, 5));
        assertEquals(-4, dr.nextInt(-5, 5));
        assertEquals(-3, dr.nextInt(-5, 5));
        assertEquals(511, dr.nextInt(-512, 512));
        assertEquals(-512, dr.nextInt(-512, 512));
        assertEquals(-511, dr.nextInt(-512, 512));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_next_int_bad_least_bound_a() {
        mockInstance(0).nextInt(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_next_int_bad_least_bound_b() {
        mockInstance(0).nextInt(1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_next_int_bad_least_bound_c() {
        mockInstance(0).nextInt(Integer.MIN_VALUE, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_next_int_bad_least_bound_d() {
        mockInstance(0).nextInt(-2000000000, 2000000000);
    }

    @Test
    public void test_next_long() {
        DigitalRandom dr = mockInstance(Long.MAX_VALUE, 1, 0, -1, Long.MIN_VALUE);
        assertEquals(Long.MAX_VALUE, dr.nextLong());
        assertEquals(1, dr.nextLong());
        assertEquals(0, dr.nextLong());
        assertEquals(-1, dr.nextLong());
        assertEquals(Long.MIN_VALUE, dr.nextLong());
    }

    @Test
    public void test_next_long_bound() {
        DigitalRandom dr = mockInstance(10, 9, -10, Long.MAX_VALUE, 1, -1, 2, 1023, 1024, 1025);
        assertEquals(0, dr.nextLong(10));
        assertEquals(9, dr.nextLong(10));
        assertEquals(8, dr.nextLong(10));
        assertEquals(1, dr.nextLong(10));
        assertEquals(2, dr.nextLong(10));
        assertEquals(1023, dr.nextLong(1024));
        assertEquals(0, dr.nextLong(1024));
        assertEquals(1, dr.nextLong(1024));
    }

    @Test
    public void test_next_long_least_bound() {
        DigitalRandom dr = mockInstance(10, 9, -10, Long.MAX_VALUE, 1, -1, 2, 1023, 1024, 1025);
        assertEquals(-5, dr.nextLong(-5, 5));
        assertEquals(4, dr.nextLong(-5, 5));
        assertEquals(3, dr.nextLong(-5, 5));
        assertEquals(-4, dr.nextLong(-5, 5));
        assertEquals(-3, dr.nextLong(-5, 5));
        assertEquals(511, dr.nextLong(-512, 512));
        assertEquals(-512, dr.nextLong(-512, 512));
        assertEquals(-511, dr.nextLong(-512, 512));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_next_long_bad_least_bound_a() {
        mockInstance(0).nextLong(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_next_long_bad_least_bound_b() {
        mockInstance(0).nextLong(1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_next_long_bad_least_bound_c() {
        mockInstance(0).nextLong(Long.MIN_VALUE, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_next_long_bad_least_bound_d() {
        mockInstance(0).nextLong(-9000000000000000000L, 9000000000000000000L);
    }

    @Test
    public void test_next_float() {
        DigitalRandom dr = mockInstance(
            0x00ffffff, 0x00c00000, 0x00800000,
            0x007fffff, 0x00400000, 0x00000000);
        assertEquals(0.99999994F, dr.nextFloat(), 0);
        assertEquals(0.75000000F, dr.nextFloat(), 0);
        assertEquals(0.50000000F, dr.nextFloat(), 0);
        assertEquals(0.49999994F, dr.nextFloat(), 0);
        assertEquals(0.25000000F, dr.nextFloat(), 0);
        assertEquals(0.00000000F, dr.nextFloat(), 0);
    }

    @Test
    public void test_next_double() {
        DigitalRandom dr = mockInstance(
            0x001fffffffffffffL, 0x0018000000000000L, 0x0010000000000000L,
            0x000fffffffffffffL, 0x0008000000000000L, 0x0000000000000000L);
        assertEquals(0.9999999999999999D, dr.nextDouble(), 0);
        assertEquals(0.7500000000000000D, dr.nextDouble(), 0);
        assertEquals(0.5000000000000000D, dr.nextDouble(), 0);
        assertEquals(0.4999999999999999D, dr.nextDouble(), 0);
        assertEquals(0.2500000000000000D, dr.nextDouble(), 0);
        assertEquals(0.0000000000000000D, dr.nextDouble(), 0);
    }

    @Test
    public void test_next_double_least_bound() {
        DigitalRandom dr = mockInstance(
            0x001fffffffffffffL, 0x0018000000000000L, 0x0010000000000000L,
            0x000fffffffffffffL, 0x0008000000000000L, 0x0000000000000000L);
        assertEquals(9.999999999999998D, dr.nextDouble(0, 10), 0);
        assertEquals(1.750000000000000D, dr.nextDouble(1, 2), 0);
        assertEquals(-75.0000000000000D, dr.nextDouble(-100, -50), 0);
        assertEquals(0.4999999999999999D, dr.nextDouble(0, 1), 0);
        assertEquals(0.25E100D, dr.nextDouble(0, 1E100), 0);
        assertEquals(-1.0000000000000000D, dr.nextDouble(-1, 0), 0);
    }

    @Test
    public void test_next_gaussian() {
        DigitalRandom dr =
            mockInstance(0x0018000000000000L, 0x0008000000000000L, 0x001fffffffffffffL, 0x0010000000000000L);
        assertEquals(0.8325546111576977, dr.nextGaussian(), 0);
        assertEquals(-0.8325546111576977, dr.nextGaussian(), 0);
        assertEquals(2.9802322387695312E-8, dr.nextGaussian(), 0);
        assertEquals(0.0, dr.nextGaussian(), 0);
    }

    @Test
    public void test_next_uuid() {
        DigitalRandom dr = mockInstance(0xffffffffffffffffL, 0xeeeeeeeeeeeeeeeeL);
        assertEquals(UUID.fromString("ffffffff-ffff-4fff-aeee-eeeeeeeeeeee"), dr.nextUUID());
        assertEquals(UUID.randomUUID().variant(), dr.nextUUID().variant());
        assertEquals(UUID.randomUUID().version(), dr.nextUUID().version());
    }

    @Test
    public void test_next_bytes() {
        DigitalRandom dr = mockInstance(1, 2, 3);
        byte[] result = new byte[6];
        dr.nextBytes(result);
        assertArrayEquals(new byte[] {1, 2, 3, 1, 2, 3}, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null_next_bytes_throws() {
        mockInstance(1).nextBytes(null);
    }

    @Test
    public void test_generate_seed() {
        assertArrayEquals(new byte[] {1, 2, 3, 4}, mockInstance(1, 2, 3, 4).generateSeed(4));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_set_seed_throws() {
        mockInstance(1).setSeed(new byte[16]);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_set_long_seed_throws() {
        mockInstance(1).setSeed(42);
    }

    @Test
    public void test_properties() {
        DigitalRandom dr = mockInstance(1);
        assertEquals("DRNG", dr.getAlgorithm());
        assertNull(dr.getProvider());
    }
}
