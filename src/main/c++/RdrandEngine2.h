/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class net_nullschool_util_RdrandEngine2 */

#ifndef _Included_net_nullschool_util_RdrandEngine2
#define _Included_net_nullschool_util_RdrandEngine2
#ifdef __cplusplus
extern "C" {
#endif
#undef net_nullschool_util_RdrandEngine2_serialVersionUID
#define net_nullschool_util_RdrandEngine2_serialVersionUID -2991854161009191830LL
#undef net_nullschool_util_RdrandEngine2_serialVersionUID
#define net_nullschool_util_RdrandEngine2_serialVersionUID 1LL
#undef net_nullschool_util_RdrandEngine2_serialVersionUID
#define net_nullschool_util_RdrandEngine2_serialVersionUID 1LL
/*
 * Class:     net_nullschool_util_RdrandEngine2
 * Method:    isRdrandSupported
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_net_nullschool_util_RdrandEngine2_isRdrandSupported
  (JNIEnv *, jclass);

/*
 * Class:     net_nullschool_util_RdrandEngine2
 * Method:    engineNextInt
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_nullschool_util_RdrandEngine2_engineNextInt
  (JNIEnv *, jobject);

/*
 * Class:     net_nullschool_util_RdrandEngine2
 * Method:    engineNextLong
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_net_nullschool_util_RdrandEngine2_engineNextLong
  (JNIEnv *, jobject);

/*
 * Class:     net_nullschool_util_RdrandEngine2
 * Method:    engineNextBytes
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_net_nullschool_util_RdrandEngine2_engineNextBytes
  (JNIEnv *, jobject, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
