#include <JavaScriptCore/JSStringRef.h>
#include "JSUtils.h"

JSC_NATIVE_METHOD(JSString,jlong,NJScreateWithCharacters) (JSC_NATIVE_PARAMS, jstring str)
{
const jchar *chars = env->GetStringChars(str, NULL);
JSStringRef string = JSStringRetain(JSStringCreateWithCharacters(chars,
                                                                 env->GetStringLength(str)));
env->ReleaseStringChars(str,chars);
return (long)string;
}

JSC_NATIVE_METHOD(JSString,jlong,NJScreateWithUTF8CString) (JSC_NATIVE_PARAMS, jstring str)
{
const char *string = env->GetStringUTFChars(str, NULL);
JSStringRef ret = JSStringRetain(JSStringCreateWithUTF8CString(string));
env->ReleaseStringUTFChars(str, string);
return (long)ret;
}

JSC_NATIVE_METHOD(JSString,jlong,NJSretain) (JSC_NATIVE_PARAMS, jlong strRef) {
return (jlong) JSStringRetain((JSStringRef)strRef);
}

JSC_NATIVE_METHOD(JSString,void,NJSrelease) (JSC_NATIVE_PARAMS, jlong stringRef) {
JSStringRelease((JSStringRef)stringRef);
}

JSC_NATIVE_METHOD(JSString,jint,NJSgetLength) (JSC_NATIVE_PARAMS, jlong stringRef) {
return (jint) JSStringGetLength((JSStringRef)stringRef);
}

JSC_NATIVE_METHOD(JSString,jstring,NJStoString) (JSC_NATIVE_PARAMS, jlong stringRef) {
char *buffer = new char[JSStringGetMaximumUTF8CStringSize((JSStringRef)stringRef)+1];
JSStringGetUTF8CString((JSStringRef)stringRef, buffer,
JSStringGetMaximumUTF8CStringSize((JSStringRef)stringRef)+1);
jstring ret = env->NewStringUTF(buffer);
delete[] buffer;
return ret;
}

JSC_NATIVE_METHOD(JSString,jint,NJSgetMaximumUTF8CStringSize) (JSC_NATIVE_PARAMS, jlong stringRef) {
return (jint) JSStringGetMaximumUTF8CStringSize((JSStringRef)stringRef);
}

JSC_NATIVE_METHOD(JSString,jboolean,NJSisEqual) (JSC_NATIVE_PARAMS, jlong a, jlong b) {
return (jboolean) JSStringIsEqual((JSStringRef)a, (JSStringRef)b);
}

JSC_NATIVE_METHOD(JSString,jboolean,NJSisEqualToUTF8CString) (JSC_NATIVE_PARAMS, jlong a, jstring b) {
const char *string = env->GetStringUTFChars(b, NULL);
jboolean ret = JSStringIsEqualToUTF8CString((JSStringRef)a, string);
env->ReleaseStringUTFChars(b, string);
return ret;
}


