#include <JavaScriptCore/JSValueRef.h>
#include <JavaScriptCore/JavaScript.h>
#include "JSUtils.h"

JSC_NATIVE_METHOD(JSValue, jint, NJSgetType)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    return (jint) JSValueGetType((JSContextRef) contextRef, (JSValueRef) valueRef);
}

JSC_NATIVE_METHOD(JSValue, jboolean, NJSisUndefined)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    return (jboolean) JSValueIsUndefined((JSContextRef) contextRef, (JSValueRef) valueRef);
}

JSC_NATIVE_METHOD(JSValue, jboolean, NJSisNull)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    return (jboolean) JSValueIsNull((JSContextRef) contextRef, (JSValueRef) valueRef);
}

JSC_NATIVE_METHOD(JSValue, jboolean, NJSisBoolean)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    return (jboolean) JSValueIsBoolean((JSContextRef) contextRef, (JSValueRef) valueRef);
}

JSC_NATIVE_METHOD(JSValue, jboolean, NJSisNumber)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    return (jboolean) JSValueIsNumber((JSContextRef) contextRef, (JSValueRef) valueRef);
}

JSC_NATIVE_METHOD(JSValue, jboolean, NJSisString)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    return (jboolean) JSValueIsString((JSContextRef) contextRef, (JSValueRef) valueRef);
}

JSC_NATIVE_METHOD(JSValue, jboolean, NJSisObject)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    return (jboolean) JSValueIsObject((JSContextRef) contextRef, (JSValueRef) valueRef);
}

JSC_NATIVE_METHOD(JSValue, jboolean, NJSisArray)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    return (jboolean) JSValueIsArray((JSContextRef) contextRef, (JSValueRef) valueRef);
}

JSC_NATIVE_METHOD(JSValue, jboolean, NJSisDate)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    return (jboolean) JSValueIsDate((JSContextRef) contextRef, (JSValueRef) valueRef);
}

/* Comparing values */

JSC_NATIVE_METHOD(JSValue, jobject, NJSisEqual)(JSC_NATIVE_PARAMS, jlong contextRef, jlong a, jlong b) {
    JSValueRef exception = NULL;

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret, "<init>", "()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret, "bool", "Z");

    bool bret = JSValueIsEqual((JSContextRef) contextRef, (JSValueRef) a, (JSValueRef) b,
                               &exception);

    env->SetBooleanField(out, fid, bret);

    fid = env->GetFieldID(ret, "exception", "J");
    env->SetLongField(out, fid, (long) exception);

    return out;
}

JSC_NATIVE_METHOD(JSValue, jboolean, NJSisStrictEqual)(JSC_NATIVE_PARAMS, jlong contextRef, jlong a, jlong b) {
    return (jboolean) JSValueIsStrictEqual((JSContextRef) contextRef, (JSValueRef) a, (JSValueRef) b);
}

JSC_NATIVE_METHOD(JSValue, jobject, NJSisInstanceOfConstructor)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef,
                                                  jlong constructor) {
    JSValueRef exception = NULL;

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret, "<init>", "()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret, "bool", "Z");

    bool bret = JSValueIsInstanceOfConstructor((JSContextRef) contextRef, (JSValueRef) valueRef,
                                               (JSObjectRef) constructor, &exception);

    env->SetBooleanField(out, fid, bret);

    fid = env->GetFieldID(ret, "exception", "J");
    env->SetLongField(out, fid, (long) exception);

    return out;
}

/* Creating values */

JSC_NATIVE_METHOD(JSValue, jlong, NJSmakeUndefined)(JSC_NATIVE_PARAMS, jlong ctx) {
    JSValueRef value = JSValueMakeUndefined((JSContextRef) ctx);
    JSValueProtect((JSContextRef) ctx, value);
    return (long) value;
}

JSC_NATIVE_METHOD(JSValue, jlong, NJSmakeNull)(JSC_NATIVE_PARAMS, jlong ctx) {
    JSValueRef value = JSValueMakeNull((JSContextRef) ctx);
    JSValueProtect((JSContextRef) ctx, value);
    return (long) value;
}

JSC_NATIVE_METHOD(JSValue, jlong, NJSmakeBoolean)(JSC_NATIVE_PARAMS, jlong ctx, jboolean boolean) {
    JSValueRef value = JSValueMakeBoolean((JSContextRef) ctx, (bool) boolean);
    JSValueProtect((JSContextRef) ctx, value);
    return (long) value;
}

JSC_NATIVE_METHOD(JSValue, jlong, NJSmakeNumber)(JSC_NATIVE_PARAMS, jlong ctx, jdouble number) {
    JSValueRef value = JSValueMakeNumber((JSContextRef) ctx, (double) number);
    JSValueProtect((JSContextRef) ctx, value);
    return (long) value;
}

JSC_NATIVE_METHOD(JSValue, jlong, NJSmakeString)(JSC_NATIVE_PARAMS, jlong ctx, jlong stringRef) {
    JSValueRef value = JSValueMakeString((JSContextRef) ctx, (JSStringRef) stringRef);
    JSValueProtect((JSContextRef) ctx, value);
    return (long) value;
}

/* Converting to and from JSON formatted strings */

JSC_NATIVE_METHOD(JSValue, jlong, NJSmakeFromJSONString)(JSC_NATIVE_PARAMS, jlong ctx, jlong stringRef) {
    JSValueRef value = JSValueMakeFromJSONString((JSContextRef) ctx, (JSStringRef) stringRef);
    JSValueProtect((JSContextRef) ctx, value);
    return (long) value;
}
/*
JSC_NATIVE_METHOD(JSValue, jobject, NJScreateJSONString)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef, jint indent) {
    JSValueRef exception = NULL;

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret, "<init>", "()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret, "reference", "J");
    JSStringRef value = JSValueCreateJSONString(
            (JSContextRef) contextRef,
            (JSValueRef) valueRef,
            (unsigned) indent,
            &exception);
    if (value)
        value = JSStringRetain(value);

    env->SetLongField(out, fid, (long) value);

    fid = env->GetFieldID(ret, "exception", "J");
    env->SetLongField(out, fid, (long) exception);

    return out;
}*/

/* Converting to primitive values */

JSC_NATIVE_METHOD(JSValue, jboolean, NJStoBoolean)(JSC_NATIVE_PARAMS, jlong ctx, jlong valueRef) {
    return (jboolean) JSValueToBoolean((JSContextRef) ctx, (JSValueRef) valueRef);
}

JSC_NATIVE_METHOD(JSValue, jobject, NJStoNumber)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    JSValueRef exception = NULL;

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret, "<init>", "()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret, "number", "D");

    jdouble dret = JSValueToNumber((JSContextRef) contextRef, (JSValueRef) valueRef, &exception);

    env->SetDoubleField(out, fid, dret);

    fid = env->GetFieldID(ret, "exception", "J");
    env->SetLongField(out, fid, (long) exception);

    return out;
}

JSC_NATIVE_METHOD(JSValue, jobject, NJStoStringCopy)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    JSValueRef exception = NULL;

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret, "<init>", "()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret, "reference", "J");

    JSStringRef string = JSValueToStringCopy((JSContextRef) contextRef, (JSValueRef) valueRef, &exception);
    if (string)
        string = JSStringRetain(string);

    env->SetLongField(out, fid, (long) string);

    fid = env->GetFieldID(ret, "exception", "J");
    env->SetLongField(out, fid, (long) exception);

    return out;
}

JSC_NATIVE_METHOD(JSValue, jobject, NJStoObject)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    JSValueRef exception = NULL;

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret, "<init>", "()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret, "reference", "J");

    JSObjectRef value = JSValueToObject((JSContextRef) contextRef, (JSValueRef) valueRef, &exception);
    JSValueProtect((JSContextRef) contextRef, value);

    env->SetLongField(out, fid, (long) value);

    fid = env->GetFieldID(ret, "exception", "J");
    env->SetLongField(out, fid, (long) exception);

    return out;
}

/* Garbage collection */

JSC_NATIVE_METHOD(JSValue, void, NJSprotect)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    JSValueProtect((JSContextRef) contextRef, (JSValueRef) valueRef);
}

JSC_NATIVE_METHOD(JSValue, void, NJSunprotect)(JSC_NATIVE_PARAMS, jlong contextRef, jlong valueRef) {
    JSValueUnprotect((JSContextRef) contextRef, (JSValueRef) valueRef);
}

JSC_NATIVE_METHOD(JSValue, void, NJSsetException)(JSC_NATIVE_PARAMS, jlong valueRef, jlong exceptionRefRef) {
    JSValueRef *exception = (JSValueRef *) exceptionRefRef;
    *exception = (JSValueRef) valueRef;
}
//extern "C"
//JNIEXPORT jlong JNICALL
//Java_io_nodekit_engine_javascriptcore_JSContext_getGroup__J(JNIEnv *env, jobject instance, jlong ctx) {
//}