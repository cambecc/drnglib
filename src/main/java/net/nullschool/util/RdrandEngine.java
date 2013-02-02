package net.nullschool.util;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.Key;

import static net.nullschool.util.EngineTools.*;

/**
 * 2012-12-08<p/>
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */
class RdrandEngine extends DigitalRandomSpi {

    private static volatile boolean isSupported;

    private static class NativeMethods {

        private static volatile boolean isLinked;

        private static native boolean isRdrandSupported();

        private static native int rdrand32(int retries) throws IllegalStateException;

        private static native long rdrand64(int retries) throws IllegalStateException;

        private static native void rdrandBytes(byte[] bytes, int retries) throws IllegalStateException;

        private synchronized static void link() throws IOException {
            if (!isLinked) {
                loadRdrandNativeLibrary();
                isLinked = true;
            }
        }
    }

    static boolean isSupported() throws IOException {
        NativeMethods.link();
        return NativeMethods.isRdrandSupported();
    }

    RdrandEngine() throws UnsupportedOperationException {
        if (!isSupported) {
            try {
                if (!(isSupported = isSupported())) {
                    throw new UnsupportedOperationException(
                        "Random number generation using rdrand is not supported by this CPU.");
                }
            }
            catch (Throwable t) {
                throw new UnsupportedOperationException(
                    "Random number generation using rdrand is not supported because engine initialization failed.",
                    t);
            }
        }
    }

    /**
     * Intel's DRNG implementation does not support setting a seed.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    protected void engineSetSeed(byte[] seed) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int engineNextInt() {
        return NativeMethods.rdrand32(10);
    }

    @Override
    protected long engineNextLong() {
        return NativeMethods.rdrand64(10);
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        NativeMethods.rdrandBytes(bytes, 10);
    }

    private byte[] nextBytes(byte[] bytes) {
        engineNextBytes(bytes);
        return bytes;
    }

    /**
     * {@inheritDoc}<p/>
     *
     * This implementation relies on the knowledge that Intel guarantees the hardware DRNG reseeds
     * itself at least every 1024 64-bit samples (8192 bytes), although in practice reseeding occurs
     * much more often. This method hashes a 8192 byte sample using SHA256 to generate 32 bytes of
     * distilled seed entropy. If more seed data is required, the process is repeated. The hash
     * function is initialized with a random 128-bit key as provided by {@link #engineNextBytes(byte[])}.
     */
    @Override
    protected byte[] engineGenerateSeed(int numBytes) {
        byte[] seed = new byte[numBytes];
        byte[] sample = new byte[8192];  // Large enough to guarantee hardware reseed.
        final Key key = new SecretKeySpec(nextBytes(new byte[16]), "None");

        int written = 0, size;
        while (written < seed.length) {
            // Hash a large amount of random data.
            byte[] hash = hashSHA256(key, nextBytes(sample));
            // Use the hash as seed data.
            System.arraycopy(hash, 0, seed, written, size = Math.min(hash.length, seed.length - written));
            // Repeat until we have enough seed data.
            written += size;
        }
        return seed;
    }


    private static final long serialVersionUID = 1;
}
