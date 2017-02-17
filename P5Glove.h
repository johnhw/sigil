/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class P5Glove */

#ifndef _Included_P5Glove
#define _Included_P5Glove
#ifdef __cplusplus
extern "C" {
#endif
#undef P5Glove_serialVersionUID
#define P5Glove_serialVersionUID 213i64
/* Inaccessible static: genColor */
#undef P5Glove_serialVersionUID
#define P5Glove_serialVersionUID 213i64
/*
 * Class:     P5Glove
 * Method:    initTracker
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_P5Glove_initTracker
  (JNIEnv *, jobject);

/*
 * Class:     P5Glove
 * Method:    pollTrackerNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_P5Glove_pollTrackerNative
  (JNIEnv *, jobject);

/*
 * Class:     P5Glove
 * Method:    getRoll
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_P5Glove_getRoll
  (JNIEnv *, jobject);

/*
 * Class:     P5Glove
 * Method:    getPitch
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_P5Glove_getPitch
  (JNIEnv *, jobject);

/*
 * Class:     P5Glove
 * Method:    getYaw
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_P5Glove_getYaw
  (JNIEnv *, jobject);

/*
 * Class:     P5Glove
 * Method:    getX
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_P5Glove_getX
  (JNIEnv *, jobject);

/*
 * Class:     P5Glove
 * Method:    getY
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_P5Glove_getY
  (JNIEnv *, jobject);

/*
 * Class:     P5Glove
 * Method:    getZ
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_P5Glove_getZ
  (JNIEnv *, jobject);

/*
 * Class:     P5Glove
 * Method:    getFinger
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_P5Glove_getFinger
  (JNIEnv *, jobject, jint);

/*
 * Class:     P5Glove
 * Method:    getButton
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_P5Glove_getButton
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif
