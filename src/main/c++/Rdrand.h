/**
 * 2013-01-24
 *
 * A utility class for invoking RDRAND instructions.
 *
 * @author Cameron Beccario
 */

#pragma once

// Standardize terminology between different compilers.
#if defined(_WIN64) || defined(_LP64)
    #define _X64
#else
    #define _X86
#endif

#if defined(_MSC_VER)
    #include <intrin.h>
    #include <immintrin.h>
//  #include <stdint.h>  // ?? does this work for vc?
    typedef __int8 int8_t;
    typedef __int16 int16_t;
    typedef __int32 int32_t;
    typedef __int64 int64_t;
    typedef unsigned __int8 uint8_t;
    typedef unsigned __int16 uint16_t;
    typedef unsigned __int32 uint32_t;
    typedef unsigned __int64 uint64_t;
#elif defined(__GNUC__)
    #include <stdint.h>
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
