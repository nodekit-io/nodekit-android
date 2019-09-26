#include <JavaScriptCore/JavaScript.h>
#include "JSFunction.h"
#include "JSUtils.h"


JSC_NATIVE_METHOD(JSObject,jlong,NJSmake) (JSC_NATIVE_PARAMS, jlong ctx, jlong data) {
    JSObjectRef value = JSObjectMake((JSContextRef)ctx, (JSClassRef) NULL, (void*)data);
    JSValueProtect((JSContextRef) ctx, value);
    return (long)value;
}

JSC_NATIVE_METHOD(JSObject,jlong,NJSmakeInstance) (JSC_NATIVE_PARAMS, jlong ctx) {
    JSInstance *instance = new JSInstance(env, thiz, (JSContextRef)ctx);
    return instance->getObjRef();
}

JSC_NATIVE_METHOD(JSObject,jlong,NJSmakeFunctionWithCallback) (JSC_NATIVE_PARAMS, jlong ctx, jlong name) {
    JSFunction *function = new JSFunction(env, thiz, (JSContextRef)ctx,
                                          (JSStringRef)name);
    return function->getObjRef();
}

JSC_NATIVE_METHOD(JSObject,jobject,NJSmakeArray) (JSC_NATIVE_PARAMS, jlong ctx, jlongArray args) {
    JSValueRef exception = NULL;

    int i;
    jsize len = env->GetArrayLength(args);
    jlong *values = env->GetLongArrayElements(args, 0);
    JSValueRef* elements = new JSValueRef[len];
    for (i=0; i<len; i++) {
        elements[i] = (JSValueRef) values[i];
    }
    env->ReleaseLongArrayElements(args, values, 0);

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");

    JSObjectRef objRef = JSObjectMakeArray((JSContextRef)ctx, (size_t)len, (len==0)?NULL:elements,
                                           &exception);
    JSValueProtect((JSContextRef) ctx, objRef);

    env->SetLongField( out, fid, (jlong)objRef );

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    delete [] elements;
    return out;
}

JSC_NATIVE_METHOD(JSObject,jobject,NJSmakeDate) (JSC_NATIVE_PARAMS, jlong ctx, jlongArray args) {
    JSValueRef exception = NULL;

    int i;
    jsize len = env->GetArrayLength(args);
    jlong *values = env->GetLongArrayElements(args, 0);
    JSValueRef* elements = new JSValueRef[len];
    for (i=0; i<len; i++) {
        elements[i] = (JSValueRef) values[i];
    }
    env->ReleaseLongArrayElements(args, values, 0);

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");

    JSObjectRef objRef = JSObjectMakeDate((JSContextRef)ctx, (size_t)len, (len==0)?NULL:elements,
                                          &exception);
    JSValueProtect((JSContextRef) ctx, objRef);
    env->SetLongField( out, fid, (jlong) objRef );

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    delete [] elements;
    return out;
}

JSC_NATIVE_METHOD(JSObject,jobject,NJSmakeError) (JSC_NATIVE_PARAMS, jlong ctx, jlongArray args) {
    JSValueRef exception = NULL;

    int i;
    jsize len = env->GetArrayLength(args);
    jlong *values = env->GetLongArrayElements(args, 0);
    JSValueRef* elements = new JSValueRef[len];
    for (i=0; i<len; i++) {
        elements[i] = (JSValueRef) values[i];
    }
    env->ReleaseLongArrayElements(args, values, 0);

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");

    JSObjectRef objRef = JSObjectMakeError((JSContextRef)ctx, (size_t)len, (len==0)?NULL:elements,
                                           &exception);
    JSValueProtect((JSContextRef) ctx, objRef);
    env->SetLongField( out, fid, (long) objRef );

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    delete [] elements;
    return out;
}

JSC_NATIVE_METHOD(JSObject,jobject,NJSmakeRegExp) (JSC_NATIVE_PARAMS, jlong ctx, jlongArray args) {
    JSValueRef exception = NULL;

    int i;
    jsize len = env->GetArrayLength(args);
    jlong *values = env->GetLongArrayElements(args, 0);
    JSValueRef* elements = new JSValueRef[len];
    for (i=0; i<len; i++) {
        elements[i] = (JSValueRef) values[i];
    }
    env->ReleaseLongArrayElements(args, values, 0);

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");

    JSObjectRef objRef = JSObjectMakeRegExp((JSContextRef)ctx, (size_t)len, (len==0)?NULL:elements,
                                            &exception);
    JSValueProtect((JSContextRef) ctx, objRef);
    env->SetLongField( out, fid, (long) objRef );

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    delete [] elements;
    return out;
}

JSC_NATIVE_METHOD(JSObject,jobject,makeFunction) (JSC_NATIVE_PARAMS, jlong ctx, jlong name,
                                       jlongArray parameterNames, jlong body, jlong sourceURL, jint startingLineNumber) {

    JSValueRef exception = NULL;

    int i;
    jsize len = env->GetArrayLength(parameterNames);
    jlong *parameters = env->GetLongArrayElements(parameterNames, 0);
    JSStringRef* parameterNameArr = new JSStringRef[len];
    for (i=0; i<len; i++) {
        parameterNameArr[i] = (JSStringRef) parameters[i];
    }
    env->ReleaseLongArrayElements(parameterNames, parameters, 0);

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");

    JSObjectRef objref = JSObjectMakeFunction(
            (JSContextRef)ctx,
            (JSStringRef)name,
            (unsigned)len,
            (len==0)?NULL:parameterNameArr,
            (JSStringRef) body,
            (JSStringRef) sourceURL,
            (int)startingLineNumber,
            &exception);
    JSValueProtect((JSContextRef) ctx, objref);
    env->SetLongField( out, fid, (long)objref);

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    delete [] parameterNameArr;
    return out;
}

JSC_NATIVE_METHOD(JSObject,jlong,NJSgetPrototype) (JSC_NATIVE_PARAMS, jlong ctx, jlong object) {
    JSValueRef value = JSObjectGetPrototype((JSContextRef)ctx, (JSObjectRef)object);
    JSValueProtect((JSContextRef)ctx, value);
    return (long)value;
}

JSC_NATIVE_METHOD(JSObject,void,NJSsetPrototype) (JSC_NATIVE_PARAMS, jlong ctx, jlong object, jlong value) {
    JSObjectSetPrototype((JSContextRef)ctx, (JSObjectRef)object, (JSValueRef)value);
}

JSC_NATIVE_METHOD(JSObject,jboolean,NJShasProperty) (JSC_NATIVE_PARAMS, jlong ctx, jlong object, jlong propertyName) {
    return JSObjectHasProperty((JSContextRef)ctx, (JSObjectRef) object, (JSStringRef)propertyName);
}

JSC_NATIVE_METHOD(JSObject,jobject,NJSgetProperty) (JSC_NATIVE_PARAMS, jlong ctx, jlong object,
                                      jlong propertyName) {

    JSValueRef exception = NULL;

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");

    JSValueRef value = JSObjectGetProperty((JSContextRef)ctx, (JSObjectRef)object, (JSStringRef)propertyName,
                                           &exception);
    JSValueProtect((JSContextRef) ctx, value);

    env->SetLongField( out, fid, (long)value);

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    return out;
}

JSC_NATIVE_METHOD(JSObject,jobject,NJSsetProperty) (JSC_NATIVE_PARAMS, jlong ctx, jlong object, jlong propertyName,
                                      jlong value, jint attributes) {

    JSValueRef exception = NULL;

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    JSObjectSetProperty((JSContextRef)ctx, (JSObjectRef) object, (JSStringRef) propertyName,
                        (JSValueRef)value, (JSPropertyAttributes)attributes, &exception);

    jfieldID fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    return out;
}

JSC_NATIVE_METHOD(JSObject,jobject,NJSdeleteProperty) (JSC_NATIVE_PARAMS, jlong ctx, jlong object,
                                         jlong propertyName) {

    JSValueRef exception = NULL;

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret ,"bool", "Z");

    bool bval = (bool) JSObjectDeleteProperty((JSContextRef)ctx, (JSObjectRef) object,
                                              (JSStringRef) propertyName, &exception);

    env->SetBooleanField( out, fid, bval);

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    return out;
}

JSC_NATIVE_METHOD(JSObject,jobject,NJSgetPropertyAtIndex) (JSC_NATIVE_PARAMS, jlong ctx, jlong object,
                                             jint propertyIndex) {

    JSValueRef exception = NULL;

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");

    JSValueRef value = JSObjectGetPropertyAtIndex((JSContextRef)ctx, (JSObjectRef) object,
                                                  (unsigned)propertyIndex, &exception);
    JSValueProtect((JSContextRef)ctx, value);

    env->SetLongField( out, fid, (long)value );

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    return out;
}

JSC_NATIVE_METHOD(JSObject,jobject,NJSsetPropertyAtIndex) (JSC_NATIVE_PARAMS, jlong ctx, jlong object,
                                             jint propertyIndex, jlong value) {

    JSValueRef exception = NULL;

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");

    JSObjectSetPropertyAtIndex((JSContextRef)ctx, (JSObjectRef) object, (unsigned) propertyIndex,
                               (JSValueRef)value, &exception);

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);


    return out;
}

JSC_NATIVE_METHOD(JSObject,jlong,NJSgetPrivate) (JSC_NATIVE_PARAMS, jlong object) {
    return (long) JSObjectGetPrivate((JSObjectRef) object);
}

JSC_NATIVE_METHOD(JSObject,jboolean,NJSsetPrivate) (JSC_NATIVE_PARAMS, jlong object, jlong data) {
    return JSObjectSetPrivate((JSObjectRef) object, (void*) data);
}

JSC_NATIVE_METHOD(JSObject,jboolean,NJSisFunction) (JSC_NATIVE_PARAMS, jlong ctx, jlong object) {
    return (jboolean) JSObjectIsFunction((JSContextRef)ctx, (JSObjectRef) object);
}

JSC_NATIVE_METHOD(JSObject,jobject,NJScallAsFunction) (JSC_NATIVE_PARAMS, jlong ctx, jlong object,
                                         jlong thisObject, jlongArray args) {
    JSValueRef exception = NULL;

    int i;
    jsize len = env->GetArrayLength(args);
    jlong *values = env->GetLongArrayElements(args, 0);
    JSValueRef* elements = new JSValueRef[len];
    for (i=0; i<len; i++) {
        elements[i] = (JSValueRef) values[i];
    }
    env->ReleaseLongArrayElements(args, values, 0);

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");

    JSValueRef value = JSObjectCallAsFunction((JSContextRef)ctx, (JSObjectRef) object, (JSObjectRef) thisObject,
                                              (size_t)len, (len==0)?NULL:elements, &exception);
    JSValueProtect((JSContextRef) ctx, value);

    env->SetLongField( out, fid, (long)value);

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    delete [] elements;
    return out;
}

JSC_NATIVE_METHOD(JSObject,jboolean,NJSisConstructor) (JSC_NATIVE_PARAMS, jlong ctx, jlong object) {
    return (jboolean) JSObjectIsConstructor((JSContextRef)ctx, (JSObjectRef)object);
}

JSC_NATIVE_METHOD(JSObject,jobject,NJScallAsConstructor) (JSC_NATIVE_PARAMS, jlong ctx, jlong object,
                                            jlongArray args) {

    JSValueRef exception = NULL;

    int i;
    jsize len = env->GetArrayLength(args);
    jlong *values = env->GetLongArrayElements(args, 0);
    JSValueRef* elements = new JSValueRef[len];
    for (i=0; i<len; i++) {
        elements[i] = (JSValueRef) values[i];
    }
    env->ReleaseLongArrayElements(args, values, 0);

    jclass ret = env->FindClass("io/nodekit/engine/javascriptcore/JSValue$JNIReturnObject");
    jmethodID cid = env->GetMethodID(ret,"<init>","()V");
    jobject out = env->NewObject(ret, cid);

    jfieldID fid = env->GetFieldID(ret , "reference", "J");

    JSValueRef value = JSObjectCallAsConstructor((JSContextRef)ctx, (JSObjectRef) object,
                                                 (size_t)len, (len==0)?NULL:elements, &exception);
    JSValueProtect((JSContextRef) ctx, value);

    env->SetLongField( out, fid, (long)value);

    fid = env->GetFieldID(ret , "exception", "J");
    env->SetLongField( out, fid, (long) exception);

    delete [] elements;
    return out;
}

JSC_NATIVE_METHOD(JSObject,jlong,NJScopyPropertyNames) (JSC_NATIVE_PARAMS, jlong ctx, jlong object) {
    JSPropertyNameArrayRef ref = JSObjectCopyPropertyNames((JSContextRef)ctx, (JSObjectRef)object);
    JSPropertyNameArrayRetain(ref);
    return (long)ref;
}

JSC_NATIVE_METHOD(JSObject,jlongArray,NJSgetPropertyNames) (JSC_NATIVE_PARAMS,jlong propertyNameArray) {
    size_t count = JSPropertyNameArrayGetCount((JSPropertyNameArrayRef)propertyNameArray);
    jlongArray retArray = env->NewLongArray(count);
    jlong* stringRefs = new jlong[count];
    for (size_t i=0; i<count; i++) {
        stringRefs[i] = (long) JSStringRetain(JSPropertyNameArrayGetNameAtIndex(
                (JSPropertyNameArrayRef)propertyNameArray, i));
    }
    env->SetLongArrayRegion(retArray,0,count,stringRefs);

    return retArray;
}

JSC_NATIVE_METHOD(JSObject,void,NJSreleasePropertyNames) (JSC_NATIVE_PARAMS, jlong propertyNameArray) {
    JSPropertyNameArrayRelease((JSPropertyNameArrayRef)propertyNameArray);
}
