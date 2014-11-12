/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class lib_module_soundtouch_NativeSoundTouch */

#ifndef _Included_lib_module_soundtouch_NativeSoundTouch
#define _Included_lib_module_soundtouch_NativeSoundTouch
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     lib_module_soundtouch_NativeSoundTouch
 * Method:    soundTouchCreate
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_lib_module_soundtouch_NativeSoundTouch_soundTouchCreate(
		JNIEnv *, jobject);

/*
 * Class:     lib_module_soundtouch_NativeSoundTouch
 * Method:    soundTouchDestory
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_lib_module_soundtouch_NativeSoundTouch_soundTouchDestory
(JNIEnv *, jobject);

/*
 * Class:     lib_module_soundtouch_NativeSoundTouch
 * Method:    soundTouchgethVersion
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_lib_module_soundtouch_NativeSoundTouch_soundTouchgethVersion(
		JNIEnv *, jobject);

/*
 * Class:     lib_module_soundtouch_NativeSoundTouch
 * Method:    setPitchSemiTones
 * Signature: (D)V
 */
JNIEXPORT void JNICALL Java_lib_module_soundtouch_NativeSoundTouch_setPitchSemiTones
  (JNIEnv *, jobject, jfloat);

/*
 * Class:     lib_module_soundtouch_NativeSoundTouch
 * Method:    setTempoChange
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_lib_module_soundtouch_NativeSoundTouch_setTempoChange
  (JNIEnv *, jobject, jfloat);

/*
 * Class:     lib_module_soundtouch_NativeSoundTouch
 * Method:    shiftingPitch
 * Signature: ([BII)V
 */
JNIEXPORT void JNICALL Java_lib_module_soundtouch_NativeSoundTouch_shiftingPitch
(JNIEnv *, jobject, jbyteArray, jint, jint);

/*
 * Class:     lib_module_soundtouch_NativeSoundTouch
 * Method:    receiveSamples
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_lib_module_soundtouch_NativeSoundTouch_receiveSamples
  (JNIEnv *, jobject, jbyteArray, jint);

/*
 * Class:     lib_module_soundtouch_NativeSoundTouch
 * Method:    soundTouchFlushLastSamples
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_lib_module_soundtouch_NativeSoundTouch_soundTouchFlushLastSamples
  (JNIEnv *, jobject);



#ifdef __cplusplus
}
#endif
#endif
