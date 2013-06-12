package net.nullschool.util;

import java.security.SecureRandomSpi;


/**
 * 2013-01-25<p/>
 *
 * An extension of {@link SecureRandomSpi} to provide more methods to instances of
 * {@link DigitalRandom} for generating random data.<p/>
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */
abstract class DigitalRandomSpi extends SecureRandomSpi {

    /**
     * Generates a random int.
     *
     * @return a 32-bit random value.
     */
    protected abstract int engineNextInt();

    /**
     * Generates a random long.
     *
     * @return a 64-bit random value.
     */
    protected abstract long engineNextLong();

    /**
     * Fills the specified array with random bytes.
     *
     * @param bytes the array to fill with random data.
     */
    @Override protected abstract void engineNextBytes(byte[] bytes);

    /**
     * Returns a byte array of the specified length filled with seed-quality
     * random data.
     *
     * @param length the length of the seed to generate, in bytes.
     * @return the seed
     */
    @Override protected abstract byte[] engineGenerateSeed(int length);


    private static final long serialVersionUID = 1;
}
