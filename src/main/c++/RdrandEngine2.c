/**
 * 2013-02-03
 *
 * Implementation of the JNI entry points. An RDRAND failure, which should be exceedingly
 * rare, is communicated to the calling Java program as an IllegalStateException.
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */

#include "RdrandEngine2.h"

#include <stdint.h>
#include <stdbool.h>
#if defined(_MSC_VER)
    #include <intrin.h>
    #include <immintrin.h>
#endif

/* Standardize terminology between different compilers. */
#if defined(_WIN64) || defined(_LP64)
    #define _X64
#else
    #define _X86
#endif

static const char* ILLEGAL_STATE_EXCEPTION = "java/lang/IllegalStateException";
static const char* ILLEGAL_ARGUMENT_EXCEPTION = "java/lang/IllegalArgumentException";
static const char* VALUE_NOT_AVAILABLE = "Random value unavailable.";
static bool isSupported = false;

#if defined(__GNUC__)
    void __cpuid(uint32_t* result, int function) {
        __asm volatile(
            "cpuid" :
            "=a"(result[0]), "=b"(result[1]), "=c"(result[2]), "=d"(result[3]) :
            "a"(function));
    }
#endif

/**
 * Returns true if the processor supports the RDRAND instruction.
 * See http://en.wikipedia.org/wiki/CPUID
 */
bool checkSupported() {
    uint32_t info[4];
    __cpuid(info, 0);  // get vendor id
    if (info[1] == *(uint32_t*)"Genu" &&  // EBX
        info[3] == *(uint32_t*)"ineI" &&  // EDX
        info[2] == *(uint32_t*)"ntel") {  // ECX
        __cpuid(info, 1);  // get feature bits
        return info[2] & 0x40000000;  // test ECX for rdrand support flag
    }
    return false;
}

#if defined(__GNUC__)
    /**
     * GCC may be too old to understand RDRAND, so manually specify it. Make
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
        /* We failed, so zero the result, just like 64-bit RDRAND would have. */
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
        __asm volatile(
            ".byte 0x48; .byte 0x0f; .byte 0xc7; .byte 0xf0; setc %1" :
            "=a"(*result), "=qm"(success));
        return success;
    }
#endif

jint throwNew(JNIEnv* env, const char* clazz, const char* message) {
    return (*env)->ThrowNew(env, (*env)->FindClass(env, clazz), message);
}

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {
    isSupported = checkSupported();
    return JNI_VERSION_1_6;
}

JNIEXPORT jboolean JNICALL
Java_net_nullschool_util_RdrandEngine2_isRdrandSupported(JNIEnv* env, jclass clazz) {
    return isSupported;
}

//JNIEXPORT jint JNICALL
//Java_net_nullschool_util_RdrandEngine2_engineNextInt(JNIEnv* env, jobject obj) {
//    int attempts = 10;
//    uint32_t result;
//    while (!isSupported || !_rdrand32_step(&result)) {
//        if (--attempts <= 0) {
//            result = 0;
//            throwNew(env, ILLEGAL_STATE_EXCEPTION, VALUE_NOT_AVAILABLE);
//            break;
//        }
//    }
//    return result;
//}

JNIEXPORT jint JNICALL
Java_net_nullschool_util_RdrandEngine2_engineNextInt(JNIEnv* env, jobject obj) {
    if (isSupported) {
        for (int attempts = 0; attempts < 10; attempts++) {
            uint32_t result;
            if (_rdrand32_step(&result)) {
                return result;
            }
        }
    }
    throwNew(env, ILLEGAL_STATE_EXCEPTION, VALUE_NOT_AVAILABLE);
    return 0;
}

JNIEXPORT jlong JNICALL
Java_net_nullschool_util_RdrandEngine2_engineNextLong(JNIEnv* env, jobject obj) {
    int attempts = 10;
    uint64_t result;
    while (!isSupported || !_rdrand64_step(&result)) {
        if (--attempts <= 0) {
            result = 0;
            throwNew(env, ILLEGAL_STATE_EXCEPTION, VALUE_NOT_AVAILABLE);
            break;
        }
    }
    return result;
}

JNIEXPORT void JNICALL
Java_net_nullschool_util_RdrandEngine2_engineNextBytes(JNIEnv* env, jobject obj, jbyteArray bytes) {
    if (!isSupported) {
        throwNew(env, ILLEGAL_STATE_EXCEPTION, VALUE_NOT_AVAILABLE);
        return;
    }
    if (bytes == NULL) {
        throwNew(env, ILLEGAL_ARGUMENT_EXCEPTION, NULL);
        return;
    }
    jbyte* buffer = (*env)->GetByteArrayElements(env, bytes, NULL);  // UNDONE: result may be null
    int length = (*env)->GetArrayLength(env, bytes);
    int i = 0;
    while (i < length) {
        int attempts = 10;
        uint64_t value;
        while (!_rdrand64_step(&value)) {
            if (--attempts <= 0) {
                throwNew(env, ILLEGAL_STATE_EXCEPTION, VALUE_NOT_AVAILABLE);
                (*env)->ReleaseByteArrayElements(env, bytes, buffer, 0);
                return;
            }
        }
        int x;
        for (x = 0; x < 8 && i < length; x++, i++) {
            ((uint8_t*)buffer)[i] = ((uint8_t*)&value)[x];
        }
    }

    (*env)->ReleaseByteArrayElements(env, bytes, buffer, 0);
    return;
/*
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
*/
    /*
    // not possible for copy to be made when assigning into the JVM's array
Set<PrimitiveType>ArrayRegion Routines
void Set<PrimitiveType>ArrayRegion(JNIEnv *env, ArrayType array,
jsize start, jsize len, const NativeType *buf);

    // may copy the entire array, but chances are it will not. but dangerous.
GetPrimitiveArrayCritical
ReleasePrimitiveArrayCritical
void * GetPrimitiveArrayCritical(JNIEnv *env, jarray array, jboolean *isCopy);
void ReleasePrimitiveArrayCritical(JNIEnv *env, jarray array, void *carray, jint mode);
    */
}
