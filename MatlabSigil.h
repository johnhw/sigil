/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class MatlabSigil */

#ifndef _Included_MatlabSigil
#define _Included_MatlabSigil
#ifdef __cplusplus
extern "C" {
#endif
#undef MatlabSigil_serialVersionUID
#define MatlabSigil_serialVersionUID 213i64
/* Inaccessible static: genColor */
#undef MatlabSigil_serialVersionUID
#define MatlabSigil_serialVersionUID 213i64
/*
 * Class:     MatlabSigil
 * Method:    initMatlab
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_MatlabSigil_initMatlab
  (JNIEnv *, jobject);

/*
 * Class:     MatlabSigil
 * Method:    pollMatlabNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_MatlabSigil_pollMatlabNative
  (JNIEnv *, jobject);

/*
 * Class:     MatlabSigil
 * Method:    getMatlabVector
 * Signature: ()[D
 */
JNIEXPORT jdoubleArray JNICALL Java_MatlabSigil_getMatlabVector
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
