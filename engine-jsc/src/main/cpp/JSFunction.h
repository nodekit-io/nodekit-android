#ifndef JNI_JSOBJ_H
#define JNI_JSOBJ_H

#include <jni.h>
#include <JavaScriptCore/JSContextRef.h>
#include <JavaScriptCore/JSValueRef.h>
#include "JSInstance.h"

class JSFunction : public JSInstance {
public:
    JSFunction(JNIEnv *env, jobject thiz, JSContextRef ctx, JSStringRef name = NULL);
    virtual ~JSFunction();

private:
    static JSValueRef StaticFunctionCallback(JSContextRef ctx, JSObjectRef function,
                                             JSObjectRef thisObject,size_t argumentCount, const JSValueRef arguments[],
                                             JSValueRef* exception);
    static JSObjectRef StaticConstructorCallback(JSContextRef ctx,
                                                 JSObjectRef constructor,size_t argumentCount,const JSValueRef arguments[],
                                                 JSValueRef* exception);
    static bool StaticHasInstanceCallback(JSContextRef ctx, JSObjectRef constructor,
                                          JSValueRef possibleInstance, JSValueRef* exception);
    static JSClassDefinition JSFunctionClassDefinition();

    JSObjectRef ConstructorCallback(JSContextRef ctx, JSObjectRef constructor,
                                    size_t argumentCount, const JSValueRef arguments[], JSValueRef* exception);
    JSValueRef FunctionCallback(JSContextRef ctx, JSObjectRef function,
                                JSObjectRef thisObject, size_t argumentCount,const JSValueRef arguments[],
                                JSValueRef* exception);
    bool HasInstanceCallback(JSContextRef ctx, JSObjectRef constructor,
                             JSValueRef possibleInstance, JSValueRef* exception);
};

#endif
