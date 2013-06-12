/**
 * 2013-02-03
 *
 * Implementation of the JNI entry points. An rdrand failure, which should be very
 * rare, is communicated to the calling Java program as an IllegalStateException.
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */

#include "RdRandEngine.h"

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

static const char* ILLEGAL_STATE_EXCEPTION = "java/lang/IllegalStateException";
static const char* ILLEGAL_ARGUMENT_EXCEPTION = "java/lang/IllegalArgumentException";
static const char* VALUE_NOT_AVAILABLE = "Random value unavailable.";
static const int MAX_ATTEMPTS = 10;

#if defined(__GNUC__)
    /**
     * GCC doesn't have a CPUID intrinsic, so we need to make one.
     */
    void __cpuid(int* result, int function) {
        __asm volatile(
            "cpuid" :
            "=a"(result[0]), "=b"(result[1]), "=c"(result[2]), "=d"(result[3]) :
            "a"(function));
    }
#endif

/**
 * Returns non-zero value if the processor supports the rdrand instruction.
 * See http://en.wikipedia.org/wiki/CPUID
 */
int checkSupported() {
    int info[4];
    __cpuid(info, 0);  // get vendor id
    if (info[1] == *(int*)"Genu" &&  // EBX
        info[3] == *(int*)"ineI" &&  // EDX
        info[2] == *(int*)"ntel") {  // ECX
        __cpuid(info, 1);  // get feature bits
        return (info[2] & 0x40000000) != 0;  // test ECX for rdrand support flag
    }
    return 0;
}

#if defined(__GNUC__)
    /**
     * Generate 32 bits of random data with rdrand. Returns 0 if the operation failed.
     *
     * GCC may be too old to understand rdrand, so manually specify it. Make
     * sure to return the carry flag to signify success.
     */
    int _rdrand32_step(uint32_t* result) {
        unsigned char success;
        __asm volatile(
            ".byte 0x0f; .byte 0xc7; .byte 0xf0; setc %1" :
            "=a"(*result), "=qm"(success));
        return success;
    }
#endif

/**
 * Generate 32 bits of random data. Upon failure, retry the operation several times
 * before giving up.
 */
int rdrand32(uint32_t* result) {
    int success;
    int attempts = MAX_ATTEMPTS;
    while (!(success = _rdrand32_step(result)) && --attempts != 0) {
    }
    return success;
}

#if defined(_X86)
    /**
     * Generate 64 bits of random data with rdrand. Returns 0 if the operation failed.
     *
     * Simulate a 64-bit rdrand invocation on a 32-bit platform by invoking 32-bit rdrand
     * twice and combining the results.
     */
    int _rdrand64_step(uint64_t* result) {
        uint32_t* halves = (uint32_t*)result;
        int success = _rdrand32_step(&halves[0]) & _rdrand32_step(&halves[1]);
        if (!success) {
            halves[0] = halves[1] = 0;  // We failed, so zero the result as 64-bit rdrand would have.
        }
        return success;
    }
#elif defined(__GNUC__)
    /**
     * Generate 64 bits of random data with rdrand. Returns 0 if the operation failed.
     *
     * GCC may be too old to understand rdrand, so manually specify it. Make
     * sure to return the carry flag to signify success.
     */
    int _rdrand64_step(uint64_t* result) {
        unsigned char success;
        __asm volatile(
            ".byte 0x48; .byte 0x0f; .byte 0xc7; .byte 0xf0; setc %1" :
            "=a"(*result), "=qm"(success));
        return success;
    }
#endif

/**
 * Generate 64 bits of random data. Upon failure, retry the operation several times
 * before giving up.
 */
int rdrand64(uint64_t* result) {
    int success;
    int attempts = MAX_ATTEMPTS;
    while (!(success = _rdrand64_step(result)) && --attempts != 0) {
    }
    return success;
}

/**
 * Align the specified pointer down to the nearest byte boundary specified by "alignment".
 */
uint8_t* align_floor(uint8_t* p, int alignment) {
    int remainder = ((uintptr_t)p) % alignment;
    return p - remainder;
}

/**
 * Align the specified pointer up to the nearest byte boundary specified by "alignment".
 */
uint8_t* align_ceiling(uint8_t* p, int alignment) {
    return align_floor(p + (alignment - 1), alignment);
}

/**
 * From cursor to end (exclusive), fill with random bytes from one invocation of 32-bit rdrand.
 */
int rdrand32_fill(uint8_t* cursor, uint8_t* end) {
    if (cursor < end) {
        uint32_t value;
        if (!rdrand32(&value)) {
            return 0;
        }
        do {
            *cursor = (uint8_t)value;
            value >>= 8;
        } while (++cursor < end);
    }
    return 1;
}

/**
 * From cursor to end (exclusive), fill with random bytes from one invocation of 64-bit rdrand.
 */
int rdrand64_fill(uint8_t* cursor, uint8_t* end) {
    if (cursor < end) {
        uint64_t value;
        if (!rdrand64(&value)) {
            return 0;
        }
        do {
            *cursor = (uint8_t)value;
            value >>= 8;
        } while (++cursor < end);
    }
    return 1;
}

/**
 * Fill the specified buffer with "length" random bytes. For performance, we fill the buffer
 * either 32-bits (x86) or 64-bits (x64) at a time on appropriately aligned boundaries. For
 * the residual unaligned bytes either at the front or back of the buffer, if any, we fill
 * them byte-by-byte. Example with eight-byte (x64) alignment:
 *
 *       0       8      16      24      32
 *       |       |       |       |       |
 *         xxxxxxAAAAAAAABBBBBBBBxxxxxx
 *         ^     ^               ^     ^
 *     start  aligned         aligned  end
 *             start            end
 *
 * Aligned blocks "A" and "B" are filled eight bytes at a time. Residual bytes not belonging
 * to an aligned block, marked "x", are filled byte-by-byte.
 */
int rdrandFill(uint8_t* buffer, int length) {
    #if defined(_X86)
        #define ALIGNMENT 4  // fill buffer four bytes at a time
        uint8_t* start = buffer;
        uint8_t* end = start + length;
        uint8_t* alignedStart = align_ceiling(start, ALIGNMENT);
        uint8_t* alignedEnd = align_floor(end, ALIGNMENT);
        uint8_t* cursor;

        // Fill leading residual bytes, if any.
        if (!rdrand32_fill(start, alignedStart)) {
            return 0;
        }

        // Now fill aligned blocks.
        for (cursor = alignedStart; cursor < alignedEnd; cursor += ALIGNMENT) {
            if (!rdrand32((uint32_t*)cursor)) {
                return 0;
            }
        }

        // Fill trailing residual bytes, if any.
        return rdrand32_fill(alignedEnd, end);

    #else
        #define ALIGNMENT 8  // fill buffer eight bytes at a time
        uint8_t* start = buffer;
        uint8_t* end = start + length;
        uint8_t* alignedStart = align_ceiling(start, ALIGNMENT);
        uint8_t* alignedEnd = align_floor(end, ALIGNMENT);
        uint8_t* cursor;

        // Fill leading residual bytes, if any.
        if (!rdrand64_fill(start, alignedStart)) {
            return 0;
        }

        // Now fill aligned blocks.
        for (cursor = alignedStart; cursor < alignedEnd; cursor += ALIGNMENT) {
            if (!rdrand64((uint64_t*)cursor)) {
                return 0;
            }
        }

        // Fill trailing residual bytes, if any.
        return rdrand64_fill(alignedEnd, end);

    #endif
}

jint throwNew(JNIEnv* env, const char* className, const char* message) {
    return (*env)->ThrowNew(env, (*env)->FindClass(env, className), message);
}

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {
    return JNI_VERSION_1_6;
}

JNIEXPORT jboolean JNICALL
Java_net_nullschool_util_RdRandEngine_isRdRandSupported(JNIEnv* env, jclass clazz) {
    return checkSupported();
}

JNIEXPORT jint JNICALL
Java_net_nullschool_util_RdRandEngine_engineNextInt(JNIEnv* env, jobject obj) {
    uint32_t result;
    if (!rdrand32(&result)) {
        throwNew(env, ILLEGAL_STATE_EXCEPTION, VALUE_NOT_AVAILABLE);
    }
    return result;
}

JNIEXPORT jlong JNICALL
Java_net_nullschool_util_RdRandEngine_engineNextLong(JNIEnv* env, jobject obj) {
    uint64_t result;
    if (!rdrand64(&result)) {
        throwNew(env, ILLEGAL_STATE_EXCEPTION, VALUE_NOT_AVAILABLE);
    }
    return result;
}

JNIEXPORT void JNICALL
Java_net_nullschool_util_RdRandEngine_engineNextBytes(JNIEnv* env, jobject obj, jbyteArray bytes) {
    jbyte* buffer;
    jsize length;

    if (bytes == NULL) {
        throwNew(env, ILLEGAL_ARGUMENT_EXCEPTION, "null byte array.");
        return;
    }

    // CONSIDER: GetByteArrayElements seems to always copy. Maybe that's okay even if input array is 100MB.
    //           But perhaps it would be better to fill the java array in chunks using SetByteArrayRegion, to
    //           avoid a huge array copy.
    length = (*env)->GetArrayLength(env, bytes);
    buffer = (*env)->GetByteArrayElements(env, bytes, NULL);
    if (buffer == NULL) {
        throwNew(env, ILLEGAL_ARGUMENT_EXCEPTION, "Failed to get byte array.");
        return;
    }

    if (!rdrandFill((uint8_t*)buffer, length)) {
        throwNew(env, ILLEGAL_STATE_EXCEPTION, VALUE_NOT_AVAILABLE);
    }

    (*env)->ReleaseByteArrayElements(env, bytes, buffer, 0);
}
