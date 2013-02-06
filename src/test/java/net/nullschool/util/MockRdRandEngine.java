package net.nullschool.util;

/**
 * 2013-01-23<p/>
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */
class MockRdRandEngine extends DigitalRandomSpi {

    private static final long serialVersionUID = 1;


    private final long[] values;
    private int i;

    MockRdRandEngine(long... values) {
        this.values = values;
    }

    @Override
    protected void engineSetSeed(byte[] seed) {
        throw new UnsupportedOperationException("engineSetSeed");
    }

    private int next() {
        int r = i;
        i = (i + 1) % values.length;
        return r;
    }

    @Override
    protected int engineNextInt() {
        return (int)values[next()];
    }

    @Override
    protected long engineNextLong() {
        return values[next()];
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte)values[next()];
        }
    }

    @Override
    protected byte[] engineGenerateSeed(int length) {
        byte[] seed = new byte[length];
        engineNextBytes(seed);
        return seed;
    }
}
