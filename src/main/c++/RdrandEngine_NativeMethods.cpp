/**
 * 2013-01-24
 *
 * Implementation of the JNI entry points. An RDRAND failure, which should be exceedingly
 * rare, is communicated to the calling Java program as an IllegalStateException.
 *
 * Released to the public domain: http://creativecommons.org/publicdomain/zero/1.0/
 *
 * @author Cameron Beccario
 */

#include "RdrandEngine_NativeMethods.h"
#include "Rdrand.h"

static const char* VALUE_NOT_AVAILABLE = "Random value unavailable.";
static Rdrand rdrand;

JNIEXPORT jboolean JNICALL
Java_net_nullschool_util_RdrandEngine_00024NativeMethods_isRdrandSupported(
    JNIEnv* env,
    jclass clazz) {

    return rdrand.isSupported;
}

JNIEXPORT jint JNICALL
Java_net_nullschool_util_RdrandEngine_00024NativeMethods_rdrand32(
    JNIEnv* env,
    jclass clazz,
    jint retries) {

    uint32_t result;
    if (!rdrand.next32(&result, retries)) {
        env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), VALUE_NOT_AVAILABLE);
    }
    return result;
}

JNIEXPORT jlong JNICALL
Java_net_nullschool_util_RdrandEngine_00024NativeMethods_rdrand64(
    JNIEnv* env,
    jclass clazz,
    jint retries) {

    uint64_t result;
    if (!rdrand.next64(&result, retries)) {
        env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), VALUE_NOT_AVAILABLE);
    }
    return result;
}

JNIEXPORT void JNICALL
Java_net_nullschool_util_RdrandEngine_00024NativeMethods_rdrandBytes(
    JNIEnv* env,
    jclass clazz,
    jbyteArray bytes,
    jint retries) {

    if (bytes == NULL) {
        env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), NULL);
        return;
    }
    jbyte* buffer = env->GetByteArrayElements(bytes, NULL);
    if (!rdrand.nextBytes((uint8_t*)buffer, env->GetArrayLength(bytes), retries)) {
        env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), VALUE_NOT_AVAILABLE);
    }
    env->ReleaseByteArrayElements(bytes, buffer, 0);
    return;
}
