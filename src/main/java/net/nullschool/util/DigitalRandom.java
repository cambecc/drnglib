package net.nullschool.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * 2012-12-05<p/>
 *
 * A cryptographically strong random number generator that sources its random data from a hardware
 * implementation of a digital random number generator (DRNG), such as Intel Secure Key. Simply
 * instantiate and use:
 * <pre>
 *     DigitalRandom random = new DigitalRandom();
 * </pre>
 *
 * The default implementation uses a native library to access the processor's DRNG. Due to system constraints,
 * the native library resource is first copied to the temp directory specified by the system property
 * {@code java.io.tmpdir} and then loaded. If the copy or load fails, or if the processor does not contain
 * a DRNG, then instantiation of this class will throw an {@link UnsupportedOperationException}.<p/>
 *
 * In extreme cases, this generator may be unable to generate random data of sufficient quality, in which
 * case it will throw an {@link IllegalStateException}. Such cases usually represent a hardware failure of
 * the DRNG and are expected to be very rare.<p/>
 *
 * This class is thread safe and can be used by multiple threads for maximum performance. Except for
 * {@link #nextGaussian()}, this class performs no synchronization.<p/>
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */
public final class DigitalRandom extends SecureRandom {

    private static final int KEEP_24_BITS  = 0b0000000000000000000000000000000000000000111111111111111111111111;
    private static final int KEEP_31_BITS  = 0b0000000000000000000000000000000001111111111111111111111111111111;
    private static final long KEEP_53_BITS = 0b0000000000011111111111111111111111111111111111111111111111111111L;
    private static final long KEEP_63_BITS = 0b0111111111111111111111111111111111111111111111111111111111111111L;


    private final DigitalRandomSpi spi;

    private DigitalRandom(DigitalRandomSpi spi) {
        super(spi, null);
        this.spi = spi;
    }

    /**
     * Constructs a digital random number generator using the default hardware DRNG. If the
     * hardware is not available or cannot be accessed (for example, due to native library load
     * failure), then {@link UnsupportedOperationException} is thrown. Otherwise, the returned
     * generator is initialized and ready for use.
     *
     * @throws UnsupportedOperationException if the hardware DRNG does not exist or cannot be accessed.
     */
    public DigitalRandom() throws UnsupportedOperationException {
        this(new RdRandEngine());
    }

    @Override public String getAlgorithm() {
        return "DRNG";
    }

    /**
     * Returns the next uniformly distributed boolean value from this generator.
     *
     * @return a random value chosen uniformly from the set {@code {true, false}}
     */
    @Override public boolean nextBoolean() {
        return spi.engineNextInt() < 0;
    }

    /**
     * Returns the next uniformly distributed int value from this generator.
     *
     * @return a random value chosen uniformly from the range
     *         {@code [Integer.MIN_VALUE, Integer.MAX_VALUE]}
     */
    @Override public int nextInt() {
        return spi.engineNextInt();
    }

    /**
     * Returns the next uniformly distributed int value from this generator between
     * {@code 0} (inclusive) and {@code bound} (exclusive).
     *
     * @param bound the upper bound (exclusive)
     * @return a random value chosen uniformly from the range {@code [0, bound)}
     * @throws IllegalArgumentException if bound is not positive
     */
    @Override public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive.");
        }

        // Mod a random sample by the bound. That's the result. However... the range of
        // random values may not be evenly divisible by the bound, introducing a non-uniformity
        // in the possible results. To see why, say random value v is four bits, having the
        // range 0-15, and the bound is six:
        //
        //     v one of { 0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15 }
        //  v % bound:6 | 0   1   2   3   4   5 | 0   1   2   3   4   5 | 0   1   2   3
        //
        // The last "bucket" is incomplete, giving 0, 1, 2, and 3 higher chance of selection.
        // To establish uniformity, reject any values selected from the top-most incomplete bucket:
        //
        //              | 0   1   2   3   4   5 | 0   1   2   3   4   5 | X   X   X   X
        //
        // where X means to try again with another random sample. It is possible, though
        // unlikely, for the loop to never terminate if the underlying generator misbehaves
        // or falls into a black hole.

        while (true) {
            int sample = spi.engineNextInt() & KEEP_31_BITS;  // clear sign bit to make positive
            int result = sample % bound;
            if (sample - result + bound - 1 > 0) {  // confirm sample is from a complete bucket
                return result;
            }
        }
    }

    /**
     * Returns the next uniformly distributed int value from this generator between
     * {@code least} (inclusive) and {@code bound} (exclusive).
     *
     * @param least the lower bound (inclusive)
     * @param bound the upper bound (exclusive)
     * @return a random value chosen uniformly from the range {@code [least, bound)}
     * @throws IllegalArgumentException if least is not less than bound
     * @see java.util.concurrent.ThreadLocalRandom#nextInt(int, int)
     */
    public int nextInt(int least, int bound) {
        if (least >= bound) {
            throw new IllegalArgumentException("bound must be greater than least.");
        }
        return nextInt(bound - least) + least;
    }

    /**
     * Returns the next uniformly distributed long value from this generator.
     *
     * @return a random value chosen uniformly from the range
     *         {@code [Long.MIN_VALUE, Long.MAX_VALUE]}
     */
    @Override public long nextLong() {
        return spi.engineNextLong();
    }

    /**
     * Returns the next uniformly distributed long value from this generator between
     * {@code 0} (inclusive) and {@code bound} (exclusive).
     *
     * @param bound the upper bound (exclusive)
     * @return a random value chosen uniformly from the range {@code [0, bound)}
     * @throws IllegalArgumentException if bound is not positive
     */
    public long nextLong(long bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive.");
        }

        // See comments in nextInt(int bound).

        while (true) {
            long sample = spi.engineNextLong() & KEEP_63_BITS;  // clear sign bit to make positive
            long result = sample % bound;
            if (sample - result + bound - 1 > 0) {  // confirm sample is from a complete bucket
                return result;
            }
        }
    }

    /**
     * Returns the next uniformly distributed long value from this generator between
     * {@code least} (inclusive) and {@code bound} (exclusive).
     *
     * @param least the lower bound (inclusive)
     * @param bound the upper bound (exclusive)
     * @return a random value chosen uniformly from the range {@code [least, bound)}
     * @throws IllegalArgumentException if least is not less than bound
     * @see java.util.concurrent.ThreadLocalRandom#nextLong(long, long)
     */
    public long nextLong(long least, long bound) {
        if (least >= bound) {
            throw new IllegalArgumentException("bound must be greater than least.");
        }
        return nextLong(bound - least) + least;
    }

    /**
     * Returns the next uniformly distributed float value from this generator between
     * {@code 0.0} (inclusive) and {@code 1.0} (exclusive) in units of {@code 2^-24}.
     *
     * @return a random value chosen uniformly from the range {@code [0.0, 1.0)}
     */
    @Override public float nextFloat() {
        return (spi.engineNextInt() & KEEP_24_BITS) / (float)(1 << 24);
    }

    /**
     * Returns the next uniformly distributed double value from this generator between
     * {@code 0.0} (inclusive) and {@code 1.0} (exclusive).
     *
     * @return a random value chosen uniformly from the range {@code [0.0, 1.0)}
     */
    @Override public double nextDouble() {
        return (spi.engineNextLong() & KEEP_53_BITS) / (double)(1L << 53);
    }

    /**
     * Returns the next uniformly distributed double value from this generator between
     * {@code least} (inclusive) and {@code bound} (exclusive).
     *
     * @param least the lower bound (inclusive)
     * @param bound the upper bound (exclusive)
     * @return a random value chosen uniformly from the range {@code [least, bound)}
     * @throws IllegalArgumentException if least is not less than bound
     * @see java.util.concurrent.ThreadLocalRandom#nextDouble(double, double)
     */
    public double nextDouble(double least, double bound) {
        if (least >= bound) {
            throw new IllegalArgumentException("bound must be greater than least.");
        }
        return nextDouble() * (bound - least) + least;
    }

    /**
     * Returns the next type 4 (random) {@link UUID} from this generator.
     *
     * @return a randomly generated UUID
     */
    public UUID nextUUID() {
        return new UUID(
            spi.engineNextLong() & 0xffffffffffff0fffL | 0x0000000000004000L,   // set version to 4
            spi.engineNextLong() & 0x3fffffffffffffffL | 0x8000000000000000L);  // set reserved bits per RFC4122
    }

    /**
     * Generates random bytes and places them into the specified array. The number
     * of random bytes generated is equal to the length of the array.
     *
     * @param bytes the array to fill with random bytes.
     * @throws IllegalArgumentException if {@code bytes} is null
     */
    @Override public void nextBytes(byte[] bytes) {
        spi.engineNextBytes(bytes);
    }


    private static final long serialVersionUID = 1;
}
