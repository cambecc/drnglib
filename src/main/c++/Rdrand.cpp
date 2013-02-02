/**
 * 2013-01-24
 *
 * Implementation of the Rdrand class. Note the multiple definitions required
 * for some methods due to inconsistent RDRAND support among compilers. Tested
 * on GCC 4.2.1 and MSVC 11.0.
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */

#include "Rdrand.h"

struct CPUIdInfo {
    uint32_t EAX;
    uint32_t EBX;
    uint32_t ECX;
    uint32_t EDX;
};

/**
 * Invoke the CPUID instruction for the given function.
 */
CPUIdInfo cpuid(int32_t function) {
    CPUIdInfo result;
    #if defined(__GNUC__)
        asm volatile(
            "cpuid" :
            "=a"(result.EAX), "=b"(result.EBX), "=c"(result.ECX), "=d"(result.EDX) :
            "a"(function));
    #else
        __cpuid((int*)&result, function);
    #endif
    return result;
}

/**
 * Returns true if the processor supports the RDRAND instruction.
 * See http://en.wikipedia.org/wiki/CPUID
 */
bool Rdrand::checkSupported() {
    CPUIdInfo info = cpuid(0);  // get vendor id
    return
        info.EBX == *(uint32_t*)"Genu" &&
        info.EDX == *(uint32_t*)"ineI" &&
        info.ECX == *(uint32_t*)"ntel" &&
        cpuid(1).ECX & 0x40000000;  // test feature bits for rdrand support flag
}

#if defined(__GNUC__)
    /**
     * GCC may be too old to understand RDRAND, so manually specify it. Make
     * sure to return the carry flag to signify success.
     */
    int _rdrand32_step(uint32_t* result) {
        unsigned char success;
        asm volatile(
            ".byte 0x0f; .byte 0xc7; .byte 0xf0; setc %1" :
            "=a"(*result), "=qm"(success));
        return success;
    }
#endif

#if defined(_X86)
    /**
     * Simulate a 64-bit RDRAND invocation on a 32-bit platform by invoking 32-bit RDRAND
     * twice and combining the results.
     */
    int _rdrand64_step(uint64_t* result) {
        uint32_t* halves = (uint32_t*)result;
        if (_rdrand32_step(&halves[0]) && _rdrand32_step(&halves[1])) {
            return 1;
        }
        // We failed, so zero the result, just like 64-bit RDRAND would have.
        *result = 0;
        return 0;
    }
#elif defined(__GNUC__)
    /**
     * GCC may be too old to understand RDRAND, so manually specify it. Make
     * sure to return the carry flag to signify success.
     */
    int _rdrand64_step(uint64_t* result) {
        unsigned char success;
        asm volatile(
            ".byte 0x48; .byte 0x0f; .byte 0xc7; .byte 0xf0; setc %1" :
            "=a"(*result), "=qm"(success));
        return success;
    }
#endif

/**
 * Generate a 32-bit random value, returning true if successful and retrying up
 * to the specified number of times upon RDRAND failure.
 */
bool Rdrand::next32(uint32_t* result, int retries) {
    if (!isSupported) {
        *result = 0;
        return false;
    }
    while (!_rdrand32_step(result)) {
        if (retries-- <= 0) {
            return false;
        }
    }
    return true;
}

/**
 * Generate a 64-bit random value, returning true if successful and retrying up
 * to the specified number of times upon RDRAND failure.
 */
bool Rdrand::next64(uint64_t* result, int retries) {
    if (!isSupported) {
        *result = 0;
        return false;
    }
    while (!_rdrand64_step(result)) {
        if (retries-- <= 0) {
            return false;
        }
    }
    return true;
}

/**
 * Fill the specified buffer with random bytes, returning true if successful and,
 * in the case of failure, retrying up to the specified number of times upon each
 * RDRAND invocation attempt.
 */
bool Rdrand::nextBytes(uint8_t* bytes, int length, int retries) {
    if (!isSupported) {
        return false;
    }
    // UNDONE: improve this implementation that naively copies byte-for-byte.
    int i = 0;
    while (i < length) {
        int remainingRetries = retries;
        uint64_t value;
        while (!_rdrand64_step(&value)) {
            if (remainingRetries-- <= 0) {
                return false;
            }
        }
        for (int x = 0; x < 8 && i < length; x++, i++) {
            bytes[i] = ((uint8_t*)&value)[x];
        }
    }
    return true;
}
