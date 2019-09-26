#ifndef JNI_JSUTILS_H
#define JNI_JSUTILS_H

#include <stdlib.h>
#include <jni.h>
#include <android/log.h>


#define JSC_NATIVE_METHOD(javaClassName, returnType, methodName) extern "C" JNIEXPORT returnType JNICALL Java_io_nodekit_engine_javascriptcore_##javaClassName##_##methodName
#define JSC_NATIVE_PARAMS JNIEnv *env, jobject thiz

#endif