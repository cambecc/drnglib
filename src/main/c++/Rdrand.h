/**
 * 2013-01-24
 *
 * A utility class for invoking RDRAND instructions.
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */

#pragma once

#include <stdint.h>
#if defined(_MSC_VER)
    #include <intrin.h>
    #include <immintrin.h>
#endif

// Standardize terminology between different compilers.
#if defined(_WIN64) || defined(_LP64)
    #define _X64
#else
    #define _X86
#endif

class Rdrand {

public:

    const bool isSupported;

    Rdrand() : isSupported(checkSupported()) {
    }

    bool next32(uint32_t* result, int retries);

    bool next64(uint64_t* result, int retries);

    bool nextBytes(uint8_t* buffer, int length, int retries);

private:

    bool checkSupported();
};
